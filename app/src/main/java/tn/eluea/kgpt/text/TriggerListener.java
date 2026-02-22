package tn.eluea.kgpt.text;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import tn.eluea.kgpt.SPManager;
import tn.eluea.kgpt.text.parse.ParsePattern;
import tn.eluea.kgpt.text.parse.PatternType;
import tn.eluea.kgpt.text.parse.result.ParseResult;
import tn.eluea.kgpt.text.parse.result.ParseResultFactory;

/**
 * 触发器监听器，用于智能检测触发器输入并进行匹配
 * 优化思路：
 * 1. 只在触发器被输入时进行匹配
 * 2. 默认模式只匹配光标所在行
 * 3. 范围选择模式监听结束符，然后往前检索起始符
 */
public class TriggerListener {
    private static final String TAG = "KGPT_TriggerListener";
    
    private final List<TriggerInfo> triggerInfos = new ArrayList<>();
    private String currentTriggerSymbol = "$";
    private boolean aiTriggerEnabled = false;
    
    public static class TriggerInfo {
        public final String startSymbol;
        public final String endSymbol;
        public final Pattern pattern;
        public final ParseResultFactory factory;
        public final PatternType type;
        public final boolean isRangeSelection;
        
        public TriggerInfo(String symbol, Pattern pattern, ParseResultFactory factory, PatternType type) {
            this(symbol, symbol, pattern, factory, type); // 默认开始符和结束符相同
        }
        
        public TriggerInfo(String startSymbol, String endSymbol, Pattern pattern, ParseResultFactory factory, PatternType type) {
            this.startSymbol = startSymbol;
            this.endSymbol = endSymbol;
            this.pattern = pattern;
            this.factory = factory;
            this.type = type;
            this.isRangeSelection = (type == PatternType.RangeSelection);
        }
    }
    
    public TriggerListener() {
        // 初始化默认触发器
        updateTriggers(SPManager.getInstance().getParsePatterns());
    }
    
    public void updateTriggers(List<ParsePattern> parsePatterns) {
        triggerInfos.clear();
        aiTriggerEnabled = false;
        
        for (ParsePattern parsePattern : parsePatterns) {
            if (parsePattern.isEnabled()) {
                PatternType type = parsePattern.getType();
                String startSymbol = null;
                String endSymbol = null;
                
                if (type == PatternType.RangeSelection) {
                    // 尝试提取开始符和结束符
                    String regex = parsePattern.getPattern().pattern();
                    startSymbol = extractStartSymbolFromRangeRegex(regex);
                    endSymbol = extractEndSymbolFromRangeRegex(regex);
                    
                    // 如果无法提取，则使用默认符号
                    if (startSymbol == null || startSymbol.isEmpty()) {
                        startSymbol = type.defaultSymbol;
                    }
                    if (endSymbol == null || endSymbol.isEmpty()) {
                        endSymbol = type.defaultSymbol;
                    }
                } else {
                    // 非范围选择模式，开始符和结束符相同
                    startSymbol = PatternType.regexToSymbol(parsePattern.getPattern().pattern());
                    endSymbol = startSymbol;
                }
                
                if (startSymbol != null && !startSymbol.isEmpty()) {
                    if (type == PatternType.CommandAI || type == PatternType.RangeSelection) {
                        currentTriggerSymbol = startSymbol;
                        aiTriggerEnabled = true;
                    }
                    
                    triggerInfos.add(new TriggerInfo(
                        startSymbol,
                        endSymbol,
                        parsePattern.getPattern(),
                        ParseResultFactory.of(type),
                        type
                    ));
                }
            }
        }
    }
    
    /**
     * 从范围选择正则表达式中提取开始符
     */
    private String extractStartSymbolFromRangeRegex(String regex) {
        if (regex == null || regex.isEmpty()) {
            return null;
        }
        
        // 检查是否是不同开始符和结束符的模式: startSymbol((?s).+?)endSymbol$
        java.util.regex.Pattern rangePattern = java.util.regex.Pattern.compile("^(.+?)\\(\\(\\?s\\)\\.\\+\\?\\)(.+?)\\$$");
        java.util.regex.Matcher rangeMatcher = rangePattern.matcher(regex);
        if (rangeMatcher.find()) {
            return unescapeRegex(rangeMatcher.group(1));
        }
        
        // 检查是否是相同符号的模式: \$((?s).+?)\$$
        java.util.regex.Pattern sameSymbolPattern = java.util.regex.Pattern.compile("^(\\\\.)(\\(\\(\\?s\\)\\.\\+\\?\\))\\\\\\1\\$$");
        java.util.regex.Matcher sameSymbolMatcher = sameSymbolPattern.matcher(regex);
        if (sameSymbolMatcher.find()) {
            return sameSymbolMatcher.group(1).substring(1); // 移除转义反斜杠
        }
        
        // 尝试通用方法
        return PatternType.regexToSymbol(regex);
    }
    
