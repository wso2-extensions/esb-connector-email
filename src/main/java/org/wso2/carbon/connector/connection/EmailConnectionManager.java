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
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.exception.EmailConnectionPoolException;
import org.wso2.carbon.connector.pojo.ConnectionConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * Manages email connections and connection pools
 */
public class EmailConnectionManager {

    private static final Log log = LogFactory.getLog(EmailConnectionManager.class);

    private Map<String, EmailConnection> connectionMap;
    private Map<String, EmailConnectionPool> connectionPoolMap;

    private static EmailConnectionManager manager;

    private EmailConnectionManager(){
        this.connectionMap = Collections.synchronizedMap(new HashMap<>());
        this.connectionPoolMap = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Gets Email Connection Manager
     *
     * @return EmailConnectionManager instance
     */
    public static synchronized EmailConnectionManager getEmailConnectionManager(){
        if (manager == null){
            manager = new EmailConnectionManager();
        }
        return manager;
    }

    /**
     * Adds new connection
     *
     * @param name name of the connection
     * @param emailConnection email connection
     */
    private void addConnection(String name, EmailConnection emailConnection) {
        if (log.isDebugEnabled()){
            log.debug(format("Creating connection : %s", name));
        }
        connectionMap.putIfAbsent(name, emailConnection);
    }

    /**
     * Retrieves connection by name
     *
     * @param name name of the connection
     * @return Email connection
     * @throws EmailConnectionException if connection from the name does not exist
     */
    public EmailConnection getConnection(String name) throws EmailConnectionException {
        if (connectionMap.get(name) != null) {
            return connectionMap.get(name);
        }
        throw new EmailConnectionException(format("Connection with the name %s has not been initialized.", name));
    }

    /**
     * Creates a connection pool
     *
     * @param name name of the connection
     * @param emailConnectionPool email connection pool
     */
    private void addConnectionPool(String name, EmailConnectionPool emailConnectionPool) {
        if (log.isDebugEnabled()){
            log.debug(format("Creating connection pool for connection : %s", name));
        }
        connectionPoolMap.putIfAbsent(name, emailConnectionPool);
    }

    /**
     * Retrieves connection pool by name
     *
     * @param name name of the connection
     * @return Email connection Pool
     * @throws EmailConnectionException if connection pool from the name does not exist
     */
    public EmailConnectionPool getConnectionPool(String name) throws EmailConnectionException {
        if (connectionPoolMap.get(name) != null){
            if (log.isDebugEnabled()){
                log.debug(format("Returning connection pool for connection: %s", name));
            }
            return connectionPoolMap.get(name);
        }
        throw new EmailConnectionException(format("Connection with the name %s has not been initialized.", name));
    }

    /**
     * Creates a connection with the given configuration
     *
     * @param connectionConfiguration connection configuration
     */
    public synchronized void createConnection(ConnectionConfiguration connectionConfiguration) {
        String connectionName = connectionConfiguration.getConnectionName();
        if (connectionConfiguration.getProtocol().getName().equalsIgnoreCase(EmailProtocol.SMTP.name())
                && connectionMap.get(connectionName) == null){
            // For SMTP protocols a connection pool is not required as they require only a session, which need not be
            // manipulated as the connection.
            EmailConnection connection = new EmailConnection(connectionConfiguration);
            addConnection(connectionConfiguration.getConnectionName(), connection);
        } else if (!connectionConfiguration.getProtocol().getName().equalsIgnoreCase(EmailProtocol.SMTP.name())
                && connectionPoolMap.get(connectionName) == null) {
            // For other protocols, such as IMAP and POP3, connections to a store and folder is made which requires to
            // handled. Hence, for these instances, we will create a connection pool to optimize the use of these
            // connections.
            EmailConnectionPool pool = new EmailConnectionPool(new EmailConnectionFactory(connectionConfiguration),
                    connectionConfiguration);
            addConnectionPool(connectionConfiguration.getConnectionName(), pool);
        } else {
            if (log.isDebugEnabled()){
                log.debug(format("Connection: %s exists", connectionName));
            }
        }
    }

    /**
     * Clears connection pools
     *
     */
    public void clearConnectionPools(){
        log.debug("Clearing connection pools...");
        for (Map.Entry<String, EmailConnectionPool> pool : connectionPoolMap.entrySet()){
            try {
                pool.getValue().close();
            } catch (EmailConnectionPoolException e) {
                log.error(format("Failed to clear connection pool for %s.", pool.getKey()), e);
            }
        }
    }

}
