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
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.pojo.EmailMessage;
import org.wso2.carbon.connector.utils.AbstractEmailConnectorOperation;
import org.wso2.carbon.connector.utils.EmailConstants;
import org.wso2.carbon.connector.utils.EmailUtils;
import org.wso2.carbon.connector.utils.ResponseConstants;
import org.wso2.carbon.connector.utils.Error;

import java.util.List;

import static java.lang.String.format;

/**
 * Retrieves an email body
 */
public class EmailGetBody extends AbstractEmailConnectorOperation {

    private static final String ERROR = "Error occurred while retrieving email body.";

    @Override
    public void execute(MessageContext messageContext, String responseVariable, 
                        Boolean overwriteBody) throws ConnectException {

        String emailIndex = (String) getParameter(messageContext, EmailConstants.EMAIL_INDEX);
        @SuppressWarnings("unchecked")
        List<EmailMessage> emailMessages = (List<EmailMessage>) messageContext
                .getProperty(ResponseConstants.PROPERTY_EMAILS);

        try {
            if (emailIndex != null && emailMessages != null) {
                EmailMessage emailMessage = EmailUtils.getEmail(emailMessages, emailIndex);
                if (log.isDebugEnabled()) {
                    log.debug(format("Retrieving email body for email at index %s...", emailIndex));
                }
                
                // Create JSON response with email details
                JsonObject resultJSON = new JsonObject();
                JsonObject emailDetails = new JsonObject();
                emailDetails.addProperty("emailID", emailMessage.getEmailId());
                emailDetails.addProperty("to", emailMessage.getTo());
                emailDetails.addProperty("from", emailMessage.getFrom());
                emailDetails.addProperty("cc", emailMessage.getCc());
                emailDetails.addProperty("bcc", emailMessage.getBcc());
                emailDetails.addProperty("subject", emailMessage.getSubject());
                emailDetails.addProperty("replyTo", emailMessage.getReplyTo());
                emailDetails.addProperty("htmlContent", emailMessage.getHtmlContent());
                emailDetails.addProperty("textContent", emailMessage.getTextContent());
                resultJSON.add("email", emailDetails);
                
                handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            } else if (emailIndex == null) {
                JsonObject resultJSON = generateErrorResult(messageContext, Error.INVALID_CONFIGURATION);
                handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
                handleException(format("%s Email Index is not set.", ERROR), messageContext);
            } else {
                JsonObject resultJSON = generateErrorResult(messageContext, Error.INVALID_CONFIGURATION);
                handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
                handleException(format("%s No emails retrieved. " +
                        "Email list operation must be invoked first to retrieve emails.", ERROR), messageContext);
            }
        } catch (InvalidConfigurationException e) {
            JsonObject resultJSON = generateErrorResult(messageContext, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(ERROR, e, messageContext);
        }
    }
}