    /**
     * 从范围选择正则表达式中提取结束符
     */
    private String extractEndSymbolFromRangeRegex(String regex) {
        if (regex == null || regex.isEmpty()) {
            return null;
        }
        
        // 检查是否是不同开始符和结束符的模式: startSymbol((?s).+?)endSymbol$
        java.util.regex.Pattern rangePattern = java.util.regex.Pattern.compile("^(.+?)\\(\\(\\?s\\)\\.\\+\\?\\)(.+?)\\$$");
        java.util.regex.Matcher rangeMatcher = rangePattern.matcher(regex);
        if (rangeMatcher.find()) {
            return unescapeRegex(rangeMatcher.group(2));
        }
        
        // 检查是否是相同符号的模式: \$((?s).+?)\$$
        java.util.regex.Pattern sameSymbolPattern = java.util.regex.Pattern.compile("^(\\\\.)(\\(\\(\\?s\\)\\.\\+\\?\\))\\\\\\1\\$$");
        java.util.regex.Matcher sameSymbolMatcher = sameSymbolPattern.matcher(regex);
        if (sameSymbolMatcher.find()) {
            return sameSymbolMatcher.group(1).substring(1); // 移除转义反斜杠
        }
        
        // 尝试通用方法（返回开始符作为结束符）
        return PatternType.regexToSymbol(regex);
    }
    
    /**
     * 反转义正则表达式中的特殊字符
     */
    private String unescapeRegex(String escaped) {
        if (escaped == null || escaped.isEmpty()) {
            return escaped;
        }
        
        // Replace escaped special characters
        return escaped.replace("\\$", "$")
                     .replace("\\.", ".")
                     .replace("\\^", "^")
                     .replace("\\*", "*")
                     .replace("\\+", "+")
                     .replace("\\?", "?")
                     .replace("\\(", "(")
                     .replace("\\)", ")")
                     .replace("\\[", "[")
                     .replace("\\]", "]")
                     .replace("\\{", "{")
                     .replace("\\}", "}")
                     .replace("\\|", "|")
                     .replace("\\\\", "\\");
    }
    
    /**
     * 检查是否刚刚输入了完整的触发器符号
     */
    private boolean isTriggerJustEntered(String text, String triggerSymbol) {
        if (text == null || triggerSymbol == null || triggerSymbol.isEmpty()) {
            return false;
        }
        
        // 检查文本是否以触发器符号结尾
        if (!text.endsWith(triggerSymbol)) {
            return false;
        }
        
        // 确保这是完整输入的触发器符号
        // 对于范围选择模式，我们需要确保这不是起始符的一部分
        // 但这里我们只关心是否完整输入了结束符
        return true;
    }
    
