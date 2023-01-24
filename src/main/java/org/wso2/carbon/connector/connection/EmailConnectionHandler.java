/*
 * Copyright (c) 2023, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.connector.connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.pool.Configuration;
import org.wso2.carbon.connector.core.pool.ConnectionFactory;
import org.wso2.carbon.connector.core.pool.ConnectionPool;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.utils.EmailConstants;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/**
 * Handles the connections
 */
public class EmailConnectionHandler {
    private static final Log log = LogFactory.getLog(EmailConnectionHandler.class);
    private static final EmailConnectionHandler handler;
    // Stores connections/connection pools against connection code name
    // defined as <connector_name>:<connection_name>
    private final Map<String, Object> connectionMap;

    static {
        handler = new EmailConnectionHandler();
    }

    private EmailConnectionHandler() {

        this.connectionMap = new ConcurrentHashMap<>();
    }

    /**
     * Gets the Connection Handler instance
     *
     * @return EmailConnectionHandler instance
     */
    public static EmailConnectionHandler getConnectionHandler() {

        return handler;
    }

    /**
     * Creates a new connection pool and stores the connection
     *
     * @param connectionName Name of the connection
     * @param factory        Connection Factory that defines how to create connections
     * @param configuration  Configurations for the connection pool
     */
    public void createConnection(String connectionName, ConnectionFactory factory,
                                 Configuration configuration) {

        ConnectionPool pool = new ConnectionPool(factory, configuration);
        connectionMap.putIfAbsent(getCode(connectionName), pool);
    }

    /**
     * Stores a new single connection
     *
     * @param connectionName Name of the connection
     * @param connection     Connection to be stored
     */
    public void createConnection(String connectionName, Connection connection) {

        connectionMap.putIfAbsent(getCode(connectionName), connection);
    }

    /**
     * Retrieve connection by connector name and connection name
     *
     * @param connectionName Name of the connection
     * @return the connection
     * @throws ConnectException if failed to get connection
     */
    public Connection getConnection(String connectionName) throws EmailConnectionException, ConnectException {

        Connection connection = null;
        String connectorCode = getCode(connectionName);
        Object connectionObj = connectionMap.get(connectorCode);
        if (connectionObj != null) {
            if (connectionObj instanceof ConnectionPool) {
                connection = (Connection) ((ConnectionPool) connectionObj).borrowObject();
            } else if (connectionObj instanceof Connection) {
                connection = (Connection) connectionObj;
            }
        } else {
            throw new EmailConnectionException(format("Error occurred during retrieving connection. " +
                    "Connection %s does not exist.", connectionName));
        }
        return connection;
    }

    /**
     * Return borrowed connection
     *
     * @param connectionName Name of the connection
     * @param connection     Connection to be returned to the pool
     */
    public void returnConnection(String connectionName, Connection connection) {
        String connectorCode = this.getCode(connectionName);
        Object connectionObj = this.connectionMap.get(connectorCode);
        if (connectionObj instanceof ConnectionPool) {
            ((ConnectionPool) connectionObj).returnObject(connection);
        }
    }

    /**
     * Shutdown all the connection pools
     * and unregister from the handler.
     */
    public void shutdownConnections() {

        for (Map.Entry<String, Object> connection : connectionMap.entrySet()) {
            closeConnection(connection.getKey(), connection.getValue());
        }
        connectionMap.clear();
    }

    /**
     * Shutdown a specified connection
     * @param connectionName Name of the connection
     */
    public void shutDownConnection(String connectionName){
        String connectionKey = getCode(connectionName);
        log.info(format("Shutting Down Connection : %s", connectionKey));
        Object object = connectionMap.get(connectionKey);
        closeConnection(connectionKey, object);
        connectionMap.remove(connectionKey);
    }

    /**
     * Shutdown connection pools for a specified connector
     * and unregister from the handler.
     */
    public void shutdownConnectorConnections() {
        Iterator<Map.Entry<String, Object>> it = connectionMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> connection = it.next();
            if (connection.getKey().split(":")[0].equals(EmailConstants.CONNECTOR_NAME)) {
                closeConnection(connection.getKey(), connection.getValue());
                it.remove();
            }
        }
    }

    /**
     * Check if a connection exists for the connector by the same connection name
     *
     * @param connectionName Name of the connection
     * @return true if a connection exists, false otherwise
     */
    public boolean checkIfConnectionExists(String connectionName) {

        return connectionMap.containsKey(getCode(connectionName));
    }

    /**
     * Closes the connection.
     *
     * @param conName       Name of connection entry
     * @param connectionObj Connection Object
     */
    private void closeConnection(String conName, Object connectionObj) {
        if (connectionObj instanceof ConnectionPool) {
            try {
                ((ConnectionPool) connectionObj).close();
            } catch (ConnectException e) {
                log.error("Failed to close connection pool. ", e);
            }
        } else if (connectionObj instanceof Connection) {
            try {
                ((Connection) connectionObj).close();
            } catch (ConnectException e) {
                log.error("Failed to close connection " + conName, e);
            }
        }
    }

    /**
     * Retrieves the connection code defined as <connector_name>:<connection_name>
     *
     * @param connectionName Name of the connection
     * @return the connector code
     */
    private String getCode(String connectionName) {

        return format("%s:%s", EmailConstants.CONNECTOR_NAME, connectionName);
    }

}
