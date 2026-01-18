/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
package tn.eluea.kgpt.text.parse.result;

import java.util.List;

public class AIParseResult extends ParseResult {
    public final String prompt;

    public AIParseResult(List<String> groups, int indexStart, int indexEnd) {
        super(groups, indexStart, indexEnd);

        this.prompt = groups.get(1);
    }
}
