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

import org.apache.commons.pool.PoolableObjectFactory;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.pojo.ConnectionConfiguration;

/**
 * Email Connection Factory
 */
public class EmailConnectionFactory implements PoolableObjectFactory {

    private ConnectionConfiguration connectionConfiguration;

    public EmailConnectionFactory(ConnectionConfiguration connectionConfiguration) {

        this.connectionConfiguration = connectionConfiguration;
    }

    @Override
    public MailBoxConnection makeObject() throws EmailConnectionException {
        return new MailBoxConnection(connectionConfiguration);
    }

    @Override
    public void destroyObject(Object connection) {
        ((MailBoxConnection) connection).disconnect();
    }

    @Override
    public boolean validateObject(Object connection) {

        return ((MailBoxConnection) connection).isConnected();
    }

    @Override
    public void activateObject(Object connection) {
        // Nothing to do here
    }

    @Override
    public void passivateObject(Object o) {
        // Nothing to do here
    }
}
