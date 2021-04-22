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

import org.wso2.carbon.connector.core.connection.Connection;
import org.wso2.carbon.connector.pojo.ConnectionConfiguration;
import org.wso2.carbon.connector.utils.EmailConstants;

import java.util.Properties;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

/**
 * Represents an email connection
 */
public class EmailConnection implements Connection {

    private static final String COMMA_SEPARATOR = ",";
    private static final String WHITESPACE_SEPARATOR = " ";
    private static final String TRUE = String.valueOf(Boolean.TRUE);

    private Session session;
    private EmailProtocol protocol;

    public EmailConnection(ConnectionConfiguration connectionConfiguration) {

        this.protocol = connectionConfiguration.getProtocol();
        Properties sessionProperties = setSessionProperties(connectionConfiguration.getHost(),
                connectionConfiguration.getPort(), connectionConfiguration.getRequireAuthentication());
        sessionProperties.putAll(setTimeouts(connectionConfiguration.getReadTimeout(),
                connectionConfiguration.getWriteTimeout(), connectionConfiguration.getConnectionTimeout()));

        if (protocol.isSecure()) {
            sessionProperties.putAll(setSecureProperties(connectionConfiguration));
        }

        if (connectionConfiguration.getRequireAuthentication()) {
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

}
