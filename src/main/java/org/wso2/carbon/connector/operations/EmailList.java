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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.connection.MailBoxConnection;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.connection.EmailConnectionHandler;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.exception.EmailParsingException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.pojo.Attachment;
import org.wso2.carbon.connector.pojo.EmailMessage;
import org.wso2.carbon.connector.pojo.MailboxConfiguration;
import org.wso2.carbon.connector.utils.AbstractEmailConnectorOperation;
import org.wso2.carbon.connector.utils.EmailConstants;
import org.wso2.carbon.connector.utils.EmailUtils;
import org.wso2.carbon.connector.utils.Error;
import org.wso2.carbon.connector.utils.ResponseConstants;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import static java.lang.String.format;
import static java.util.Date.from;

/**
 * Lists emails
 */
public class EmailList extends AbstractEmailConnectorOperation {

    @Override
    public void execute(MessageContext messageContext, String responseVariable,
                        Boolean overwriteBody) throws ConnectException {

        String errorString = "Error occurred while retrieving messages from folder: %s.";
        String connectionName = null;
        String folderName = StringUtils.EMPTY;
        EmailConnectionHandler handler = EmailConnectionHandler.getConnectionHandler();
        MailBoxConnection connection = null;
        try {
            connectionName = EmailUtils.getConnectionName(messageContext);
            connection = (MailBoxConnection) handler.getConnection(connectionName);
            MailboxConfiguration mailboxConfiguration = getMailboxConfigFromContext(messageContext);
            folderName = mailboxConfiguration.getFolder();
            List<EmailMessage> messageList = retrieveMessages(connection, mailboxConfiguration);
            messageContext.setProperty(ResponseConstants.PROPERTY_EMAILS, messageList);
            JsonObject resultJSON = new JsonObject();
            JsonArray emailsArray = createEmailJsonArray(messageList);
            resultJSON.add("emails", emailsArray);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            
        } catch (EmailConnectionException e) {
            JsonObject resultJSON = generateErrorResult(messageContext, Error.CONNECTIVITY);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(format(errorString, folderName), e, messageContext);
        } catch (InvalidConfigurationException e) {
            JsonObject resultJSON = generateErrorResult(messageContext, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(format(errorString, folderName), e, messageContext);
        } catch (EmailParsingException e) {
            JsonObject resultJSON = generateErrorResult(messageContext, Error.RESPONSE_GENERATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(format(errorString, folderName), e, messageContext);
        } finally {
            if (connection != null) {
                handler.returnConnection(connectionName, connection);
            }
        }
    }

    /**
     * Creates a JSON array of email messages
     *
     * @param emailMessages List of emails
     * @return JSON array of email information
     */
    private JsonArray createEmailJsonArray(List<EmailMessage> emailMessages) {
        JsonArray emailsArray = new JsonArray();
        
        for (int i = 0; i < emailMessages.size(); i++) {
            EmailMessage emailMessage = emailMessages.get(i);
            JsonObject emailObject = new JsonObject();
            
            emailObject.addProperty("index", i);
            emailObject.addProperty("emailID", emailMessage.getEmailId());
            emailObject.addProperty("to", emailMessage.getTo());
            emailObject.addProperty("from", emailMessage.getFrom());
            emailObject.addProperty("cc", emailMessage.getCc());
            emailObject.addProperty("bcc", emailMessage.getBcc());
            emailObject.addProperty("replyTo", emailMessage.getReplyTo());
            emailObject.addProperty("subject", emailMessage.getSubject());
            
            if (emailMessage.getAttachments() != null && !emailMessage.getAttachments().isEmpty()) {
                JsonArray attachmentsArray = new JsonArray();
                for (int j = 0; j < emailMessage.getAttachments().size(); j++) {
                    Attachment attachment = emailMessage.getAttachments().get(j);
                    JsonObject attachmentObject = new JsonObject();
                    attachmentObject.addProperty("index", j);
                    attachmentObject.addProperty("name", attachment.getName());
                    attachmentObject.addProperty("contentType", attachment.getContentType());
                    attachmentsArray.add(attachmentObject);
                }
                emailObject.add("attachments", attachmentsArray);
            }
            
            emailsArray.add(emailObject);
        }
        
        return emailsArray;
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
                messagesList.add(new EmailMessage((MimeMessage) message));
            }
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return messagesList;
    }

    /**
     * Retrieves messages that matches given filtering criteria
     *
     * @param connection           Mailbox connection to be used
     * @param mailboxConfiguration Mailbox Configurations
     */
    private List<EmailMessage> retrieveMessages(MailBoxConnection connection, MailboxConfiguration mailboxConfiguration)
            throws EmailConnectionException, EmailParsingException {

        List<EmailMessage> messageList;
        boolean deleteAfterRetrieval = mailboxConfiguration.getDeleteAfterRetrieve();
        try {
            String folderName = mailboxConfiguration.getFolder();
            Folder mailbox;
            if (deleteAfterRetrieval) {
                mailbox = connection.getFolder(folderName, Folder.READ_WRITE);
            } else {
                mailbox = connection.getFolder(folderName, Folder.READ_ONLY);
            }
            if (log.isDebugEnabled()) {
                log.debug(format("Retrieving messages from Mail folder: %s ...", folderName));
            }
            SearchTerm searchTerm = getSearchTerm(mailboxConfiguration, mailbox);
            Message[] messages;
            if (searchTerm != null) {
                messages = mailbox.search(getSearchTerm(mailboxConfiguration, mailbox));
            } else {
                messages = mailbox.getMessages();
            }
            messageList = parseMessageList(getPaginatedMessages(messages,
                    mailboxConfiguration.getOffset(), mailboxConfiguration.getLimit(), deleteAfterRetrieval));
        } catch (MessagingException e) {
            throw new EmailConnectionException("Error occurred when searching emails. %s", e);
        } finally {
            connection.closeFolder(deleteAfterRetrieval);
        }
        return messageList;
    }

    /**
     * Retrieves paginated messages
     *
     * @param messages Messages to filter
     * @param offset   Record index to start filtering from
     * @param limit    Number of emails to be retrieved
     * @return List of paginated messages
     */
    private List<Message> getPaginatedMessages(Message[] messages, int offset, int limit, boolean deleteAfterRetrieval) {

        List<Message> messageList = Arrays.asList(messages);
        int toIndex;
        if (limit != -1) {
            toIndex = offset + limit;
            if (toIndex > messageList.size()) {
                toIndex = messageList.size();
            }
            if (log.isDebugEnabled()) {
                log.debug(format("Retrieving messages from index %d to %d ...", offset, toIndex));
            }
            if (messageList.size() >= offset) {
                messageList = messageList.subList(offset, toIndex);
            }
            if (log.isDebugEnabled()) {
                log.debug(format("Retrieved %d message(s)...", messageList.size()));
            }
        } else {
            toIndex = messages.length;
        }
        if (deleteAfterRetrieval) {
            markMessagesAsDeleted(messages, offset, toIndex);
        }
        return messageList;
    }

    /**
     * Marks emails within a range as deleted
     *
     * @param messages List of messages
     * @param from     Start index
     * @param to       End index
     */
    private void markMessagesAsDeleted(Message[] messages, int from, int to) {

        if (log.isDebugEnabled()) {
            log.debug(format("Marking messages from %d to %d as deleted...", from, to));
        }
        for (int i = from; i < to; i++) {
            try {
                messages[i].setFlag(Flags.Flag.DELETED, true);
            } catch (MessagingException e) {
                log.error("Failed to mark message as deleted.", e);
            }
        }
    }

    /**
     * Builds search term
     *
     * @param mailboxConfiguration Configured parameters containing the filters
     * @return {@link SearchTerm} containing all the filters
     * @throws EmailConnectionException if failed to parse email address
     */
    private SearchTerm getSearchTerm(MailboxConfiguration mailboxConfiguration, Folder folder) throws EmailConnectionException {

        SearchTerm searchTerm = null;

        // Some servers may not support flags. Therefore, we will only add the search terms if the servers support the
        // relevant flags
        if (folder.getPermanentFlags().getSystemFlags().length != 0) {
            FlagTerm seenFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), mailboxConfiguration.getSeen());
            FlagTerm answeredFlagTerm = new FlagTerm(new Flags(Flags.Flag.ANSWERED), mailboxConfiguration.getAnswered());
            FlagTerm recentFlagTerm = new FlagTerm(new Flags(Flags.Flag.RECENT), mailboxConfiguration.getRecent());
            FlagTerm deletedFlagTerm = new FlagTerm(new Flags(Flags.Flag.DELETED), mailboxConfiguration.getDeleted());

            searchTerm = new AndTerm(deletedFlagTerm, new AndTerm(seenFlagTerm, new AndTerm(answeredFlagTerm,
                    recentFlagTerm)));
        }