    /**
     * 检查是否刚刚输入了触发器
     * @param oldText 之前的文本
     * @param newText 新的文本
     * @param cursor 光标位置
     * @return 是否检测到触发器输入
     */
    public boolean shouldCheckForTrigger(String oldText, String newText, int cursor) {
        if (newText == null || cursor <= 0) {
            return false;
        }
        
        // 检查是否有字符被添加
        if (oldText != null && newText.length() <= oldText.length()) {
            return false;
        }
        
        // 获取光标前的字符
        String textBeforeCursor = newText.substring(0, cursor);
        
        // 检查每个触发器
        for (TriggerInfo info : triggerInfos) {
            // 对于范围选择模式，只在输入结束符时触发
            if (info.isRangeSelection) {
                if (textBeforeCursor.endsWith(info.endSymbol)) {
                    // 确保这是新输入的结束符
                    String oldTextBeforeCursor = "";
                    if (oldText != null) {
                        oldTextBeforeCursor = oldText.substring(0, Math.min(oldText.length(), cursor));
                    }
                    if (!oldTextBeforeCursor.endsWith(info.endSymbol)) {
                        return true;
                    }
                }
            } else {
                // 对于普通触发器，检查是否刚输入
                if (isTriggerJustEntered(textBeforeCursor, info.endSymbol)) {
                    // 确保这是新输入的触发器
                    String oldTextBeforeCursor = "";
                    if (oldText != null) {
                        oldTextBeforeCursor = oldText.substring(0, Math.min(oldText.length(), cursor));
                    }
                    if (!oldTextBeforeCursor.endsWith(info.endSymbol)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 解析文本，只在触发器被输入时进行
     */
    public ParseResult parseOnTrigger(String text, int cursor) {
        if (text == null || text.isEmpty() || cursor <= 0) {
            return null;
        }
        
        String textBeforeCursor = text.substring(0, cursor);
        
        // 首先检查范围选择模式（需要特殊处理）
        for (TriggerInfo info : triggerInfos) {
            if (info.isRangeSelection) {
                ParseResult result = parseRangeSelection(textBeforeCursor, info);
                if (result != null) {
                    return result;
                }
            }
        }
        
        // 然后检查普通触发器模式
        for (TriggerInfo info : triggerInfos) {
            if (!info.isRangeSelection) {
                ParseResult result = parseSingleLineTrigger(textBeforeCursor, info);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 解析范围选择模式：startSymbol text endSymbol
     * 只在结束符被完整输入时才进行匹配
     */
    private ParseResult parseRangeSelection(String text, TriggerInfo info) {
        if (text == null || info.endSymbol == null || info.endSymbol.isEmpty()) {
            return null;
        }
        
        // 精确检查是否以结束符结尾
        if (!text.endsWith(info.endSymbol)) {
            return null;
        }
        
        // 获取结束符的开始位置
        int endSymbolStart = text.length() - info.endSymbol.length();
        if (endSymbolStart <= 0) {
            return null;
        }
        
        // 在结束符之前查找起始符
        String textBeforeEndSymbol = text.substring(0, endSymbolStart);
        int startSymbolPos = -1;
        
        // 从后往前查找起始符，确保找到最近的一个
        for (int i = textBeforeEndSymbol.length() - info.startSymbol.length(); i >= 0; i--) {
            if (textBeforeEndSymbol.startsWith(info.startSymbol, i)) {
                startSymbolPos = i;
                break;
            }
        }
        
        // 起始符必须存在
        if (startSymbolPos == -1) {
            return null;
        }
        
        // 提取中间的文本（允许包含换行）
        int capturedStart = startSymbolPos + info.startSymbol.length();
        int capturedEnd = endSymbolStart;
        
        if (capturedStart >= capturedEnd) {
            return null; // 中间没有内容
        }
        
        String capturedText = text.substring(capturedStart, capturedEnd);
        if (capturedText.isEmpty()) {
            return null;
        }
        
        // 创建匹配结果
        java.util.List<String> groups = new ArrayList<>();
        String fullMatch = text.substring(startSymbolPos, endSymbolStart + info.endSymbol.length());
        groups.add(fullMatch); // full match
        groups.add(capturedText); // captured text
        
        return info.factory.getParseResult(groups, startSymbolPos, endSymbolStart + info.endSymbol.length());
    }
    
    /**
     * 解析单行触发器模式（如 text$）
     * 只匹配光标所在行，遇到换行符就截断
     */
    private ParseResult parseSingleLineTrigger(String text, TriggerInfo info) {
        if (!text.endsWith(info.endSymbol)) {
            return null;
        }
        
        // 找到光标所在行的开始位置（从后往前找换行符）
        int lineStart = 0;
        for (int i = text.length() - 1; i >= 0; i--) {
            if (text.charAt(i) == '\n') {
                lineStart = i + 1;
                break;
            }
        }
        
        // 只处理当前行
        String currentLine = text.substring(lineStart);
        if (!currentLine.endsWith(info.endSymbol)) {
            return null;
        }
        
        // 移除触发器符号，获取要发送的文本
        String capturedText = currentLine.substring(0, currentLine.length() - info.endSymbol.length());
        if (capturedText.isEmpty()) {
            return null;
        }
        
        // 创建匹配结果
        java.util.List<String> groups = new ArrayList<>();
        groups.add(currentLine); // full match
        groups.add(capturedText); // captured text
        
        return info.factory.getParseResult(groups, lineStart, text.length());
    }
    
    public boolean isAiTriggerEnabled() {
        return aiTriggerEnabled;
    }
    
    public String getCurrentTriggerSymbol() {
        return currentTriggerSymbol;
    }
}