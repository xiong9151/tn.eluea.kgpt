/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
package tn.eluea.kgpt.text.transform.format;

public interface CharacterFilter {

    CharacterFilter noCharacterFilter = c -> true;

    boolean filterCharacter(char c);
}
