/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
package tn.eluea.kgpt.text.parse;

import tn.eluea.kgpt.R;

public enum PatternType {
    Settings(R.string.title_settings_trigger, 0, "€$", true, "€", R.string.example_opens_settings),
    CommandAI(R.string.title_ai_trigger, 1, "(.+)\\$$", true, "$", R.string.example_ai_responds),
    CommandCustom(R.string.title_custom_command, 2, "([^%]+)%(?:([^ %]+))?%$", true, "%", R.string.example_translates),
    FormatItalic(R.string.title_italic, 1, "([^|]+)\\|$", true, "|", R.string.example_format_italic),
    FormatBold(R.string.title_bold, 1, "([^@]+)@$", true, "@", R.string.example_format_bold),
    FormatCrossout(R.string.title_crossout, 1, "([^~]+)~$", true, "~", R.string.example_format_crossout),
    FormatUnderline(R.string.title_underline, 1, "([^_]+)_$", true, "_", R.string.example_format_underline),
    WebSearch(R.string.title_web_search, 1, "(.+)\\?\\?$", true, "??", R.string.example_ai_responds), // Reusing generic
                                                                                                      // example or
                                                                                                      // create new if
                                                                                                      // needed
    RangeSelection(R.string.title_range_selection, 3, "\\$(.+)\\$$", true, "$", R.string.example_range_selection);

    public final int titleResId;
    public final int groupCount;
    public final String defaultPattern;
    public final boolean editable;
    public final String defaultSymbol;
    public final int exampleResId;

    PatternType(int titleResId, int groupCount, String defaultPattern, boolean editable, String defaultSymbol,
            int exampleResId) {
        this.titleResId = titleResId;
        this.groupCount = groupCount;
        this.defaultPattern = defaultPattern;
        this.editable = editable;
        this.defaultSymbol = defaultSymbol;
        this.exampleResId = exampleResId;
    }

    /**
     * Convert a user-friendly symbol to regex pattern
     * The new logic: text is written normally, symbol at the END triggers AI
     * IMPORTANT: Requires at least one character before the trigger symbol
     */
    public static String symbolToRegex(String symbol, int groupCount) {
        if (symbol == null || symbol.isEmpty()) {
            return null;
        }

        String escapedSymbol = escapeRegex(symbol);

        if (groupCount == 0) {
            // For Settings-like patterns: exact match of the symbol
            return String.format("%s$", escapedSymbol);
        } else if (groupCount == 1) {
            // For multi-char symbols like "??", use (.+) to capture any text (at least 1
            // char)
            // For single-char symbols, use (.+) to allow symbol in text
            return String.format("(.+)%s$", escapedSymbol);
        } else if (groupCount == 2) {
            // Pattern for custom commands: text%command% or text%%
            // Changed from * to + to require at least one character before trigger
            String literalSymbol = escapeLiteralForCharClass(symbol);
            return String.format("([^%s]+)%s(?:([^ %s]+))?%s$", literalSymbol, escapedSymbol, literalSymbol,
                    escapedSymbol);
        } else if (groupCount == 3) {
            // Pattern for range selection: symbol + text + symbol
            // Use non-greedy matching with DOTALL mode only for the capture group
            // The (?s) enables DOTALL mode so . matches newlines, but only for the captured text
            return String.format("%s((?s).+?)%s$", escapedSymbol, escapedSymbol);
        }
        return null;
    }
    
    /**
     * Convert start and end symbols to regex pattern for range selection
     */
    public static String symbolsToRangeRegex(String startSymbol, String endSymbol) {
        if (startSymbol == null || startSymbol.isEmpty() || endSymbol == null || endSymbol.isEmpty()) {
            return null;
        }
        
        String escapedStart = escapeRegex(startSymbol);
        String escapedEnd = escapeRegex(endSymbol);
        // Enable DOTALL mode only for the capture group to match across lines
        return String.format("%s((?s).+?)%s$", escapedStart, escapedEnd);
    }

