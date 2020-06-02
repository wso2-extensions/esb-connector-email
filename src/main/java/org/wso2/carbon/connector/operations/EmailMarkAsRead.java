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

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.connection.EmailConnectionManager;
import org.wso2.carbon.connector.connection.EmailConnectionPool;
import org.wso2.carbon.connector.connection.MailBoxConnection;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.exception.ContentBuilderException;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.exception.EmailConnectionPoolException;
import org.wso2.carbon.connector.exception.EmailNotFoundException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.utils.ConfigurationUtils;
import org.wso2.carbon.connector.utils.EmailConstants;
import org.wso2.carbon.connector.utils.EmailOperationUtils;
import org.wso2.carbon.connector.utils.Error;
import org.wso2.carbon.connector.utils.ResponseHandler;

import javax.mail.Flags;

import static java.lang.String.format;

public class EmailMarkAsRead extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) {

        String folder = (String) getParameter(messageContext, EmailConstants.FOLDER);
        String emailID = (String) getParameter(messageContext, EmailConstants.EMAIL_ID);
        String errorString = "Error occurred while marking email with ID: %s as read. %s";
        EmailConnectionPool pool = null;
        MailBoxConnection connection = null;
        try {
            String connectionName = ConfigurationUtils.getConnectionName(messageContext);
            pool = EmailConnectionManager.getEmailConnectionManager().getConnectionPool(connectionName);
            connection = (MailBoxConnection) pool.borrowObject();
            boolean status = EmailOperationUtils.changeEmailState(connection, folder, emailID, Flags.Flag.SEEN,
                    false);
            ResponseHandler.generateOutput(messageContext, status);
        } catch (EmailConnectionException | EmailConnectionPoolException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.CONNECTIVITY);
            handleException(format(errorString, folder, e.getMessage()), e, messageContext);
        } catch (EmailNotFoundException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.EMAIL_NOT_FOUND);
            handleException(format(errorString, folder, e.getMessage()), e, messageContext);
        } catch (InvalidConfigurationException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.INVALID_CONFIGURATION);
            handleException(format(errorString, folder, e.getMessage()), e, messageContext);
        } catch (ContentBuilderException e) {
            ResponseHandler.setErrorsInMessage(messageContext, Error.RESPONSE_GENERATION);
            handleException(format(errorString, folder, e.getMessage()), e, messageContext);
        } finally {
            if (pool != null) {
                pool.returnObject(connection);
            }
        }

    }
}
