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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.pojo.ConnectionConfiguration;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;

import static java.lang.String.format;

/**
 * Represents a connection to a mailbox
 */
public class MailBoxConnection extends EmailConnection {

    private static final Logger log = LoggerFactory.getLogger(MailBoxConnection.class);

    private Store store;
    private Folder folder;

    MailBoxConnection(ConnectionConfiguration connectionConfiguration) throws EmailConnectionException {

        super(connectionConfiguration);
        try {
            this.store = this.getSession().getStore(connectionConfiguration.getProtocol().getName());
            this.store.connect();
        } catch (MessagingException e) {
            throw new EmailConnectionException(format("Error occurred while connecting to the store. %s",
                    e.getMessage()), e);
        }
    }

    /**
     * Opens and return the email folder.
     * <p>
     * If there was an already opened folder and a different one is requested the opened folder will be closed
     * and the new one will be opened.
     *
     * @param mailBoxFolder the name of the folder to be opened.
     * @param openMode      open the folder in READ_ONLY or READ_WRITE mode
     * @return the opened Folder
     */
    public synchronized Folder getFolder(String mailBoxFolder, int openMode) throws EmailConnectionException {

        try {
            if (folder != null) {
                if (isCurrentFolder(mailBoxFolder) && folder.isOpen() && folder.getMode() == openMode) {
                    return folder;
                }
                closeFolder(false);
            }

            folder = store.getFolder(mailBoxFolder);
            folder.open(openMode);

        } catch (MessagingException e) {
            throw new EmailConnectionException(format("Error while opening folder : %s. %s", mailBoxFolder,
                    e.getMessage()), e);
        }
        return folder;
    }

    /**
     * Closes the current connection folder.
     *
     * @param expunge whether to remove all the emails marked as DELETED.
     */
    public synchronized void closeFolder(boolean expunge) throws EmailConnectionException {

        try {
            if (log.isDebugEnabled()) {
                log.debug(format("Closing folder: %s ...", this.folder.getFullName()));
            }
            if (folder != null && folder.isOpen()) {
                folder.close(expunge);
            }
        } catch (MessagingException e) {
            throw new EmailConnectionException(format("Error occurred while closing folder: %s. %s",
                    this.folder.getFullName(), e.getMessage()), e);
        }
    }

    /**
     * Checks if a mailBoxFolder name is the same name as the current folder.
     *
     * @param mailBoxFolder the name of the folder
     * @return true if is the same folder, false otherwise.
     */
    private boolean isCurrentFolder(String mailBoxFolder) {

        return folder.getName() != null && folder.getName().equalsIgnoreCase(mailBoxFolder);
    }

    /**
     * Closes the folder and the store
     */
    synchronized void disconnect() {

        try {
            closeFolder(false);
        } catch (Exception e) {
            log.error(format("Error closing mailbox folder %s when disconnecting. %s", folder.getName(),
                    e.getMessage()), e);
        } finally {
            try {
                store.close();
            } catch (Exception e) {
                log.error(format("Error closing store when disconnecting. %s", e.getMessage()), e);
            }
        }
    }

    /**
     * Checks if the store is connection is active
     *
     * @return true if the connection is active, false otherwise
     */
    boolean isConnected() {

        return store.isConnected();
    }
}
