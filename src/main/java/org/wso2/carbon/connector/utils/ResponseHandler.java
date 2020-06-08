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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.wso2.carbon.connector.exception.ContentBuilderException;
import org.wso2.carbon.connector.pojo.Attachment;
import org.wso2.carbon.connector.pojo.EmailMessage;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static java.lang.String.format;

/**
 * Generates responses
 */
public final class ResponseHandler {

    private static final QName EMAILS_ELEMENT = new QName("emails");
    private static final QName EMAIL_ELEMENT = new QName("email");
    private static final QName ATTACHMENTS_ELEMENT = new QName("attachments");
    private static final QName ATTACHMENT_ELEMENT = new QName("attachment");
    private static final QName INDEX_ELEMENT = new QName("index");
    private static final QName EMAIL_ID_ELEMENT = new QName("emailID");
    private static final QName EMAIL_TO_ELEMENT = new QName("to");
    private static final QName EMAIL_FROM_ELEMENT = new QName("from");
    private static final QName EMAIL_CC_ELEMENT = new QName("cc");
    private static final QName EMAIL_BCC_ELEMENT = new QName("bcc");
    private static final QName EMAIL_REPLY_TO_ELEMENT = new QName("replyTo");
    private static final QName EMAIL_SUBJECT_ELEMENT = new QName("subject");
    private static final QName ATTACHMENT_CONTENT_TYPE = new QName("contentType");
    private static final QName ATTACHMENT_NAME = new QName("name");

    // Response constants
    private static final String START_TAG = "<result><success>";
    private static final String END_TAG = "</success></result>";

    private ResponseHandler() {

    }

    /**
     * Generates the output payload with result status
     *
     * @param messageContext The message context that is processed
     * @param resultStatus   Result of the status
     */
    public static void generateOutput(MessageContext messageContext, boolean resultStatus)
            throws ContentBuilderException {

        String response = START_TAG + resultStatus + END_TAG;
        ResponseHandler.preparePayload(messageContext, response);
    }

    /**
     * Sets payload in body
     *
     * @param messageContext The message context that is processed
     * @param output         Output response
     */
    private static void preparePayload(MessageContext messageContext, String output) throws ContentBuilderException {

        OMElement element;
        try {
            if (StringUtils.isNotEmpty(output)) {
                element = AXIOMUtil.stringToOM(output);
            } else {
                element = AXIOMUtil.stringToOM("<result></></result>");
            }
            SOAPBody soapBody = messageContext.getEnvelope().getBody();
            for (Iterator itr = soapBody.getChildElements(); itr.hasNext(); ) {
                OMElement child = (OMElement) itr.next();
                child.detach();
            }
            soapBody.addChild(element);
            messageContext.setResponse(true);
        } catch (XMLStreamException e) {
            throw new ContentBuilderException(format("Failed to set response in payload. %s", e.getMessage()), e);
        }
    }

