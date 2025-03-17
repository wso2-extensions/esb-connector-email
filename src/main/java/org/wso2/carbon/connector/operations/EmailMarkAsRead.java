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

import com.google.gson.JsonObject;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.connection.EmailConnectionHandler;
import org.wso2.carbon.connector.connection.MailBoxConnection;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.exception.EmailNotFoundException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.utils.AbstractEmailConnectorOperation;
import org.wso2.carbon.connector.utils.EmailConstants;
import org.wso2.carbon.connector.utils.EmailUtils;
import org.wso2.carbon.connector.utils.Error;

import javax.mail.Flags;

public class EmailMarkAsRead extends AbstractEmailConnectorOperation {

    @Override
    public void execute(MessageContext messageContext, String responseVariable,
                        Boolean overwriteBody) {

        String folder = (String) getParameter(messageContext, EmailConstants.FOLDER);
        String emailID = (String) getParameter(messageContext, EmailConstants.EMAIL_ID);
        String connectionName = null;
        EmailConnectionHandler handler = EmailConnectionHandler.getConnectionHandler();
        MailBoxConnection connection = null;
        try {
            connectionName = EmailUtils.getConnectionName(messageContext);
            connection = (MailBoxConnection) handler.getConnection(connectionName);
            boolean status = EmailUtils.changeEmailState(connection, folder, emailID, Flags.Flag.SEEN,
                    false);
            JsonObject resultJSON = generateOperationResult(messageContext, status, null);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
        } catch (EmailConnectionException | ConnectException e) {
            JsonObject resultJSON = generateOperationResult(messageContext, false, Error.CONNECTIVITY);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(e.getMessage(), e, messageContext);
        } catch (EmailNotFoundException e) {
            JsonObject resultJSON = generateOperationResult(messageContext, false, Error.EMAIL_NOT_FOUND);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(e.getMessage(), e, messageContext);
        } catch (InvalidConfigurationException e) {
            JsonObject resultJSON = generateOperationResult(messageContext, false, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(e.getMessage(), e, messageContext);
        }  finally {
            if (connection != null) {
                handler.returnConnection(connectionName, connection);
            }
        }
    }
}
