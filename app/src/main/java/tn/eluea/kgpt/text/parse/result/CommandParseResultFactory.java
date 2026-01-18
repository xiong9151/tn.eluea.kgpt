/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
package tn.eluea.kgpt.text.parse.result;

import java.util.List;

public class CommandParseResultFactory implements ParseResultFactory {
    public CommandParseResultFactory() {
    }

    @Override
    public ParseResult getParseResult(List<String> groups, int indexStart, int indexEnd) {
        return new CommandParseResult(groups, indexStart, indexEnd);
    }
}
