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
package org.wso2.carbon.connector.operations.list;

import com.google.gson.JsonObject;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.exception.ContentBuilderException;
import org.wso2.carbon.connector.core.util.PayloadUtils;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.pojo.Attachment;
import org.wso2.carbon.connector.pojo.EmailMessage;
import org.wso2.carbon.connector.utils.AbstractEmailConnectorOperation;
import org.wso2.carbon.connector.utils.EmailConstants;
import org.wso2.carbon.connector.utils.EmailUtils;
import org.wso2.carbon.connector.utils.Error;
import org.wso2.carbon.connector.utils.ResponseConstants;

import java.util.List;

import static java.lang.String.format;

/**
 * Retrieves an email attachment
 */
public class EmailGetAttachment extends AbstractEmailConnectorOperation {

    private static final String ERROR = "Error occurred while retrieving attachment.";

    @Override
    public void execute(MessageContext messageContext, String responseVariable, 
                        Boolean overwriteBody) throws ConnectException {

        String emailIndex = (String) getParameter(messageContext, EmailConstants.EMAIL_INDEX);
        String attachmentIndex = (String) getParameter(messageContext, EmailConstants.ATTACHMENT_INDEX);
        List<EmailMessage> emailMessages = (List<EmailMessage>) messageContext
                .getProperty(ResponseConstants.PROPERTY_EMAILS);

        if (emailIndex != null && attachmentIndex != null && emailMessages != null) {
            setAttachment(messageContext, emailIndex, attachmentIndex, emailMessages, responseVariable, overwriteBody);
        } else if (emailIndex == null) {
            JsonObject resultJSON = generateOperationResult(messageContext, false, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(format("%s Email Index is not set.", ERROR), messageContext);
        } else if (attachmentIndex == null) {
            JsonObject resultJSON = generateOperationResult(messageContext, false, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(format("%s Attachment Index is not set.", ERROR), messageContext);
        } else {
            JsonObject resultJSON = generateOperationResult(messageContext, false, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(format("%s No emails retrieved. " +
                    "Email list operation must be invoked first to retrieve emails.", ERROR), messageContext);
        }
    }

    /**
     * Set attachment in body
     *
     * @param messageContext  Message Context
     * @param emailIndex      Email Index
     * @param attachmentIndex Attachment Index
     * @param emailMessages   List of emails
     * @param responseVariable The variable to store the response
     * @param overwriteBody   Whether to overwrite the body
     */
    private void setAttachment(MessageContext messageContext, String emailIndex, String attachmentIndex,
                               List<EmailMessage> emailMessages, String responseVariable, Boolean overwriteBody) {

        if (log.isDebugEnabled()) {
            log.debug(format("Retrieving email attachment for email at index %s and attachment at index %s...",
                    emailIndex, attachmentIndex));
        }
        try {
            EmailMessage emailMessage = EmailUtils.getEmail(emailMessages, emailIndex);
            Attachment attachment = EmailUtils.getEmailAttachment(emailMessage, attachmentIndex);
            
            // Create result JSON with success status
            JsonObject resultJSON = new JsonObject();
            JsonObject attachmentInfo = new JsonObject();

            // Convert attachment content (InputStream) to Base64 string for JSON
            String base64Content = EmailUtils.convertInputStreamToBase64(attachment.getContent());
            
            attachmentInfo.addProperty("name", attachment.getName());
            attachmentInfo.addProperty("contentType", attachment.getContentType());
            attachmentInfo.addProperty("content", base64Content);

            resultJSON.add("attachment", attachmentInfo);
            
            // Handle response for the operation status
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
        } catch (InvalidConfigurationException e) {
            JsonObject resultJSON = generateErrorResult(messageContext, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(ERROR, e, messageContext);
        } catch (ContentBuilderException e) {
            JsonObject resultJSON = generateErrorResult(messageContext, Error.RESPONSE_GENERATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException("Error occurred during setting attachment content.", e, messageContext);
        }
    }
}
