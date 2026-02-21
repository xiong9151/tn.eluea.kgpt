/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
package tn.eluea.kgpt.text.parse.result;

import java.util.List;

public class RangeSelectionParseResultFactory implements ParseResultFactory {
    public RangeSelectionParseResultFactory() {
    }

    @Override
    public ParseResult getParseResult(List<String> groups, int indexStart, int indexEnd) {
        // Use AIParseResult since it's already handled by BrainDispatcher
        return new AIParseResult(groups, indexStart, indexEnd);
    }
}