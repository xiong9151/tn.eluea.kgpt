/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
package tn.eluea.kgpt.text.parse.result;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tn.eluea.kgpt.text.parse.PatternType;

/**
 * Factory for creating RangeSelectionParseResult.
 * Handles patterns like $text$ or @text@ where text between symbols is sent to AI.
 */
public class RangeSelectionParseResultFactory implements ParseResultFactory {

    private final String triggerSymbol;

    public RangeSelectionParseResultFactory(String triggerSymbol) {
        this.triggerSymbol = triggerSymbol;
    }

    @Override
    public ParseResult getParseResult(List<String> groups, int indexStart, int indexEnd) {
        // Validate input parameters
        if (groups == null || groups.size() < 2) {
            throw new IllegalArgumentException("Groups must contain at least full match and captured text");
        }
        
        // The groups list should contain: [fullMatch, capturedText]
        String fullMatch = groups.get(0);
        String capturedText = groups.get(1);
        
        // Create AI parse result with the extracted text
        return new AIParseResult(capturedText, indexStart, indexEnd);
    }
}