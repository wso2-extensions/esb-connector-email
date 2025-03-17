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
 * Contains the constants used to set the response
 */
public final class ResponseConstants {

    public static final String RESPONSE_CONTENT_TYPE = "application/xml";

    // Properties set in the message context which contains data returned by the operation
    public static final String PROPERTY_EMAILS = "PROPERTY_EMAILS";
    public static final String PROPERTY_ATTACHMENT_TYPE = "ATTACHMENT_TYPE";
    public static final String PROPERTY_ATTACHMENT_NAME = "ATTACHMENT_NAME";
    public static final String PROPERTY_HTML_CONTENT = "HTML_CONTENT";
    public static final String PROPERTY_TEXT_CONTENT = "TEXT_CONTENT";
    public static final String PROPERTY_EMAIL_ID = "EMAIL_ID";
    public static final String PROPERTY_EMAIL_TO = "TO";
    public static final String PROPERTY_EMAIL_FROM = "FROM";
    public static final String PROPERTY_EMAIL_SUBJECT = "SUBJECT";
    public static final String PROPERTY_EMAIL_CC = "CC";
    public static final String PROPERTY_EMAIL_BCC = "BCC";
    public static final String PROPERTY_EMAIL_REPLY_TO = "REPLY_TO";
    public static final String PROPERTY_ERROR_CODE = "ERROR_CODE";
    public static final String PROPERTY_ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String RESPONSE_VARIABLE = "responseVariable";
    public static final String OVERWRITE_BODY = "overwriteBody";
    public final static String JSON_CONTENT_TYPE = "application/json";
    public static final String STATUS_CODE = "HTTP_SC";
    public static final Object HTTP_STATUS_500 = "500";
    private ResponseConstants() {

    }
}
