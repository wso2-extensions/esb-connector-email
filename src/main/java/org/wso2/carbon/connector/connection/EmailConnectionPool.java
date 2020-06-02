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
import org.apache.commons.pool.impl.GenericObjectPool;
import org.wso2.carbon.connector.exception.EmailConnectionPoolException;
import org.wso2.carbon.connector.pojo.ConnectionConfiguration;

import static java.lang.String.format;

/**
 * Connection Pool Implementation for Email Connections
 */
public class EmailConnectionPool extends GenericObjectPool {

    private static final Log log = LogFactory.getLog(EmailConnectionPool.class);

    EmailConnectionPool(EmailConnectionFactory objFactory, ConnectionConfiguration connectionConfiguration) {

        super(objFactory);
        this.setMaxActive(connectionConfiguration.getMaxActiveConnections());
        this.setMaxIdle(connectionConfiguration.getMaxIdleConnections());
        if (connectionConfiguration.getMaxWaitTime() > 0) {
            this.setMaxWait(connectionConfiguration.getMaxWaitTime());
        }
        if (connectionConfiguration.getMinEvictionTime() > 0) {
            this.setMinEvictableIdleTimeMillis(connectionConfiguration.getMinEvictionTime());
        }
        if (connectionConfiguration.getEvictionCheckInterval() > 0) {
            this.setTimeBetweenEvictionRunsMillis(connectionConfiguration.getEvictionCheckInterval());
        }
        if (connectionConfiguration.getExhaustedAction() != null) {
            this.setWhenExhaustedAction(getExhaustedAction(connectionConfiguration.getExhaustedAction()));
        }
        this.setTestOnBorrow(true);
    }

    /**
     * Parse exhausted action from string
     *
     * @param exhaustedAction exhausted action in string
     * @return respective byte that represents the action
     */
    private byte getExhaustedAction(String exhaustedAction) {

        byte action;
        switch (exhaustedAction) {
            case "WHEN_EXHAUSTED_FAIL":
                action = WHEN_EXHAUSTED_FAIL;
                break;
            case "WHEN_EXHAUSTED_BLOCK":
                action = WHEN_EXHAUSTED_BLOCK;
                break;
            case "WHEN_EXHAUSTED_GROW":
                action = WHEN_EXHAUSTED_GROW;
                break;
            default:
                action = DEFAULT_WHEN_EXHAUSTED_ACTION;
                log.warn(format("Unable to find the configured exhausted action. Setting to default: %s.", action));
                break;
        }
        return action;
    }

    @Override
    public synchronized Object borrowObject() throws EmailConnectionPoolException {

        try {
            log.debug("Borrowing object from the connection pool...");
            return super.borrowObject();
        } catch (Exception e) {
            throw new EmailConnectionPoolException(format("Error occurred while borrowing connection from the pool. %s",
                    e.getMessage()), e);
        }
    }

    @Override
    public synchronized void returnObject(Object obj) {

        try {
            log.debug("Returning object to the connection pool...");
            super.returnObject(obj);
        } catch (Exception e) {
            log.error(format("Error occurred while returning the connection to the pool. %s", e.getMessage()), e);
        }
    }

    @Override
    public synchronized void close() throws EmailConnectionPoolException {

        try {
            super.close();
        } catch (Exception e) {
            throw new EmailConnectionPoolException(format("Error occurred while closing the connections. %s",
                    e.getMessage()), e);
        }
    }
}
