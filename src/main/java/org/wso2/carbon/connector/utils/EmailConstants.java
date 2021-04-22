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
 * Contains constants used for email operations
 */
public final class EmailConstants {

    public static final String CONNECTOR_NAME = "email";

    // Template Parameters
    public static final String NAME = "name";
    public static final String TO = "to";
    public static final String FROM = "from";
    public static final String CC = "cc";
    public static final String BCC = "bcc";
    public static final String REPLY_TO = "replyTo";
    public static final String SUBJECT = "subject";
    public static final String CONTENT = "content";
    public static final String CONTENT_TYPE = "contentType";
    public static final String ENCODING = "encoding";
    public static final String ATTACHMENTS = "attachments";
    public static final String CONTENT_TRANSFER_ENCODING = "contentTransferEncoding";
    public static final String CONNECTION_TYPE = "connectionType";
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
    public static final String REQUIRE_AUTHENTICATION = "requireAuthentication";
    // Default email configuration values
    public static final String DEFAULT_SOCKETFACTORY_FALLBACK = "false";
    public static final String DEFAULT_FOLDER = "INBOX";
    public static final int DEFAULT_OFFSET = 0;
    public static final int DEFAULT_LIMIT = -1;
    // Flags
    public static final String FLAG_SEEN = "seen";
    public static final String FLAG_ANSWERED = "answered";
    public static final String FLAG_DELETED = "deleted";
    public static final String FLAG_RECENT = "recent";

    private EmailConstants() {

    }

}
