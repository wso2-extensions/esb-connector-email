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
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayConstants;
import org.apache.synapse.transport.passthru.util.StreamingOnRequestDataSource;
import org.wso2.carbon.connector.exception.ContentBuilderException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static java.lang.String.format;

/**
 * Utils for building content according to their content type to the format is understandable by the mediation engine
 */
public final class ContentBuilder {

    private static final QName TEXT_ELEMENT = new QName("http://ws.apache.org/commons/ns/payload",
            "text");

    private ContentBuilder() {

    }

    /**
     * Build content according to the content type and set in the message body
     *
     * @param messageContext Current message content
     * @param inputStream    Content to be built as an input stream
     * @param contentType    Content Type of the content
     * @throws ContentBuilderException if failed to build the content
     */
    public static void buildContent(MessageContext messageContext, InputStream inputStream, String contentType)
            throws ContentBuilderException {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        try {
            if (ContentTypes.TEXT_XML.equalsIgnoreCase(contentType)
                    || ContentTypes.APPLICATION_XML.equalsIgnoreCase(contentType)) {
                setXMLContent(inputStream, axis2MessageContext);
                ResponseHandler.handleSpecialProperties(ContentTypes.APPLICATION_XML, axis2MessageContext);
            } else if (ContentTypes.APPLICATION_JSON.equalsIgnoreCase(contentType)) {
                setJSONPayload(inputStream, axis2MessageContext);
                ResponseHandler.handleSpecialProperties(ContentTypes.APPLICATION_JSON, axis2MessageContext);
            } else if (ContentTypes.TEXT_PLAIN.equalsIgnoreCase(contentType)
                    || ContentTypes.TEXT_CSV.equalsIgnoreCase(contentType)) {
                setTextContent(inputStream, axis2MessageContext);
                ResponseHandler.handleSpecialProperties(ContentTypes.TEXT_PLAIN, axis2MessageContext);
            } else {
                setBinaryContent(inputStream, axis2MessageContext);
            }
        } catch (AxisFault e) {
            throw new ContentBuilderException(format("Failed to build content. %s", e.getMessage()), e);
        }
    }

    /**
     * Builds and sets Binary content
     *
     * @param inputStream         Content as an input stream
     * @param axis2MessageContext Axis2 Message Context
     */
    private static void setBinaryContent(InputStream inputStream,
                                         org.apache.axis2.context.MessageContext axis2MessageContext) throws AxisFault {

        SOAPFactory factory = OMAbstractFactory.getSOAP12Factory();
        OMNamespace ns = factory.createOMNamespace(
                RelayConstants.BINARY_CONTENT_QNAME.getNamespaceURI(), "ns");
        OMElement element = factory.createOMElement(
                RelayConstants.BINARY_CONTENT_QNAME.getLocalPart(), ns);

        StreamingOnRequestDataSource ds = new StreamingOnRequestDataSource(inputStream);
        DataHandler dataHandler = new DataHandler(ds);

        //create an OMText node with the above DataHandler and set optimized to true
        OMText textData = factory.createOMText(dataHandler, true);
        element.addChild(textData);
        axis2MessageContext.setEnvelope(TransportUtils.createSOAPEnvelope(element));
    }

    /**
     * Builds and sets text content
     *
     * @param inputStream         Content as an input stream
     * @param axis2MessageContext Axis2 Message Context
     * @throws ContentBuilderException if failed to set text content
     */
    private static void setTextContent(InputStream inputStream,
                                       org.apache.axis2.context.MessageContext axis2MessageContext)
            throws ContentBuilderException {

        try {
            String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            ResponseHandler.setPayloadInEnvelope(axis2MessageContext, getTextElement(text));
        } catch (IOException e) {
            throw new ContentBuilderException(format("Failed to set text content. %s ", e.getMessage()), e);
        }
    }

    /**
     * Builds and sets JSON content
     *
     * @param inputStream         Content as an input stream
     * @param axis2MessageContext Axis2 Message Context
     * @throws ContentBuilderException if failed to set JSON content
     */
    private static void setJSONPayload(InputStream inputStream,
                                       org.apache.axis2.context.MessageContext axis2MessageContext)
            throws ContentBuilderException {

        try {
            String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            JsonUtil.getNewJsonPayload(axis2MessageContext, text, true, true);
        } catch (IOException e) {
            throw new ContentBuilderException(format("Failed to set JSON content. %s ", e.getMessage()), e);
        }
    }

    /**
     * Builds and sets XML content
     *
     * @param inputStream         Content as an input stream
     * @param axis2MessageContext Axis2 Message Context
     * @throws ContentBuilderException if failed to set XML content
     */
    private static void setXMLContent(InputStream inputStream,
                                      org.apache.axis2.context.MessageContext axis2MessageContext)
            throws ContentBuilderException {

        try {
            String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            OMElement omXML = AXIOMUtil.stringToOM(text);
            ResponseHandler.setPayloadInEnvelope(axis2MessageContext, omXML);
        } catch (IOException | XMLStreamException e) {
            throw new ContentBuilderException(format("Failed to set XML content. %s ", e.getMessage()), e);
        }
    }

    /**
     * Get text element
     *
     * @param content Content to be wrapped
     * @return Text Element
     */
    private static OMElement getTextElement(String content) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement textElement = factory.createOMElement(TEXT_ELEMENT);
        if (content == null) {
            content = StringUtils.EMPTY;
        }
        textElement.setText(content);
        return textElement;
    }

}
