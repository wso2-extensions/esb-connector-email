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

/**
 * Utility class defining constants used by the OAuth handlers
 */
public class OAuthConstants {

    public static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";
    public static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public static final String CLIENT_CRED_GRANT_TYPE = "grant_type=client_credentials";
    public static final String REFRESH_TOKEN_GRANT_TYPE = "grant_type=refresh_token";

    // parameters used to build oauth requests
    public static final String PARAM_CLIENT_ID = "&client_id=";
    public static final String PARAM_CLIENT_SECRET = "&client_secret=";
    public static final String PARAM_REFRESH_TOKEN = "&refresh_token=";
    public static final String SCOPE = "&scope=";

    // elements in the oauth response
    public static final String ACCESS_TOKEN = "access_token";
    public static final String EXPIRES_IN = "expires_in";
}
