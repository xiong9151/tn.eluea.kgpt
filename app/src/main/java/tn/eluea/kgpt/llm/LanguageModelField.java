/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
package tn.eluea.kgpt.llm;

import android.text.InputType;
import tn.eluea.kgpt.R;

public enum LanguageModelField {
        ApiKey("api_key", R.string.field_api_key, Type.String,
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, false),
        SubModel("sub_model", R.string.sub_model, Type.String,
                        InputType.TYPE_CLASS_TEXT, false),
        BaseUrl("base_url", R.string.base_url, Type.String,
                        InputType.TYPE_CLASS_TEXT, false),
        MaxTokens("max_tokens", R.string.max_tokens, Type.Integer,
                        InputType.TYPE_CLASS_NUMBER, true),
        Temperature("temperature", R.string.temperature, Type.Double,
                        InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, true),
        TopP("top_p", R.string.field_top_p, Type.Double,
                        InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, true);

        public final String name;
        public final int titleResId;
        public final Type type;
        public final int inputType;
        public final boolean advanced;

        LanguageModelField(String name, int titleResId, Type type, int inputType, boolean advanced) {
                this.name = name;
                this.titleResId = titleResId;
                this.type = type;
                this.inputType = inputType;
                this.advanced = advanced;
        }

        public enum Type {
                String,
                Integer,
                Double
        }
}