    /**
     * Extract the trigger symbol from a regex pattern
     */
    public static String regexToSymbol(String regex) {
        if (regex == null || regex.isEmpty()) {
            return null;
        }

        // Check if it's a range selection pattern with two different symbols
        // Pattern like: startSymbol((?s).+?)endSymbol$
        java.util.regex.Pattern rangePattern = java.util.regex.Pattern.compile("^(.+?)\\(\\(\\?s\\)\\.\\+\\?\\)(.+?)\\$$");
        java.util.regex.Matcher rangeMatcher = rangePattern.matcher(regex);
        if (rangeMatcher.find()) {
            // For range selection, return the start symbol as the primary symbol
            return unescapeRegex(rangeMatcher.group(1));
        }
        
        // Check if it's a range selection pattern with same symbol (symbol at both ends)
        // Pattern like: \$((?s).+?)\$$
        java.util.regex.Pattern sameSymbolPattern = java.util.regex.Pattern.compile("^\\\\(.)(\\(\\(\\?s\\)\\.\\+\\?\\))\\\\\\1\\$$");
        java.util.regex.Matcher sameSymbolMatcher = sameSymbolPattern.matcher(regex);
        if (sameSymbolMatcher.find()) {
            return sameSymbolMatcher.group(1);
        }
        
        // For CommandCustom pattern: ([^%]+)%(?:([^ %]+))?%$ - extract the % symbol
        // Look for pattern like ([^X]+)X where X is the symbol
        java.util.regex.Pattern charClassPattern = java.util.regex.Pattern.compile("\\[\\^([^\\]]+)\\]");
        java.util.regex.Matcher matcher = charClassPattern.matcher(regex);
        if (matcher.find()) {
            String charClass = matcher.group(1);
            if (charClass.length() > 0) {
                // Check if it starts with escaped char
                if (charClass.startsWith("\\") && charClass.length() > 1) {
                    return String.valueOf(charClass.charAt(1));
                } else {
                    // Get first character that's not a backslash or space
                    for (int j = 0; j < charClass.length(); j++) {
                        char c = charClass.charAt(j);
                        if (c != ' ' && c != '\\') {
                            return String.valueOf(c);
                        }
                    }
                }
            }
        }

        // Fallback: Try to find the symbol at the end (before $)
        String pattern = regex;
        if (pattern.endsWith("$")) {
            pattern = pattern.substring(0, pattern.length() - 1);
        }

        // Build the symbol by reading escaped characters from the end
        StringBuilder symbol = new StringBuilder();
        int i = pattern.length() - 1;

        while (i >= 0) {
            char c = pattern.charAt(i);

            // Stop at regex special constructs (but not escaped ones)
            if (c == ')' || c == ']' || c == '+' || c == '*' || c == '?') {
                // Check if this is an escaped character
                if (i > 0 && pattern.charAt(i - 1) == '\\') {
                    // It's escaped, include it in symbol
                    symbol.insert(0, c);
                    i -= 2; // Skip the backslash
                    continue;
                }
                // Not escaped, stop here
                break;
            }

            // Check for escaped character
            if (i > 0 && pattern.charAt(i - 1) == '\\') {
                symbol.insert(0, c);
                i -= 2; // Skip the backslash
            } else if (c == '\\') {
                // Lone backslash, stop
                break;
            } else {
                symbol.insert(0, c);
                i--;
            }

            // Limit symbol length to prevent infinite loops
            if (symbol.length() > 20) {
                break;
            }
        }

        return symbol.length() > 0 ? symbol.toString() : null;
    }
    
    private static String unescapeRegex(String escaped) {
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

    private static String escapeRegex(String symbol) {
        // Characters that need escaping in regex
        String specialChars = "\\^$.|?*+()[]{}";
        StringBuilder escaped = new StringBuilder();
        for (char c : symbol.toCharArray()) {
            if (specialChars.indexOf(c) >= 0) {
                escaped.append("\\");
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    private static String escapeLiteralForCharClass(String symbol) {
        // Characters that need escaping inside character class [...]
        String specialChars = "\\^-]";
        StringBuilder escaped = new StringBuilder();
        for (char c : symbol.toCharArray()) {
            if (specialChars.indexOf(c) >= 0) {
                escaped.append("\\");
            }
            escaped.append(c);
        }
        return escaped.toString();
    }
}
