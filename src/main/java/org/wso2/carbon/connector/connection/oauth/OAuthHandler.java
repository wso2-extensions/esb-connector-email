/*
 * Copyright (c) 2023, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.connector.connection.oauth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This abstract class is to be used by OAuth handlers
 * This class checks validity of tokens, request for tokens and add tokens to in-memory cache
 */
public abstract class OAuthHandler {
    private static final Log log = LogFactory.getLog(OAuthHandler.class);

    private final String username;
    private final String clientId;
    private final String clientSecret;
    private final String tokenUrl;
    private final String tokenId;

    protected OAuthHandler(String username, String clientId, String clientSecret, String tokenUrl, String tokenId) {
        this.username = username;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUrl = tokenUrl;
        this.tokenId = tokenId;
    }

    /**
     * Generate access-token
     * @return Access token
     * @throws EmailConnectionException
     */
    public String generateAccessToken() throws EmailConnectionException {
        log.debug("Generating access token");
        Token token = refreshAccessToken();
        storeToken(token);
        return token.getAccessToken();
    }

    private void storeToken(Token token) {
        TokenCache tokenCache = TokenCache.getInstance();
        tokenCache.addToken(tokenId, token);
    }

    private Token refreshAccessToken() throws EmailConnectionException {
        Map<String,String> headers = new HashMap<>();
        headers.put(OAuthConstants.HEADER_CONTENT_TYPE, OAuthConstants.APPLICATION_X_WWW_FORM_URLENCODED);
        try {
            return OAuthClient.generateAccessToken(getTokenUrl(), headers, buildTokenRequestPayload());
        } catch (IOException e) {
            throw new EmailConnectionException("An error occurred while refreshing access token", e.getCause());
        }
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getTokenId() {
        return tokenId;
    }

    protected abstract String buildTokenRequestPayload();

}
