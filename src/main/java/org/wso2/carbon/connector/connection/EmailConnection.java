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
package org.wso2.carbon.connector.connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.connector.connection.oauth.OAuthUtils;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.connection.ConnectionConfig;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.pojo.ConnectionConfiguration;
import org.wso2.carbon.connector.utils.EmailConstants;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import static java.lang.String.format;

/**
 * Represents an email connection
 */
public class EmailConnection implements Connection {

    private static final String COMMA_SEPARATOR = ",";
    private static final String WHITESPACE_SEPARATOR = " ";
    private static final String TRUE = String.valueOf(Boolean.TRUE);

    private static final Log log = LogFactory.getLog(EmailConnection.class);

    private Session session;
    private EmailProtocol protocol;

    /**
     * Tests the connection with the email server
     *
     * @throws EmailConnectionException if the connection fails
     */
    public void testConnection() throws EmailConnectionException {
        Store store = null;
        try {
            String protocolName = protocol.getName();
            
            if (protocolName == null || protocolName.isEmpty()) {
                throw new EmailConnectionException("Protocol name is null or empty");
            }
            
            // For SMTP/SMTPS protocols, we need to use Transport instead of Store
            if (protocolName.toLowerCase().startsWith("smtp")) {
                javax.mail.Transport transport = null;
                try {
                    transport = session.getTransport(protocolName);
                    transport.connect();
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully connected to " + protocolName + " server");
                    }
                } finally {
                    if (transport != null && transport.isConnected()) {
                        try {
                            transport.close();
                        } catch (MessagingException e) {
                            log.warn("Error closing transport connection: " + e.getMessage(), e);
                        }
                    }
                }
            } else {
                // For IMAP/IMAPS/POP3/POP3S protocols, use Store
                store = session.getStore(protocolName);
                store.connect();
                if (log.isDebugEnabled()) {
                    log.debug("Successfully connected to " + protocolName + " server");
                }
            }
        } catch (NoSuchProviderException e) {
            throw new EmailConnectionException("Invalid email protocol provider: " + 
                    (protocol != null ? protocol.getName() : "null"), e);
        } catch (MessagingException e) {
            throw new EmailConnectionException("Failed to establish connection with the email server: " + 
                    e.getMessage(), e);
        } finally {
            // Close the store if it was opened
            if (store != null && store.isConnected()) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    log.warn("Error closing store connection: " + e.getMessage(), e);
                }
            }
        }
    }

    public EmailConnection(ConnectionConfiguration connectionConfiguration) throws EmailConnectionException {

        this.protocol = connectionConfiguration.getProtocol();
        Properties sessionProperties = setSessionProperties(connectionConfiguration.getHost(),
                connectionConfiguration.getPort(), connectionConfiguration.getRequireAuthentication());
        sessionProperties.putAll(setTimeouts(connectionConfiguration.getReadTimeout(),
                connectionConfiguration.getWriteTimeout(), connectionConfiguration.getConnectionTimeout()));

        if (protocol.isSecure()) {
            sessionProperties.putAll(setSecureProperties(connectionConfiguration));
        }

        if (connectionConfiguration.isOAuth2Enabled()) {
            sessionProperties.setProperty(protocol.getAuthMechanismsProperty(), EmailConstants.AUTH_MECHANISM_XOAUTH2);
        }

        if (connectionConfiguration.getRequireAuthentication()) {
            if (connectionConfiguration.isOAuth2Enabled()) {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to connect to " + connectionConfiguration.getProtocol().getName() +
                            " server for " + connectionConfiguration.getUsername() +
                            " using grant-type : " + connectionConfiguration.getOAuthConfig().getGrantType());
                }
                try {
                    String password = OAuthUtils.generateAccessToken(connectionConfiguration);
                    connectionConfiguration.setPassword(password);
                } catch (EmailConnectionException e) {
                    log.error(e.getMessage());
                    throw new EmailConnectionException(format("An error occurred while generating access token for " +
                            "%s. " + e.getMessage(), connectionConfiguration.getUsername()), e);
                } catch (InvalidConfigurationException e) {
                    throw new EmailConnectionException("An error occurred while configuring connections", e);
                }
            }
            this.session = Session.getInstance(sessionProperties,
                    new javax.mail.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {

                            return new PasswordAuthentication(connectionConfiguration.getUsername(),
                                    connectionConfiguration.getPassword());
                        }
                    });
        } else {
            this.session = Session.getInstance(sessionProperties, null);
        }
    }

    public Session getSession() {

        return session;
    }

    /**
     * Sets basic session properties required by the protocol
     *
     * @param host Host name of the server
     * @param port Port to connect to
     * @return Properties configured
     */
    private Properties setSessionProperties(String host, String port, Boolean requireAuthentication) {

        Properties props = new Properties();
        props.setProperty(protocol.getPortProperty(), port);
        props.setProperty(protocol.getHostProperty(), host);
        props.setProperty(protocol.getTransportProtocolProperty(), protocol.getName());
        props.setProperty(protocol.getMailAuthProperty(), String.valueOf(requireAuthentication));
        return props;
    }

    /**
     * Sets secure properties
     *
     * @param connectionConfiguration configurations to be set
     * @return Properties to be configured
     */
    private Properties setSecureProperties(ConnectionConfiguration connectionConfiguration) {

        Properties props = new Properties();
        props.setProperty(protocol.getStartTlsProperty(), TRUE);
        if (connectionConfiguration.isRequireTLS()) {
            props.setProperty(protocol.getStartTlsProperty(), TRUE);
        } else {
            props.setProperty(protocol.getSslEnableProperty(), TRUE);
            props.setProperty(protocol.getSocketFactoryFallbackProperty(),
                    EmailConstants.DEFAULT_SOCKETFACTORY_FALLBACK);
            props.setProperty(protocol.getSocketFactoryPortProperty(),
                    String.valueOf(connectionConfiguration.getPort()));
        }

        if (connectionConfiguration.getCipherSuites() != null) {
            props.setProperty(protocol.getSslCipherSuitesProperty(),
                    replaceWithWhitespace(connectionConfiguration.getCipherSuites()));
        }

        if (connectionConfiguration.getSslProtocols() != null) {
            props.setProperty(protocol.getSslProtocolsProperty(),
                    replaceWithWhitespace(connectionConfiguration.getSslProtocols()));
        }

        if (connectionConfiguration.getTrustedHosts() != null) {
            props.setProperty(protocol.getSslTrustProperty(),
                    replaceWithWhitespace(connectionConfiguration.getTrustedHosts()));
        }

        if (connectionConfiguration.isCheckServerIdentity()) {
            props.setProperty(protocol.getCheckServerIdentityProperty(), Boolean.toString(connectionConfiguration
                    .isCheckServerIdentity()));
        }

        return props;
    }

    /**
     * Sets timeout properties
     *
     * @param readTimeout       Read Timeout
     * @param writeTimeout      Write Timeout
     * @param connectionTimeout Connection Timeout
     * @return timeout properties
     */
    private Properties setTimeouts(String readTimeout, String writeTimeout, String connectionTimeout) {

        Properties props = new Properties();
        if (readTimeout != null) {
            props.setProperty(protocol.getReadTimeoutProperty(), readTimeout);
        }
        if (writeTimeout != null) {
            props.setProperty(protocol.getWriteTimeoutProperty(), writeTimeout);
        }
        if (connectionTimeout != null) {
            props.setProperty(protocol.getConnectionTimeoutProperty(), connectionTimeout);
        }
        return props;
    }

    /**
     * Replace a comma in a comma separated string with whitespace
     *
     * @param configString Comma separated string
     * @return Whitespace separated string
     */
    private String replaceWithWhitespace(String configString) {

        return configString.replace(COMMA_SEPARATOR, WHITESPACE_SEPARATOR).trim();
    }

    @Override
    public void connect(ConnectionConfig config) throws ConnectException {

    }

    @Override
    public void close() throws ConnectException {

    }
}
