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
package org.wso2.carbon.connector.operations;

import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.connection.EmailConnection;
import org.wso2.carbon.connector.connection.EmailConnectionManager;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.exception.ContentBuilderException;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.utils.ConfigurationUtils;
import org.wso2.carbon.connector.utils.EmailConstants;
import org.wso2.carbon.connector.utils.Error;
import org.wso2.carbon.connector.utils.MessageBuilder;
import org.wso2.carbon.connector.utils.ResponseHandler;

import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import static java.lang.String.format;

/**
 * Sends an email
 */
public class EmailSend extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) {

        try {
            String name = ConfigurationUtils.getConnectionName(messageContext);
            EmailConnection connection = EmailConnectionManager.getEmailConnectionManager().getConnection(name);
            sendMessage(messageContext, connection);
            ResponseHandler.generateOutput(messageContext, true);
        } catch (EmailConnectionException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.CONNECTIVITY);
            handleException(e.getMessage(), e, messageContext);
        } catch (InvalidConfigurationException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
            handleException(e.getMessage(), e, messageContext);
        } catch (ContentBuilderException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.RESPONSE_GENERATION);
            handleException(e.getMessage(), e, messageContext);
        }
    }

    /**
     * Sends an email
     *
     * @param messageContext The message context that is generated for sending the email
     * @param session        Mail Session to be used
     * @return a result status indicating whether the email send is successful or not
     */
    private void sendMessage(MessageContext messageContext, EmailConnection session) throws EmailConnectionException,
            InvalidConfigurationException {

        String to = (String) getParameter(messageContext, EmailConstants.TO);
        String from = (String) getParameter(messageContext, EmailConstants.FROM);
        String cc = (String) getParameter(messageContext, EmailConstants.CC);
        String bcc = (String) getParameter(messageContext, EmailConstants.BCC);
        String replyTo = (String) getParameter(messageContext, EmailConstants.REPLY_TO);
        String subject = (String) getParameter(messageContext, EmailConstants.SUBJECT);
        String content = (String) getParameter(messageContext, EmailConstants.CONTENT);
        String attachments = (String) getParameter(messageContext, EmailConstants.ATTACHMENTS);
        String contentType = (String) getParameter(messageContext, EmailConstants.CONTENT_TYPE);
        String encoding = (String) getParameter(messageContext, EmailConstants.ENCODING);
        String contentTransferEncoding = (String) getParameter(messageContext, EmailConstants.CONTENT_TRANSFER_ENCODING);

        if (StringUtils.isEmpty(to) && StringUtils.isEmpty(cc) && StringUtils.isEmpty(bcc)) {
            throw new InvalidConfigurationException("Error occurred while sending the email. " +
                    "Recipients are not provided.");
        } else {
            try {
                //TODO: Set headers from transport properties
                MimeMessage message = MessageBuilder.newMessage(session.getSession())
                        .to(to)
                        .fromAddresses(from)
                        .cc(cc)
                        .bcc(bcc)
                        .replyTo(replyTo)
                        .withSubject(subject)
                        .withBody(content, contentType, encoding, contentTransferEncoding)
                        .withAttachments(attachments)
                        .build();
                Transport.send(message);
                log.debug("Email was sent successfully...");
            } catch (MessagingException e) {
                throw new EmailConnectionException(format("Error occurred while sending the email. %s", e.getMessage()),
                        e);
            } catch (IOException e) {
                throw new EmailConnectionException(format("Error occurred while adding attachments to the email. %s"
                        , e.getMessage()), e);
            }
        }
    }
}
