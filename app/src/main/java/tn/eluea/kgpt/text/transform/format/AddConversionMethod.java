/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
package tn.eluea.kgpt.text.transform.format;

public class AddConversionMethod extends ConversionMethod {
    private int mOffset;
    public AddConversionMethod(int offset) {
        mOffset = offset;
    }

    @Override
    public String doConvert(char c) {
        if (Character.isSpaceChar(c))
            return String.valueOf(c);
        return new StringBuilder()
                .append(c)
                .append((char)mOffset)
                .toString();
    }
}
