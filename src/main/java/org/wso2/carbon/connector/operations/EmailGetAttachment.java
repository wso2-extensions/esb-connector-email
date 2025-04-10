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
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.data.connector.ConnectorResponse;
import org.apache.synapse.data.connector.DefaultConnectorResponse;
import org.wso2.carbon.connector.connection.EmailConnectionHandler;
import org.wso2.carbon.connector.connection.MailBoxConnection;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.exception.ContentBuilderException;
import org.wso2.carbon.connector.core.util.PayloadUtils;
import org.wso2.carbon.connector.exception.EmailConnectionException;
import org.wso2.carbon.connector.exception.EmailParsingException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.pojo.Attachment;
import org.wso2.carbon.connector.pojo.EmailMessage;
import org.wso2.carbon.connector.utils.AbstractEmailConnectorOperation;
import org.wso2.carbon.connector.utils.EmailConstants;
import org.wso2.carbon.connector.utils.EmailUtils;
import org.wso2.carbon.connector.utils.Error;
import org.wso2.carbon.connector.utils.ResponseConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Retrieves an email attachment
 */
@SuppressWarnings("ALL")
public class EmailGetAttachment extends AbstractEmailConnectorOperation {

    private static final String ERROR = "Error occurred while retrieving attachment.";

    @Override
    public void execute(MessageContext messageContext, String responseVariable, 
                        Boolean overwriteBody) throws ConnectException {

        String emailId = (String) getParameter(messageContext, EmailConstants.EMAIL_ID);
        String folder = (String) getParameter(messageContext, EmailConstants.FOLDER);
        String attachmentIndex = (String) getParameter(messageContext, EmailConstants.ATTACHMENT_INDEX);
        String connectionName = null;
        EmailConnectionHandler handler = EmailConnectionHandler.getConnectionHandler();
        MailBoxConnection connection = null;
        EmailMessage emailMessage = null;
        try {
            connectionName = EmailUtils.getConnectionName(messageContext);
            connection = (MailBoxConnection) handler.getConnection(connectionName);
            emailMessage = EmailUtils.getEmail(connection, emailId, folder);

        } catch (InvalidConfigurationException e) {
            JsonObject resultJSON = generateErrorResult(messageContext, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(ERROR, e, messageContext);
        } catch (EmailParsingException e) {
            JsonObject resultJSON = generateErrorResult(messageContext, Error.RESPONSE_GENERATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(ERROR, e, messageContext);
        } catch (EmailConnectionException e) {
            JsonObject resultJSON = generateErrorResult(messageContext, Error.CONNECTIVITY);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(ERROR, e, messageContext);
        }

        if (emailId != null && attachmentIndex != null && emailMessage != null) {
            setAttachment(messageContext, attachmentIndex, emailMessage, responseVariable, overwriteBody);
        } else if (emailId == null) {
            JsonObject resultJSON = generateOperationResult(messageContext, false, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(format("%s Email Index is not set.", ERROR), messageContext);
        } else if (attachmentIndex == null) {
            JsonObject resultJSON = generateOperationResult(messageContext, false, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(format("%s Attachment Index is not set.", ERROR), messageContext);
        } else {
            JsonObject resultJSON = generateOperationResult(messageContext, false, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(format("%s Invalid email messages.", ERROR), messageContext);
        }
    }

    /**
     * Set attachment in body
     *
     * @param messageContext  Message Context
     * @param emailId      Email Index
     * @param attachmentIndex Attachment Index
     * @param emailMessages   List of emails
     * @param responseVariable The variable to store the response
     * @param overwriteBody   Whether to overwrite the body
     */
    private void setAttachment(MessageContext messageContext, String attachmentIndex,
                               EmailMessage emailMessage, String responseVariable, Boolean overwriteBody) {

        if (log.isDebugEnabled()) {
            log.debug(format("Retrieving email attachment at index %s...", attachmentIndex));
        }
        try {
            Attachment attachment = EmailUtils.getEmailAttachment(emailMessage, attachmentIndex);
            if (overwriteBody != null && overwriteBody) {
                buildRawResponse(messageContext, attachment);
            } else {
                messageContext.setVariable(responseVariable, buildJSONResponse(attachment));
            }
        } catch (InvalidConfigurationException e) {
            JsonObject resultJSON = generateErrorResult(messageContext, Error.INVALID_CONFIGURATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException(ERROR, e, messageContext);
        } catch (ContentBuilderException e) {
            JsonObject resultJSON = generateErrorResult(messageContext, Error.RESPONSE_GENERATION);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
            handleException("Error occurred during setting attachment content.", e, messageContext);
        }
    }

    private ConnectorResponse buildJSONResponse(Attachment attachment) throws ContentBuilderException {
        ConnectorResponse response = new DefaultConnectorResponse();

        // Create result JSON with success status
        JsonObject payload = new JsonObject();

        // Attributes to be set in the response
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(ResponseConstants.ATTACHMENT_NAME, attachment.getName());
        attributes.put(ResponseConstants.ATTACHMENT_TYPE, attachment.getContentType());

        // Convert attachment content (InputStream) to Base64 string for JSON
        String base64Content = EmailUtils.convertInputStreamToBase64(attachment.getContent());
        payload.addProperty(ResponseConstants.BASE64_ENCODED, base64Content);

        // Setting the base64 encoded attachment content to the response
        response.setPayload(payload);
        response.setHeaders(new HashMap<>());
        response.setAttributes(attributes);
        return response;
    }

    private void buildRawResponse(MessageContext messageContext, Attachment attachment) throws ContentBuilderException {
        // Setting the RAW attachment content to the message context
        org.apache.axis2.context.MessageContext axisMsgCtx = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        PayloadUtils.setContent(axisMsgCtx, attachment.getContent(), attachment.getContentType());
        axisMsgCtx.setProperty("ContentType",attachment.getContentType());
        axisMsgCtx.setProperty("messageType",attachment.getContentType());
        Map<String, Object> headers = (Map<String, Object>)axisMsgCtx.getProperty("TRANSPORT_HEADERS");
        headers.put("Content-Type",attachment.getContentType());
    }
}

