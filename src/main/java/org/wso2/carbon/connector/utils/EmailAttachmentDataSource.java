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
package org.wso2.carbon.connector.utils;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;

/**
 * Email Attachment Data Source
 */
public class EmailAttachmentDataSource implements DataSource {

    private final String name;
    private final InputStream content;
    private final String contentType;

    EmailAttachmentDataSource(String name, InputStream content, String contentType) throws IOException {
        this.name = name;
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        IOUtils.copy(content, bs);
        this.content = new ByteArrayInputStream(bs.toByteArray());
        this.contentType = contentType;
    }

    @Override
    public InputStream getInputStream() {
        return content;
    }

    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException(EmailAttachmentDataSource.class.getName() + " does not provide an " +
                "OutputStream");
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }
}
