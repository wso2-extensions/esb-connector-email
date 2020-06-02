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

import org.apache.commons.mail.util.MimeMessageParser;
import org.wso2.carbon.connector.exception.EmailParsingException;
import org.wso2.carbon.connector.pojo.Attachment;
import org.wso2.carbon.connector.pojo.EmailMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;

import static java.lang.String.format;

/**
 * Parses emails
 */
public final class EmailParser {

    private EmailParser() {

    }

    /**
     * Gets email content and attachments
     *
     * @param messages List of messages to be parsed
     * @return Parsed messages
     * @throws EmailParsingException if failed to parse content
     */
    public static List<EmailMessage> parseMessageList(List<Message> messages) throws EmailParsingException {

        List<EmailMessage> messagesList = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(javax.mail.Message.class.getClassLoader());
            for (Message message : messages) {
                messagesList.add(parseMessage((MimeMessage) message));
            }
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return messagesList;
    }

    /**
     * Parses a message and obtains the email content
     *
     * @param message Message to be parsed
     * @throws EmailParsingException if failed to parse the message
     */
    private static EmailMessage parseMessage(MimeMessage message)
            throws EmailParsingException {

        MimeMessageParser parser = new MimeMessageParser(message);
        EmailMessage emailMessage = new EmailMessage();
        try {
            parser.parse();
            emailMessage.setTextContent(parser.getPlainContent());
            emailMessage.setHtmlContent(parser.getHtmlContent());
            emailMessage.setAttachments(getAttachmentList(parser.getAttachmentList()));
            emailMessage.setEmailId(parser.getMimeMessage().getMessageID());
            emailMessage.setTo(parser.getTo());
            emailMessage.setFrom(parser.getFrom());
            emailMessage.setCc(parser.getCc());
            emailMessage.setBcc(parser.getBcc());
            emailMessage.setSubject(parser.getSubject());
            emailMessage.setReplyTo(parser.getReplyTo());
            return emailMessage;
        } catch (Exception e) {
            throw new EmailParsingException(format("Error occurred while retrieving message data. %s ",
                    e.getMessage()), e);
        }
    }

    /**
     * Parse attachments
     *
     * @param dataSources List of attachments
     * @return Parsed list of attachments
     * @throws IOException if failed to parse content
     */
    private static List<Attachment> getAttachmentList(List<DataSource> dataSources) throws IOException {

        List<Attachment> attachments = new ArrayList<>();
        for (DataSource dataSource : dataSources) {
            Attachment attachment = new Attachment();
            attachment.setName(dataSource.getName());
            attachment.setContentType(dataSource.getContentType());
            attachment.setContent(dataSource.getInputStream());
            attachments.add(attachment);
        }
        return attachments;
    }

}
