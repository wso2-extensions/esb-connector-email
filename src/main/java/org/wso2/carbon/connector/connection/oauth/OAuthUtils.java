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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.pojo.ConnectionConfiguration;
import org.wso2.carbon.connector.pojo.OAuthConfig;

/**
 * Utility class for OAuth functions
 * Currently this supports only authorization_code and client_credentials grant type
 */
public class OAuthUtils {

    static final Log log = LogFactory.getLog(OAuthUtils.class);

    /**
     * Generate access token
     * @param connectionConfiguration connection configuration
     * @return Access token generated
     * @throws EmailConnectionException
     */
    public static String generateAccessToken(ConnectionConfiguration connectionConfiguration)
            throws EmailConnectionException {
        OAuthHandler oAuthHandler =
                getOAuthHandler(connectionConfiguration.getUsername(), connectionConfiguration.getOAuthConfig());
        if (oAuthHandler != null) {
            return oAuthHandler.generateAccessToken();
        } else {
            throw new EmailConnectionException("An invalid authHandler is returned.");
        }
    }

    /**
     * Check if the access-token of the provided tokenID is expired
     * @param tokenID Token ID
     * @return if the access-token is expired or not
     */
    public static boolean checkIfTokenExpired(String tokenID) {
        log.debug("Checking if the token is expired");
        TokenCache tokenCache = TokenCache.getInstance();
        Token token = tokenCache.getTokenObject(tokenID);
        if (null != token) {
            return isAccessTokenExpired(token.getExpiryTime());
        }
        return false;
    }

    private static boolean isAccessTokenExpired(Long expiryTime) {
        return (System.currentTimeMillis() >= expiryTime);
    }

    private static OAuthHandler getOAuthHandler(String username, OAuthConfig oAuthConfig) throws EmailConnectionException {
        String grantType = oAuthConfig.getGrantType().toLowerCase();
        switch (grantType) {
            case OAuthConstants.AUTHORIZATION_CODE_GRANT_TYPE :
                return getAuthorizationCodeHandler(username, oAuthConfig);
            case OAuthConstants.CLIENT_CREDENTIALS_GRANT_TYPE:
                return getClientCredentialsHandler(username, oAuthConfig);
            default:
                throw new EmailConnectionException("Grant type " + grantType + " is invalid.");
        }
    }

    private static OAuthHandler getClientCredentialsHandler(String userName, OAuthConfig oAuthConfig)
            throws EmailConnectionException {
        String clientId = oAuthConfig.getClientId();
        String clientSecret = oAuthConfig.getClientSecret();
        String tokenUrl = oAuthConfig.getTokenUrl();
        String scope = oAuthConfig.getScope();
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret) || StringUtils.isBlank(scope) ||
                StringUtils.isBlank(tokenUrl)) {
            throw new EmailConnectionException("Invalid configurations provided for client credentials grant type.");
        }
        return new ClientCredentialsHandler(userName, clientId, clientSecret, scope, tokenUrl,
                oAuthConfig.getTokenId());
    }

    private static OAuthHandler getAuthorizationCodeHandler(String userName, OAuthConfig oAuthConfig)
            throws EmailConnectionException {
        String clientId = oAuthConfig.getClientId();
        String clientSecret = oAuthConfig.getClientSecret();
        String refreshToken = oAuthConfig.getRefreshToken();
        String tokenUrl = oAuthConfig.getTokenUrl();
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret) ||
                StringUtils.isBlank(refreshToken) || StringUtils.isBlank(tokenUrl)) {
            throw new EmailConnectionException("Invalid configurations provided for authorization code grant type.");
        }
        return new AuthorizationCodeHandler(userName, clientId, clientSecret, refreshToken, tokenUrl,
                oAuthConfig.getTokenId());
    }
}
