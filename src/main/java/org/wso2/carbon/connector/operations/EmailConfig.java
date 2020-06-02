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
import org.wso2.carbon.connector.connection.EmailConnectionManager;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.pojo.ConnectionConfiguration;
import org.wso2.carbon.connector.utils.ConfigurationUtils;
import org.wso2.carbon.connector.utils.Error;
import org.wso2.carbon.connector.utils.ResponseHandler;

import static java.lang.String.format;

/**
 * Configures and initializes the email connection
 */
public class EmailConfig extends AbstractConnector implements ManagedLifecycle {

    @Override
    public void connect(MessageContext messageContext) {

        try {
            ConnectionConfiguration configuration = ConfigurationUtils.getConnectionConfigFromContext(messageContext);
            EmailConnectionManager.getEmailConnectionManager().createConnection(configuration);
        } catch (InvalidConfigurationException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
            handleException(format("Failed to initiate email configuration. %s", e.getMessage()), messageContext);
        }
    }

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        // Nothing to do when initiating the connector
    }

    @Override
    public void destroy() {

        EmailConnectionManager.getEmailConnectionManager().clearConnectionPools();
    }
}