        String subjectRegex = mailboxConfiguration.getSubjectRegex();
        if (StringUtils.isNotEmpty(subjectRegex)) {
            SubjectTerm subjectTerm = new SubjectTerm(subjectRegex);
            searchTerm = addSearchTerm(searchTerm, subjectTerm);
        }

        String fromRegex = mailboxConfiguration.getFromRegex();
        if (StringUtils.isNotEmpty(fromRegex)) {
            try {
                FromTerm fromTerm = new FromTerm(new InternetAddress(fromRegex));
                searchTerm = addSearchTerm(searchTerm, fromTerm);
            } catch (AddressException e) {
                throw new EmailConnectionException("Error occurred when parsing 'from' email address. %s");
            }
        }

        String receivedSince = mailboxConfiguration.getReceivedSince();
        if (receivedSince != null) {
            LocalDateTime date = LocalDateTime.parse(receivedSince);
            ReceivedDateTerm receivedSinceDateTerm = new ReceivedDateTerm(ComparisonTerm.GT,
                    from(date.atZone(ZoneId.systemDefault()).toInstant()));
            searchTerm = addSearchTerm(searchTerm, receivedSinceDateTerm);
        }

        String receivedUntil = mailboxConfiguration.getReceivedUntil();
        if (receivedUntil != null) {
            LocalDateTime date = LocalDateTime.parse(receivedUntil);
            ReceivedDateTerm receivedUntilDateTerm = new ReceivedDateTerm(ComparisonTerm.LT,
                    from(date.atZone(ZoneId.systemDefault()).toInstant()));
            searchTerm = addSearchTerm(searchTerm, receivedUntilDateTerm);
        }

