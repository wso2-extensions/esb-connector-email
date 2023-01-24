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

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.connector.connection.EmailConnectionHandler;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.pojo.ConnectionConfiguration;
import org.wso2.carbon.connector.pojo.OAuthConfig;
import org.wso2.carbon.connector.utils.EmailConstants;
import org.wso2.carbon.connector.utils.EmailUtils;
import org.wso2.carbon.connector.utils.Error;

/**
 * Configures and initializes the email connection
 */
public class EmailConfig extends AbstractConnector implements ManagedLifecycle {

    @Override
    public void connect(MessageContext messageContext) {

        try {
            ConnectionConfiguration configuration = getConnectionConfigFromContext(messageContext);
            EmailUtils.createConnection(configuration);
        } catch (InvalidConfigurationException e) {
            EmailUtils.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
            handleException("Failed to initiate email configuration.", e, messageContext);
        } catch (EmailConnectionException e) {
            EmailUtils.setErrorsInMessage(messageContext, Error.CONNECTIVITY);
            handleException("An error occurred in email connection creation.", e, messageContext);
        }
    }

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        // Nothing to do when initiating the connector
    }

    @Override
    public void destroy() {

        EmailConnectionHandler.getConnectionHandler().shutdownConnectorConnections();
    }

    /**
     * Extracts connection configuration parameters from operation template
     *
     * @param messageContext Message Context from which the parameters should be extracted from
     * @return Connection Configurations set
     * @throws InvalidConfigurationException if the configurations contain invalid inputs
     */
    private ConnectionConfiguration getConnectionConfigFromContext(MessageContext messageContext)
            throws InvalidConfigurationException {

        String host = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.HOST);
        String port = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.PORT);
        String connectionName = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.NAME);
        String username = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.USERNAME);
        String password = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.PASSWORD);
        String protocol = (String) ConnectorUtils.lookupTemplateParamater(messageContext, EmailConstants.CONNECTION_TYPE);
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
        String requireAuthentication = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.REQUIRE_AUTHENTICATION);
        String enableOAuth2 = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.ENABLE_OAUTH2);

        ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration();
        connectionConfiguration.setHost(host);
        connectionConfiguration.setPort(port);
        connectionConfiguration.setConnectionName(connectionName);
        connectionConfiguration.setRequireAuthentication(requireAuthentication);
        connectionConfiguration.setEnableOAuth2(enableOAuth2);
        if (Boolean.parseBoolean(enableOAuth2)) {
            OAuthConfig oAuthConfig = generateOAuthConfig(messageContext, connectionName);
            connectionConfiguration.setOAuthConfig(oAuthConfig);
        }
        connectionConfiguration.setPassword(password);
        connectionConfiguration.setProtocol(protocol);
        connectionConfiguration.setReadTimeout(readTimeout);
        connectionConfiguration.setWriteTimeout(writeTimeout);
        connectionConfiguration.setConnectionTimeout(connectionTimeout);
        connectionConfiguration.setRequireTLS(Boolean.parseBoolean(requireTLS));
        connectionConfiguration.setUsername(username);
        connectionConfiguration.setCheckServerIdentity(Boolean.parseBoolean(checkServerIdentity));
        connectionConfiguration.setTrustedHosts(trustedHosts);
        connectionConfiguration.setSslProtocols(sslProtocols);
        connectionConfiguration.setCipherSuites(cipherSuites);
        connectionConfiguration.setConfiguration(ConnectorUtils.getPoolConfiguration(messageContext));

        return connectionConfiguration;
    }

    private static OAuthConfig generateOAuthConfig(MessageContext messageContext, String connectionName)
            throws InvalidConfigurationException {
        String grantType = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.GRANT_TYPE);
        String clientID = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.CLIENT_ID);
        String clientSecret = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.CLIENT_SECRET);
        String refreshToken = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.REFRESH_TOKEN);
        String tokenUrl = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.TOKEN_URL);
        String scope = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                EmailConstants.SCOPE);
        String tokenId = EmailUtils.getTokenID(connectionName);
        OAuthConfig oAuthConfig = new OAuthConfig();
        oAuthConfig.setGrantType(grantType);
        oAuthConfig.setClientId(clientID);
        oAuthConfig.setClientSecret(clientSecret);
        oAuthConfig.setRefreshToken(refreshToken);
        oAuthConfig.setTokenUrl(tokenUrl);
        oAuthConfig.setScope(scope);
        oAuthConfig.setTokenId(tokenId);
        return oAuthConfig;
    }
}
