package org.wso2.carbon.connector.connection;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.pojo.ConnectionConfiguration;
import org.wso2.carbon.connector.pojo.OAuthConfig;
import org.wso2.carbon.connector.utils.EmailConstants;
import org.wso2.carbon.connector.utils.EmailUtils;

/**
 * Tests the email connection using existing connection framework
 */
public class TestConnection extends AbstractConnector {
    @Override
    public void connect(MessageContext messageContext) {
        // Get the Axis2 MessageContext for setting properties
        Axis2MessageContext axis2smc = (Axis2MessageContext) messageContext;
        org.apache.axis2.context.MessageContext axis2MessageCtx = axis2smc.getAxis2MessageContext();

        // Set the status of the connection test
        try {
            ConnectionConfiguration configuration = getConnectionConfigFromContext(messageContext);
            EmailUtils.testConnection(configuration);
            axis2MessageCtx.setProperty(EmailConstants.STATUS, "success");
        } catch (InvalidConfigurationException | EmailConnectionException e){
            axis2MessageCtx.setProperty(EmailConstants.STATUS, "failed");
            axis2MessageCtx.setProperty(EmailConstants.ERROR_MESSAGE, e.getMessage());
        }
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
        connectionConfiguration.setConnectionName("TEST_CONNECTION");
        connectionConfiguration.setRequireAuthentication(requireAuthentication);
        connectionConfiguration.setEnableOAuth2(enableOAuth2);
        if (Boolean.parseBoolean(enableOAuth2)) {
            OAuthConfig oAuthConfig = generateOAuthConfig(messageContext, "TEST_CONNECTION");
            connectionConfiguration.setOAuthConfig(oAuthConfig);
        }
        connectionConfiguration.setPassword(password);
        connectionConfiguration.setProtocolByName(protocol);
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



