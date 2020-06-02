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

import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.connection.EmailProtocol;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.pojo.ConnectionConfiguration;
import org.wso2.carbon.connector.pojo.MailboxConfiguration;

/**
 * Utils for reading configurations from operations
 */
public final class ConfigurationUtils {

    private ConfigurationUtils() {

    }

    /**
     * Extracts mailbox connection configurations from operation template
     *
     * @param messageContext Message Context from which the parameters should be extracted from
     * @return Mailbox Configurations set
     */
    public static MailboxConfiguration getMailboxConfigFromContext(MessageContext messageContext) {

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

    /**
     * Retrieves connection name from message context if configured as configKey attribute
     * or from the template parameter
     *
     * @param messageContext Message Context from which the parameters should be extracted from
     * @return connection name
     */
    public static String getConnectionName(MessageContext messageContext) throws InvalidConfigurationException {
        // Retrieve name configured init template if referred to as the configKey attribute
        String connectionName = (String) messageContext.getProperty(EmailConstants.NAME);
        if (connectionName == null) {
            connectionName = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.CONNECTION);
            if (connectionName == null) {
                throw new InvalidConfigurationException("Connection name is not set.");
            }
        }
        return connectionName;
    }

    /**
     * Extracts connection configuration parameters from operation template
     *
     * @param messageContext Message Context from which the parameters should be extracted from
     * @return Connection Configurations set
     * @throws InvalidConfigurationException if the configurations contain invalid inputs
     */
    public static ConnectionConfiguration getConnectionConfigFromContext(MessageContext messageContext)
            throws InvalidConfigurationException {

        String host = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.HOST);
        String port = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.PORT);
        String connectionName = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.NAME);
        String username = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.USERNAME);
        String password = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.PASSWORD);
        String protocol = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.PROTOCOL);
        String readTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.READ_TIMEOUT);
        String connectionTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.CONNECTION_TIMEOUT);
        String writeTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.WRITE_TIMEOUT);
        String requireTLS = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.REQUIRE_TLS);
        String checkServerIdentity = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.CHECK_SERVER_IDENTITY);
        String trustedHosts = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.TRUSTED_HOSTS);
        String sslProtocols = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.SSL_PROTOCOLS);
        String cipherSuites = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.CIPHER_SUITES);
        String maxActiveConnections = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.MAX_ACTIVE_CONNECTIONS);
        String maxIdleConnections = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.MAX_IDLE_CONNECTIONS);
        String maxWaitTime = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.MAX_WAIT_TIME);
        String minEvictionTime = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.MAX_EVICTION_TIME);
        String evictionCheckInterval = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.EVICTION_CHECK_INTERVAL);
        String exhaustedAction = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.EXHAUSTED_ACTION);
        String initialisationPolicy = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.INITIALISATION_POLICY);
        String disablePooling = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.DISABLE_POOLING);

        ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration();
        connectionConfiguration.setHost(host);
        connectionConfiguration.setPort(port);
        connectionConfiguration.setConnectionName(connectionName);
        connectionConfiguration.setPassword(password);
        connectionConfiguration.setProtocol(EmailProtocol.valueOf(protocol));
        connectionConfiguration.setReadTimeout(readTimeout);
        connectionConfiguration.setWriteTimeout(writeTimeout);
        connectionConfiguration.setConnectionTimeout(connectionTimeout);
        connectionConfiguration.setRequireTLS(Boolean.parseBoolean(requireTLS));
        connectionConfiguration.setUsername(username);
        connectionConfiguration.setCheckServerIdentity(Boolean.parseBoolean(checkServerIdentity));
        connectionConfiguration.setTrustedHosts(trustedHosts);
        connectionConfiguration.setSslProtocols(sslProtocols);
        connectionConfiguration.setCipherSuites(cipherSuites);

        if (maxActiveConnections != null) {
            connectionConfiguration.setMaxActiveConnections(Integer.parseInt(maxActiveConnections));
        }
        if (maxWaitTime != null) {
            connectionConfiguration.setMaxWaitTime(Long.parseLong(maxWaitTime));
        }
        if (maxIdleConnections != null) {
            connectionConfiguration.setMaxIdleConnections(Integer.parseInt(maxIdleConnections));
        }
        if (minEvictionTime != null) {
            connectionConfiguration.setMinEvictionTime(Long.parseLong(minEvictionTime));
        }
        if (evictionCheckInterval != null) {
            connectionConfiguration.setEvictionCheckInterval(Long.parseLong(evictionCheckInterval));
        }
        if (exhaustedAction != null) {
            connectionConfiguration.setExhaustedAction(exhaustedAction);
        }
        if (initialisationPolicy != null) {
            connectionConfiguration.setInitialisationPolicy(initialisationPolicy);
        }
        connectionConfiguration.setDisablePooling(Boolean.parseBoolean(disablePooling));

        return connectionConfiguration;
    }

}