    /**
     * Sets email response in body
     *
     * @param emailMessages  List of emails
     * @param messageContext The message context that is processed
     */
    public static void setEmailListResponse(List<EmailMessage> emailMessages, MessageContext messageContext)
            throws ContentBuilderException {

        org.apache.axis2.context.MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.
                Axis2MessageContext) messageContext).getAxis2MessageContext();

        SOAPFactory factory = OMAbstractFactory.getSOAP12Factory();
        OMElement emailsElement = factory.createOMElement(EMAILS_ELEMENT);
        for (int i = 0; i < emailMessages.size(); i++) {
            EmailMessage emailMessage = emailMessages.get(i);
            OMElement emailElement = factory.createOMElement(EMAIL_ELEMENT);
            addTextElement(factory, emailElement, INDEX_ELEMENT, Integer.toString(i));
            addTextElement(factory, emailElement, EMAIL_ID_ELEMENT, emailMessage.getEmailId());
            addTextElement(factory, emailElement, EMAIL_TO_ELEMENT, emailMessage.getTo());
            addTextElement(factory, emailElement, EMAIL_FROM_ELEMENT, emailMessage.getFrom());
            addTextElement(factory, emailElement, EMAIL_CC_ELEMENT, emailMessage.getCc());
            addTextElement(factory, emailElement, EMAIL_BCC_ELEMENT, emailMessage.getBcc());
            addTextElement(factory, emailElement, EMAIL_REPLY_TO_ELEMENT, emailMessage.getReplyTo());
            addTextElement(factory, emailElement, EMAIL_SUBJECT_ELEMENT, emailMessage.getSubject());
            OMElement attachmentsElement = factory.createOMElement(ATTACHMENTS_ELEMENT);
            for (int j = 0; j < emailMessage.getAttachments().size(); j++) {
                Attachment attachment = emailMessage.getAttachments().get(j);
                OMElement attachmentElement = factory.createOMElement(ATTACHMENT_ELEMENT);
                addTextElement(factory, attachmentElement, INDEX_ELEMENT, Integer.toString(j));
                addTextElement(factory, attachmentElement, ATTACHMENT_NAME, attachment.getName());
                addTextElement(factory, attachmentElement, ATTACHMENT_CONTENT_TYPE, attachment.getContentType());
                attachmentsElement.addChild(attachmentElement);
            }
            emailElement.addChild(attachmentsElement);
            emailsElement.addChild(emailElement);
        }
        setPayloadInEnvelope(axis2MsgCtx, emailsElement);
        handleSpecialProperties(ContentTypes.APPLICATION_XML, axis2MsgCtx);
    }

    /**
     * Adds text element to parent OMElement
     *
     * @param factory SOAP factory to create element
     * @param parent Parent OMElement
     * @param qName QName of the new text element
     * @param value Value of the new text element
     */
    private static void addTextElement(SOAPFactory factory, OMElement parent, QName qName, String value) {
        if (!StringUtils.isEmpty(value)) {
            OMElement newElement = factory.createOMElement(qName);
            newElement.addChild(factory.createOMText(value));
            parent.addChild(newElement);
        }
    }

    /**
     * Overrides the payload in message body
     *
     * @param axis2MsgCtx Axis2MessageContext
     * @param payload     Payload to be set in the body
     * @throws ContentBuilderException if failed to set payload
     */
    public static void setPayloadInEnvelope(org.apache.axis2.context.MessageContext axis2MsgCtx, OMElement payload) throws ContentBuilderException {

        JsonUtil.removeJsonPayload(axis2MsgCtx);
        try {
            axis2MsgCtx.setEnvelope(TransportUtils.createSOAPEnvelope(payload));
        } catch (AxisFault e) {
            throw new ContentBuilderException(format("Failed to set XML content. %s", e.getMessage()), e);
        }
    }

    /**
     * Changes the content type and handles other headers
     *
     * @param resultValue Value to be set in header
     * @param axis2MessageCtx Axis2 Message Context
     */
    public static void handleSpecialProperties(Object resultValue,
                                         org.apache.axis2.context.MessageContext axis2MessageCtx) {
        axis2MessageCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, resultValue);
        axis2MessageCtx.setProperty(Constants.Configuration.CONTENT_TYPE, resultValue);
        Object o = axis2MessageCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Map headers = (Map) o;
        if (headers != null) {
            headers.remove(HTTP.CONTENT_TYPE);
            headers.put(HTTP.CONTENT_TYPE, resultValue);
        }
    }

    /**
     * Sets the error code and error detail in message
     *
     * @param messageContext Message Context
     * @param error          Error to be set
     */
    public static void setErrorsInMessage(MessageContext messageContext, Error error) {

        messageContext.setProperty(EmailPropertyNames.PROPERTY_ERROR_CODE, error.getErrorCode());
        messageContext.setProperty(EmailPropertyNames.PROPERTY_ERROR_MESSAGE, error.getErrorDetail());
    }

}
