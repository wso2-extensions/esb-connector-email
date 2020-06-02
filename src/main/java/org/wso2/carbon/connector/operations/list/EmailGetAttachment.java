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
import org.wso2.carbon.connector.exception.ContentBuilderException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.pojo.Attachment;
import org.wso2.carbon.connector.pojo.EmailMessage;
import org.wso2.carbon.connector.utils.ContentBuilder;
import org.wso2.carbon.connector.utils.EmailConstants;
import org.wso2.carbon.connector.utils.EmailOperationUtils;
import org.wso2.carbon.connector.utils.EmailPropertyNames;
import org.wso2.carbon.connector.utils.Error;
import org.wso2.carbon.connector.utils.ResponseHandler;

import java.util.List;

import static java.lang.String.format;

/**
 * Retrieves an email attachment
 */
public class EmailGetAttachment extends AbstractConnector {

    private static final String ERROR = "Error occurred while retrieving attachment.";

    @Override
    public void connect(MessageContext messageContext) {

        String emailIndex = (String) getParameter(messageContext, EmailConstants.EMAIL_INDEX);
        String attachmentIndex = (String) getParameter(messageContext, EmailConstants.ATTACHMENT_INDEX);
        List<EmailMessage> emailMessages = (List<EmailMessage>) messageContext
                .getProperty(EmailPropertyNames.PROPERTY_EMAILS);

        if (emailIndex != null && attachmentIndex != null && emailMessages != null) {
            setAttachment(messageContext, emailIndex, attachmentIndex, emailMessages);
        } else if (emailIndex == null) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
            handleException(format("%s Email Index is not set.", ERROR), messageContext);
        } else if (attachmentIndex == null) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
            handleException(format("%s Attachment Index is not set.", ERROR), messageContext);
        } else {
            ResponseHandler.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
            handleException(format("%s No emails retrieved. " +
                    "Email list operation must be invoked first to retrieve emails.", ERROR), messageContext);
        }
    }

    /**
     * Set attachment in body
     *
     * @param messageContext  Message Context
     * @param emailIndex      Email Index
     * @param attachmentIndex Attachment Indec
     * @param emailMessages   List of emails
     */
    private void setAttachment(MessageContext messageContext, String emailIndex, String attachmentIndex,
                               List<EmailMessage> emailMessages) {

        if (log.isDebugEnabled()) {
            log.debug(format("Retrieving email attachment for email at index %s and attachment at index %s...",
                    emailIndex, attachmentIndex));
        }
        try {
            EmailMessage emailMessage = EmailOperationUtils.getEmail(emailMessages, emailIndex);
            if (emailMessage != null) {
                Attachment attachment = EmailOperationUtils.getEmailAttachment(emailMessage, attachmentIndex);
                if (attachment != null) {
                    setProperties(messageContext, attachment);
                    ContentBuilder.buildContent(messageContext, attachment.getContent(),
                            attachment.getContentType());
                }
            }
        } catch (InvalidConfigurationException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
            handleException(ERROR + " " + e.getMessage(), e, messageContext);
        } catch (ContentBuilderException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.RESPONSE_GENERATION);
            handleException("Error occurred during setting attachment content.", e, messageContext);
        }
    }

    /**
     * Sets attachment properties in Message Context
     *
     * @param messageContext Message Context
     * @param attachment     Attachment
     */
    private void setProperties(MessageContext messageContext, Attachment attachment) {

        messageContext.setProperty(EmailPropertyNames.PROPERTY_ATTACHMENT_TYPE, attachment.getContentType());
        messageContext.setProperty(EmailPropertyNames.PROPERTY_ATTACHMENT_NAME, attachment.getName());
    }
}
