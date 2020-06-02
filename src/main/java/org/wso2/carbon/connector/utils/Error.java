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
 * Contains the error codes and details
 */
public enum Error {

    EMAIL_NOT_FOUND("700201", "EMAIL:EMAIL_NOT_FOUND"),
    ACCESSING_FOLDER("700202", "EMAIL:ACCESSING_FOLDER"),
    CONNECTIVITY("700203", "EMAIL:CONNECTIVITY"),
    INVALID_CONFIGURATION("700204", "EMAIL:INVALID_CONFIGURATION"),
    RESPONSE_GENERATION("700205", "EMAIL:RESPONSE_GENERATION"),
    INVALID_CREDENTIALS("700206", "EMAIL:INVALID_CREDENTIALS");

    private final String code;
    private final String message;

    /**
     * Creates an Error Code instance.
     *
     * @param code the error code.
     * @param message the error detail.
     */
    Error(String code, String message) {

        this.code = code;
        this.message = message;
    }

    public String getErrorCode() {

        return code;
    }

    public String getErrorDetail() {

        return message;
    }
}
