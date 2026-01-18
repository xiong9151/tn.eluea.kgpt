/*
 * Copyright (c) 2025 Amr Aldeeb @Eluea
 * GitHub: https://github.com/Eluea
 * Telegram: https://t.me/Eluea
 *
 * Licensed under the GPLv3.
 */
package tn.eluea.kgpt.llm.internet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import tn.eluea.kgpt.llm.service.InternetRequestListener;

public interface InternetProvider {
    InputStream sendRequest(HttpURLConnection con, String body, InternetRequestListener irl) throws IOException;
}