        String sentSince = mailboxConfiguration.getSentSince();
        if (sentSince != null) {
            LocalDateTime date = LocalDateTime.parse(sentSince);
            ReceivedDateTerm sentSinceDateTerm = new ReceivedDateTerm(ComparisonTerm.GT,
                    from(date.atZone(ZoneId.systemDefault()).toInstant()));
            searchTerm = addSearchTerm(searchTerm, sentSinceDateTerm);
        }

        String sentUntil = mailboxConfiguration.getSentUntil();
        if (sentUntil != null) {
            LocalDateTime date = LocalDateTime.parse(sentUntil);
            ReceivedDateTerm sentUntilDateTerm = new ReceivedDateTerm(ComparisonTerm.LT,
                    from(date.atZone(ZoneId.systemDefault()).toInstant()));
            searchTerm = addSearchTerm(searchTerm, sentUntilDateTerm);
        }
        return searchTerm;
    }

    /**
     * Aggregate search terms
     *
     * @param term1 Search Term 1
     * @param term2 Search Term 2
     * @return aggregated search term
     */
    private SearchTerm addSearchTerm(SearchTerm term1, SearchTerm term2) {

        if (term1 == null) {
            term1 = term2;
        } else {
            term1 = new AndTerm(term1, term2);
        }
        return term1;
    }

    /**
     * Extracts mailbox connection configurations from operation template
     *
     * @param messageContext Message Context from which the parameters should be extracted from
     * @return Mailbox Configurations set
     */
    private MailboxConfiguration getMailboxConfigFromContext(MessageContext messageContext) {

        String folder = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.FOLDER);
        String deleteAfterRetrieve = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.DELETE_AFTER_RETRIEVE);
        String seen = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.FLAG_SEEN);
        String answered = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.FLAG_ANSWERED);
        String recent = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.FLAG_RECENT);
        String deleted = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.FLAG_DELETED);
        String receivedSince = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.RECEIVED_SINCE);
        String receivedUntil = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.RECEIVED_UNTIL);
        String sentSince = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.SENT_SINCE);
        String sentUntil = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.SENT_UNTIL);
        String subjectRegex = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.SUBJECT_REGEX);
        String fromRegex = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.FROM_REGEX);
        String offset = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.OFFSET);
        String limit = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.LIMIT);

        if (StringUtils.isEmpty(folder)) {
            folder = EmailConstants.DEFAULT_FOLDER;
        }

        boolean seenFlag = true;
        if (seen != null) {
            seenFlag = Boolean.parseBoolean(seen);
        }

        boolean answeredFlag = true;
        if (answered != null) {
            answeredFlag = Boolean.parseBoolean(answered);
        }

        boolean recentFlag = true;
        if (recent != null) {
            recentFlag = Boolean.parseBoolean(recent);
        }

        boolean deletedFlag = true;
        if (deleted != null) {
            deletedFlag = Boolean.parseBoolean(deleted);
        }

        int offSetValue = EmailConstants.DEFAULT_OFFSET;
        if (offset != null) {
            offSetValue = Integer.parseInt(offset);
        }

        int limitValue = EmailConstants.DEFAULT_LIMIT;
        if (limit != null) {
            limitValue = Integer.parseInt(limit);
        }

        MailboxConfiguration mailboxConfiguration = new MailboxConfiguration();
        mailboxConfiguration.setFolder(folder);
        mailboxConfiguration.setDeleteAfterRetrieve(Boolean.parseBoolean(deleteAfterRetrieve));
        mailboxConfiguration.setSeen(seenFlag);
        mailboxConfiguration.setAnswered(answeredFlag);
        mailboxConfiguration.setRecent(recentFlag);
        mailboxConfiguration.setDeleted(deletedFlag);
        mailboxConfiguration.setReceivedSince(receivedSince);
        mailboxConfiguration.setReceivedUntil(receivedUntil);
        mailboxConfiguration.setSentSince(sentSince);
        mailboxConfiguration.setSentUntil(sentUntil);
        mailboxConfiguration.setSubjectRegex(subjectRegex);
        mailboxConfiguration.setFromRegex(fromRegex);
        mailboxConfiguration.setOffset(offSetValue);
        mailboxConfiguration.setLimit(limitValue);

        return mailboxConfiguration;
    }
}
