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

public final class EmailConstants {

    // Template Parameters
    public static final String NAME = "name";
    public static final String TO = "to";
    public static final String FROM = "from";
    public static final String CC = "cc";
    public static final String BCC = "bcc";
    public static final String REPLY_TO = "replyTo";
    public static final String SUBJECT = "subject";
    public static final String HEADERS = "headers";
    public static final String CONTENT = "content";
    public static final String CONTENT_TYPE = "contentType";
    public static final String ENCODING = "encoding";
    public static final String ATTACHMENTS = "attachments";
    public static final String CONTENT_TRANSFER_ENCODING = "contentTransferEncoding";
    public static final String PROTOCOL = "protocol";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String FOLDER = "folder";
    public static final String EMAIL_ID = "emailID";
    public static final String EMAIL_INDEX = "emailIndex";
    public static final String ATTACHMENT_INDEX = "attachmentIndex";
    public static final String CONNECTION = "connection";
    public static final String DELETE_AFTER_RETRIEVE = "deleteAfterRetrieve";
    public static final String RECEIVED_SINCE = "receivedSince";
    public static final String RECEIVED_UNTIL = "receivedUntil";
    public static final String SENT_SINCE = "sentSince";
    public static final String SENT_UNTIL = "sentUntil";
    public static final String SUBJECT_REGEX = "subjectRegex";
    public static final String FROM_REGEX = "fromRegex";
    public static final String OFFSET = "offset";
    public static final String LIMIT = "limit";
    public static final String READ_TIMEOUT = "readTimeout";
    public static final String WRITE_TIMEOUT = "writeTimeout";
    public static final String CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String REQUIRE_TLS = "requireTLS";
    public static final String CHECK_SERVER_IDENTITY = "checkServerIdentity";
    public static final String TRUSTED_HOSTS = "trustedHosts";
    public static final String SSL_PROTOCOLS = "sslProtocols";
    public static final String CIPHER_SUITES = "cipherSuites";
    public static final String MAX_ACTIVE_CONNECTIONS = "maxActiveConnections";
    public static final String MAX_IDLE_CONNECTIONS = "maxIdleConnections";
    public static final String MAX_WAIT_TIME = "maxWaitTime";
    public static final String MAX_EVICTION_TIME = "minEvictionTime";
    public static final String EVICTION_CHECK_INTERVAL = "evictionCheckInterval";
    public static final String EXHAUSTED_ACTION = "exhaustedAction";
    public static final String INITIALISATION_POLICY = "initialisationPolicy";
    public static final String DISABLE_POOLING = "disablePooling";
    //Java Mail API Constants
    public static final String CONTENT_TRANSFER_ENCODING_HEADER = "Content-Transfer-Encoding";
    // Default configuration values
    public static final String DEFAULT_CONTENT_TYPE = "text/html";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String DEFAULT_CONTENT_TRANSFER_ENCODING = "Base64";
    public static final String DEFAULT_SOCKETFACTORY_CLASS = "javax.net.ssl.SSLSocketFactory";
    public static final String DEFAULT_SOCKETFACTORY_FALLBACK = "false";
    public static final String DEFAULT_FOLDER = "INBOX";
    public static final int DEFAULT_OFFSET = 0;
    public static final int DEFAULT_LIMIT = 10;
    public static final int DEFAULT_MAX_ACTIVE_CONNECTIONS = 4;
    public static final int DEFAULT_MAX_IDLE_CONNECTIONS = 2;
    // Flags
    public static final String FLAG_SEEN = "seen";
    public static final String FLAG_ANSWERED = "answered";
    public static final String FLAG_DELETED = "deleted";
    public static final String FLAG_RECENT = "recent";

    private EmailConstants() {

    }

}
