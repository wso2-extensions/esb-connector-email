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
import org.wso2.carbon.connector.connection.EmailConnectionManager;
import org.wso2.carbon.connector.connection.EmailConnectionPool;
import org.wso2.carbon.connector.connection.MailBoxConnection;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.exception.ContentBuilderException;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.exception.EmailConnectionPoolException;
import org.wso2.carbon.connector.exception.EmailParsingException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.pojo.EmailMessage;
import org.wso2.carbon.connector.pojo.MailboxConfiguration;
import org.wso2.carbon.connector.utils.ConfigurationUtils;
import org.wso2.carbon.connector.utils.EmailParser;
import org.wso2.carbon.connector.utils.EmailPropertyNames;
import org.wso2.carbon.connector.utils.Error;
import org.wso2.carbon.connector.utils.ResponseHandler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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
public class EmailList extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) {

        String errorString = "Error occurred while retrieving messages from folder: %s. %s";
        EmailConnectionPool pool = null;
        MailBoxConnection connection = null;
        String folderName = StringUtils.EMPTY;

        try {
            String connectionName = ConfigurationUtils.getConnectionName(messageContext);
            pool = EmailConnectionManager.getEmailConnectionManager().getConnectionPool(connectionName);
            connection = (MailBoxConnection) pool.borrowObject();
            MailboxConfiguration mailboxConfiguration = ConfigurationUtils.getMailboxConfigFromContext(messageContext);
            folderName = mailboxConfiguration.getFolder();
            List<EmailMessage> messageList = retrieveMessages(connection, mailboxConfiguration);
            messageContext.setProperty(EmailPropertyNames.PROPERTY_EMAILS, messageList);
            ResponseHandler.setEmailListResponse(messageList, messageContext);
        } catch (EmailConnectionException | EmailConnectionPoolException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.CONNECTIVITY);
            handleException(format(errorString, folderName, e.getMessage()), e, messageContext);
        } catch (InvalidConfigurationException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
            handleException(format(errorString, folderName, e.getMessage()), e, messageContext);
        } catch (EmailParsingException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.RESPONSE_GENERATION);
            handleException(format(errorString, folderName, e.getMessage()), e, messageContext);
        } catch (ContentBuilderException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.RESPONSE_GENERATION);
            handleException(format(errorString, folderName), e, messageContext);
        } finally {
            if (pool != null) {
                pool.returnObject(connection);
            }
        }
    }

    /**
     * Retrieves messages that matches given filtering criteria
     *
     * @param connection           Mailbox connection to be used
     * @param mailboxConfiguration Mailbox Configurations
     */
    private List<EmailMessage> retrieveMessages(MailBoxConnection connection, MailboxConfiguration mailboxConfiguration)
            throws EmailConnectionException, EmailParsingException {

        try {
            String folderName = mailboxConfiguration.getFolder();

            Folder mailbox;
            boolean deleteAfterRetrieval = mailboxConfiguration.getDeleteAfterRetrieve();
            if (deleteAfterRetrieval) {
                mailbox = connection.getFolder(folderName, Folder.READ_WRITE);
            } else {
                mailbox = connection.getFolder(folderName, Folder.READ_ONLY);
            }
            if (log.isDebugEnabled()) {
                log.debug(format("Retrieving messages from Mail folder: %s ...", folderName));
            }
            Message[] messages = mailbox.search(getSearchTerm(mailboxConfiguration));
            List<EmailMessage> messageList = EmailParser.parseMessageList(getPaginatedMessages(messages,
                    mailboxConfiguration.getOffset(), mailboxConfiguration.getLimit(), deleteAfterRetrieval));
            connection.closeFolder(deleteAfterRetrieval);
            return messageList;
        } catch (MessagingException e) {
            throw new EmailConnectionException(format("Error occurred when searching emails. %s", e.getMessage()), e);
        }
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
        int toIndex = offset + limit;
        if (toIndex > messageList.size()) {
            toIndex = messageList.size();
        }
        if (log.isDebugEnabled()) {
            log.debug(format("Retrieving messages from index %d to %d ...", offset, toIndex));
        }
        if (messageList.size() >= offset) {
            messageList = messageList.subList(offset, toIndex);
        }
        if (deleteAfterRetrieval) {
            markMessagesAsDeleted(messages, offset, toIndex);
        }
        if (log.isDebugEnabled()) {
            log.debug(format("Retrieved %d message(s)...", messageList.size()));
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
                log.error(format("Failed to mark message as deleted. %s", e.getMessage()), e);
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
    private SearchTerm getSearchTerm(MailboxConfiguration mailboxConfiguration) throws EmailConnectionException {

        FlagTerm seenFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), mailboxConfiguration.getSeen());
        FlagTerm answeredFlagTerm = new FlagTerm(new Flags(Flags.Flag.ANSWERED), mailboxConfiguration.getAnswered());
        FlagTerm recentFlagTerm = new FlagTerm(new Flags(Flags.Flag.RECENT), mailboxConfiguration.getRecent());
        FlagTerm deletedFlagTerm = new FlagTerm(new Flags(Flags.Flag.DELETED), mailboxConfiguration.getDeleted());

        AndTerm searchTerm = new AndTerm(deletedFlagTerm, new AndTerm(seenFlagTerm, new AndTerm(answeredFlagTerm,
                recentFlagTerm)));

        String subjectRegex = mailboxConfiguration.getSubjectRegex();
        if (!StringUtils.isEmpty(subjectRegex)) {
            SubjectTerm subjectTerm = new SubjectTerm(subjectRegex);
            searchTerm = new AndTerm(searchTerm, subjectTerm);
        }

        String fromRegex = mailboxConfiguration.getFromRegex();
        if (!StringUtils.isEmpty(fromRegex)) {
            try {
                FromTerm fromTerm = new FromTerm(new InternetAddress(fromRegex));
                searchTerm = new AndTerm(searchTerm, fromTerm);
            } catch (AddressException e) {
                throw new EmailConnectionException(format("Error occurred when parsing 'from' email address. %s",
                        e.getMessage()));
            }
        }

        String receivedSince = mailboxConfiguration.getReceivedSince();
        if (receivedSince != null) {
            LocalDateTime date = LocalDateTime.parse(receivedSince);
            ReceivedDateTerm receivedSinceDateTerm = new ReceivedDateTerm(ComparisonTerm.GT,
                    from(date.atZone(ZoneId.systemDefault()).toInstant()));
            searchTerm = new AndTerm(searchTerm, receivedSinceDateTerm);
        }

        String receivedUntil = mailboxConfiguration.getReceivedUntil();
        if (receivedUntil != null) {
            LocalDateTime date = LocalDateTime.parse(receivedUntil);
            ReceivedDateTerm receivedUntilDateTerm = new ReceivedDateTerm(ComparisonTerm.LT,
                    from(date.atZone(ZoneId.systemDefault()).toInstant()));
            searchTerm = new AndTerm(searchTerm, receivedUntilDateTerm);
        }

        String sentSince = mailboxConfiguration.getSentSince();
        if (sentSince != null) {
            LocalDateTime date = LocalDateTime.parse(sentSince);
            ReceivedDateTerm sentSinceDateTerm = new ReceivedDateTerm(ComparisonTerm.GT,
                    from(date.atZone(ZoneId.systemDefault()).toInstant()));
            searchTerm = new AndTerm(searchTerm, sentSinceDateTerm);
        }

        String sentUntil = mailboxConfiguration.getSentUntil();
        if (sentUntil != null) {
            LocalDateTime date = LocalDateTime.parse(sentUntil);
            ReceivedDateTerm sentUntilDateTerm = new ReceivedDateTerm(ComparisonTerm.LT,
                    from(date.atZone(ZoneId.systemDefault()).toInstant()));
            searchTerm = new AndTerm(searchTerm, sentUntilDateTerm);
        }
        return searchTerm;
    }
}
