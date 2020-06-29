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
package org.wso2.carbon.connector.pojo;

/**
 * Configuration parameters used to connect to a mailbox folder in the email server
 */
public class MailboxConfiguration {

    private String folder;
    private boolean deleteAfterRetrieve;
    private boolean seen;
    private boolean answered;
    private boolean recent;
    private boolean deleted;
    private String receivedSince;
    private String receivedUntil;
    private String sentSince;
    private String sentUntil;
    private String subjectRegex;
    private String fromRegex;
    private int offset;
    private int limit;

    public String getFolder() {

        return folder;
    }

    public void setFolder(String folder) {

        this.folder = folder;
    }

    public boolean getDeleteAfterRetrieve() {

        return deleteAfterRetrieve;
    }

    public void setDeleteAfterRetrieve(boolean deleteAfterRetrieve) {

        this.deleteAfterRetrieve = deleteAfterRetrieve;
    }

    public boolean getSeen() {

        return seen;
    }

    public void setSeen(boolean seen) {

        this.seen = seen;
    }

    public boolean getAnswered() {

        return answered;
    }

    public void setAnswered(boolean answered) {

        this.answered = answered;
    }

    public boolean getRecent() {

        return recent;
    }

    public void setRecent(boolean recent) {

        this.recent = recent;
    }

    public boolean getDeleted() {

        return deleted;
    }

    public void setDeleted(boolean deleted) {

        this.deleted = deleted;
    }

    public String getReceivedSince() {

        return receivedSince;
    }

    public void setReceivedSince(String receivedSince) {

        this.receivedSince = receivedSince;
    }

    public String getReceivedUntil() {

        return receivedUntil;
    }

    public void setReceivedUntil(String receivedUntil) {

        this.receivedUntil = receivedUntil;
    }

    public String getSentSince() {

        return sentSince;
    }

    public void setSentSince(String sentSince) {

        this.sentSince = sentSince;
    }

    public String getSentUntil() {

        return sentUntil;
    }

    public void setSentUntil(String sentUntil) {

        this.sentUntil = sentUntil;
    }

    public String getSubjectRegex() {

        return subjectRegex;
    }

    public void setSubjectRegex(String subjectRegex) {

        this.subjectRegex = subjectRegex;
    }

    public String getFromRegex() {

        return fromRegex;
    }

    public void setFromRegex(String fromRegex) {

        this.fromRegex = fromRegex;
    }

    public int getOffset() {

        return offset;
    }

    public void setOffset(int offset) {

        this.offset = offset;
    }

    public int getLimit() {

        return limit;
    }

    public void setLimit(int limit) {

        this.limit = limit;
    }
}
