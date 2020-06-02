/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.connector.utils;

/**
 * Contains the Java Mail API Properties
 */
public final class EmailConfigProperties {

    public static final String PROPERTY_STORE_PROTOCOL = "mail.store.protocol";
    public static final String PROPERTY_HOST = "mail.%s.host";
    public static final String PROPERTY_PORT = "mail.%s.port";
    public static final String PROPERTY_AUTH = "mail.%s.auth";
    public static final String PROPERTY_TLS_ENABLE = "mail.%s.starttls.enable";
    public static final String PROPERTY_SOCKETFACTORY_CLASS = "mail.%s.socketFactory.class";
    public static final String PROPERTY_SOCKETFACTORY_FALLBACK = "mail.%s.socketFactory.fallback";
    public static final String PROPERTY_SOCKETFACTORY_PORT = "mail.%s.socketFactory.port";
    public static final String PROPERTY_CHARSET = "mail.mime.charset";
    public static final String PROPERTY_SOCKET_FACTORY = "mail.%s.ssl.socketFactory";
    public static final String PROPERTY_SSL_CIPHER_SUITES = "mail.%s.ssl.ciphersuites";
    public static final String PROPERTY_SSL_PROTOCOLS = "mail.%s.ssl.protocols";
    public static final String PROPERTY_SSL_ENABLE = "mail.%s.ssl.enable";
    public static final String PROPERTY_SSL_TRUST = "mail.%s.ssl.trust";
    public static final String PROPERTY_START_TLS_ENABLE = "mail.%s.starttls.enable";
    public static final String PROPERTY_TRANSPORT_NAME = "mail.transport.name";
    public static final String PROPERTY_TIMEOUT = "mail.%s.timeout";
    public static final String PROPERTY_CONNECTION_TIMEOUT = "mail.%s.connectiontimeout";
    public static final String PROPERTY_WRITE_TIMEOUT = "mail.%s.writetimeout";
    public static final String PROPERTY_CHECK_SERVER_IDENTITY = "mail.%s.ssl.checkserveridentity";

    private EmailConfigProperties() {

    }
}
