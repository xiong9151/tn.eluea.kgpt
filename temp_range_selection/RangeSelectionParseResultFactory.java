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
        // The groups list should contain: [fullMatch, capturedText]
        return new AIParseResult(groups, indexStart, indexEnd);
    }
}