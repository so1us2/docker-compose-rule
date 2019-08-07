/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.docker.compose.reporting;

import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.java.config.ssl.PemX509Certificate;
import com.palantir.conjure.java.config.ssl.SslSocketFactories;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.HttpsURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebhookPoster {
    private static final Logger log = LoggerFactory.getLogger(WebhookPoster.class);

    private final ReportingConfig reportingConfig;

    WebhookPoster(ReportingConfig reportingConfig) {
        this.reportingConfig = reportingConfig;
    }

    public void postHook(String json) {
        try {
            URL url = new URL(reportingConfig.url());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (url.getProtocol().equals("https") && reportingConfig.caCert().isPresent()) {
                PemX509Certificate caCert = PemX509Certificate.of(reportingConfig.caCert().get());
                Map<String, PemX509Certificate> caCerts = ImmutableMap.of("ca-cert", caCert);

                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                httpsConnection.setSSLSocketFactory(SslSocketFactories.createSslSocketFactory(caCerts));
            }

            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/json");

            String version = Optional.ofNullable(this.getClass().getPackage().getImplementationVersion())
                    .orElse("unknown");
            connection.setRequestProperty("User-Agent", "docker-compose-rule/" + version);

            connection.setDoOutput(true);
            PrintWriter body = new PrintWriter(connection.getOutputStream());
            body.println(json);
            body.close();

            connection.connect();
        } catch (Exception e) {
            log.error("Failed to post report", e);
        }
    }
}