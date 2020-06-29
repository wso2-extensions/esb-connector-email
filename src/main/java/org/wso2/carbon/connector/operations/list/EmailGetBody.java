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

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.pojo.EmailMessage;
import org.wso2.carbon.connector.utils.EmailConstants;
import org.wso2.carbon.connector.utils.EmailUtils;
import org.wso2.carbon.connector.utils.ResponseConstants;
import org.wso2.carbon.connector.utils.Error;

import java.util.List;

import static java.lang.String.format;

/**
 * Retrieves an email body
 */
public class EmailGetBody extends AbstractConnector {

    private static final String ERROR = "Error occurred while retrieving email body.";

    @Override
    public void connect(MessageContext messageContext) {

        String emailIndex = (String) getParameter(messageContext, EmailConstants.EMAIL_INDEX);
        List<EmailMessage> emailMessages = (List<EmailMessage>) messageContext
                .getProperty(ResponseConstants.PROPERTY_EMAILS);

        try {
            if (emailIndex != null && emailMessages != null) {
                EmailMessage emailMessage = EmailUtils.getEmail(emailMessages, emailIndex);
                if (log.isDebugEnabled()) {
                    log.debug(format("Retrieving email body for email at index %s...", emailIndex));
                }
                setProperties(messageContext, emailMessage);
            } else if (emailIndex == null) {
                EmailUtils.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
                handleException(format("%s Email Index is not set.", ERROR), messageContext);
            } else {
                EmailUtils.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
                handleException(format("%s No emails retrieved. " +
                        "Email list operation must be invoked first to retrieve emails.", ERROR), messageContext);
            }
        } catch (InvalidConfigurationException e) {
            EmailUtils.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
            handleException(ERROR, e, messageContext);
        }
    }

    /**
     * Sets email message content in Message Context
     *
     * @param messageContext Message Context
     * @param emailMessage   Email
     */
    private void setProperties(MessageContext messageContext, EmailMessage emailMessage) {

        messageContext.setProperty(ResponseConstants.PROPERTY_EMAIL_ID, emailMessage.getEmailId());
        messageContext.setProperty(ResponseConstants.PROPERTY_EMAIL_TO, emailMessage.getTo());
        messageContext.setProperty(ResponseConstants.PROPERTY_EMAIL_FROM, emailMessage.getFrom());
        messageContext.setProperty(ResponseConstants.PROPERTY_EMAIL_CC, emailMessage.getCc());
        messageContext.setProperty(ResponseConstants.PROPERTY_EMAIL_BCC, emailMessage.getBcc());
        messageContext.setProperty(ResponseConstants.PROPERTY_EMAIL_SUBJECT, emailMessage.getSubject());
        messageContext.setProperty(ResponseConstants.PROPERTY_EMAIL_REPLY_TO, emailMessage.getReplyTo());
        messageContext.setProperty(ResponseConstants.PROPERTY_HTML_CONTENT, emailMessage.getHtmlContent());
        messageContext.setProperty(ResponseConstants.PROPERTY_TEXT_CONTENT, emailMessage.getTextContent());
    }
}
