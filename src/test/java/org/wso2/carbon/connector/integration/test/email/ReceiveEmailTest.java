package org.wso2.carbon.connector.integration.test.email;

import com.google.gson.JsonObject;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.connector.integration.test.email.utils.EmailTestUtils;
import org.wso2.carbon.connector.integration.test.email.utils.GreenMailServer;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;
import org.wso2.connector.integration.test.base.RestResponse;
import org.wso2.esb.integration.common.utils.Utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test class for list email operation
 */
public class ReceiveEmailTest extends ConnectorIntegrationTestBase {

    private static LogViewerClient logViewerClient;
    private Map<String, String> esbRequestHeadersMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        String connectorName = System.getProperty("connector_name") + "-connector-" +
                System.getProperty("connector_version") + ".zip";
        init(connectorName);
        getApiConfigProperties();
        logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
        esbRequestHeadersMap.put("Accept-Charset", "UTF-8");
        esbRequestHeadersMap.put("Content-Type", "application/json");
        esbRequestHeadersMap.put("Accept", "application/json");
    }

    @Test(enabled = true, groups = {"wso2.ei"}, description = "Test email receive operation using imap")
    public void testEmailReceiveUsingIMAP() throws Exception {

        GreenMailServer.getInstance().clear();
        EmailTestUtils.sendSampleEmail(EmailTestUtils.Constants.PROTOCOL_IMAP);
        RestResponse<JSONObject> eiRestResponse = sendJsonRestRequest(
                getProxyServiceURLHttp("ReceiveEmailProxy"), "POST",
                esbRequestHeadersMap, "receiveEmail.json");
        JSONObject emails = eiRestResponse.getBody().getJSONObject("emails");
        assertNotNull(emails, "No emails retrieved.");
        JSONObject email = emails.getJSONObject("email");
        assertEquals(email.getString("subject"), EmailTestUtils.Constants.SUBJECT);
        assertEquals(email.getString("to"), EmailTestUtils.Constants.TO);
        assertEquals(email.getString("from"), EmailTestUtils.Constants.FROM);
    }

    @Test(enabled = true, groups = {"wso2.ei"},
            description = "Test retrieving email metadata and content in the email body")
    public void testEmailRetrieveBodyUsingIMAP() throws Exception {

        GreenMailServer.getInstance().clear();
        logViewerClient.clearLogs();
        EmailTestUtils.sendSampleEmail(EmailTestUtils.Constants.PROTOCOL_IMAP);
        sendJsonRestRequest(getProxyServiceURLHttp("ReceiveEmailBodyProxy"), "POST",
                esbRequestHeadersMap, "receiveEmail.json");
        assertTrue(Utils.checkForLog(logViewerClient, EmailTestUtils.Constants.TO_LOG, 10000));
        assertTrue(Utils.checkForLog(logViewerClient, EmailTestUtils.Constants.SUBJECT_LOG, 10000));
        assertTrue(Utils.checkForLog(logViewerClient, EmailTestUtils.Constants.FROM_LOG, 10000));
        assertTrue(Utils.checkForLog(logViewerClient, EmailTestUtils.Constants.CC_LOG, 10000));
        assertTrue(Utils.checkForLog(logViewerClient, EmailTestUtils.Constants.BCC_LOG, 10000));
        assertTrue(Utils.checkForLog(logViewerClient, EmailTestUtils.Constants.HTML_CONTENT_LOG, 10000));
    }

    @Test(enabled = true, groups = {"wso2.ei"},
            description = "Test retrieving multiple email bodies")
    public void testEmailRetrieveMultipleEmailBodiesUsingIMAP() throws Exception {

        GreenMailServer.getInstance().clear();
        logViewerClient.clearLogs();
        // Send email 1
        EmailTestUtils.sendSampleEmail(EmailTestUtils.Constants.PROTOCOL_IMAP);

        // Send email 2
        String content = "<h1>Test email.....!</h1>";
        String subject = "subject 2";
        GreenMailServer.getInstance().sendEmail(subject, EmailTestUtils.Constants.FROM,
                EmailTestUtils.Constants.TO, EmailTestUtils.Constants.CC, EmailTestUtils.Constants.BCC,
                content, EmailTestUtils.Constants.PROTOCOL_IMAP);
        sendJsonRestRequest(getProxyServiceURLHttp("MultipleEmailsProxy"), "POST",
                esbRequestHeadersMap, "receiveEmail.json");

        // Check email 1 content
        assertTrue(Utils.checkForLog(logViewerClient, EmailTestUtils.Constants.HTML_CONTENT_LOG, 10000));
        assertTrue(Utils.checkForLog(logViewerClient, EmailTestUtils.Constants.SUBJECT_LOG, 10000));

        // Check email 2 content
        assertTrue(Utils.checkForLog(logViewerClient, format("htmlContent = %s", content), 10000));
        assertTrue(Utils.checkForLog(logViewerClient, format("subject = %s", subject), 10000));
    }

    @Test(enabled = true, groups = {"wso2.ei"},
            description = "Test transform content in attachment of an email")
    public void testEmailMediateAttachment() throws Exception {

        GreenMailServer.getInstance().clear();
        logViewerClient.clearLogs();
        JsonObject payload = new JsonObject();
        payload.addProperty("from", EmailTestUtils.Constants.FROM);
        payload.addProperty("to", EmailTestUtils.Constants.TO);
        payload.addProperty("subject", EmailTestUtils.Constants.SUBJECT);
        payload.addProperty("content", "Hello WSO2.....!");
        payload.addProperty("attachments", pathToResourcesDirectory + "contacts.csv");
        // Send email with attachment
        HttpRequestUtil.doPost(new URL(getProxyServiceURLHttp("SendEmailProxy")), payload.toString(),
                esbRequestHeadersMap);
        // Receive email
        sendJsonRestRequest(getProxyServiceURLHttp("TransformAttachmentProxy"), "POST",
                esbRequestHeadersMap, "receiveEmail.json");
        assertTrue(Utils.checkForLog(logViewerClient, "{\"emails\":[{\"id\":\"1\",\"firstName\":\"John1\"," +
                "\"lastName\":\"Doe\",\"tpNumber\":\"096548763\",\"email\":\"john1.doe@texasComp.com\"}," +
                "{\"id\":\"2\",\"firstName\":\"Jane2\",\"lastName\":\"Doe\",\"tpNumber\":\"091558780\"," +
                "\"email\":\"jane2.doe@texasComp.com\"}]}", 10000));
    }

    @Test(enabled = true, groups = {"wso2.ei"}, description = "Test email receive operation using pop3")
    public void testEmailReceiveUsingPOP3() throws Exception {

        GreenMailServer.getInstance().clear();
        EmailTestUtils.sendSampleEmail(EmailTestUtils.Constants.PROTOCOL_POP3);
        RestResponse<JSONObject> eiRestResponse = sendJsonRestRequest(
                getProxyServiceURLHttp("ReceiveEmailPOP3Proxy"), "POST",
                esbRequestHeadersMap, "receivePop3Email.json");
        JSONObject emails = eiRestResponse.getBody().getJSONObject("emails");
        assertNotNull(emails, "No emails retrieved.");
        JSONObject email = emails.getJSONObject("email");
        assertEquals(email.getString("subject"), EmailTestUtils.Constants.SUBJECT);
        assertEquals(email.getString("to"), EmailTestUtils.Constants.TO);
        assertEquals(email.getString("from"), EmailTestUtils.Constants.FROM);
    }

    @Test(enabled = true, groups = {"wso2.ei"}, description = "Test retrieving emails periodically")
    public void testCheckingForEmailPeriodically() throws Exception {

        GreenMailServer.getInstance().clear();
        OMElement task = AXIOMUtil.stringToOM("<task:task xmlns:task=\"http://www.wso2.org/products/wso2commons/tasks\"\n" +
                "           name=\"EmailSequenceTask\"\n" +
                "           class=\"org.apache.synapse.startup.tasks.MessageInjector\" group=\"synapse.simple.quartz\">\n" +
                "    <task:trigger count=\"1\" interval=\"5\"/>\n" +
                "    <task:property name=\"proxyName\" value=\"EmailTaskProxy\"/>\n" +
                "    <task:property name=\"message\">\n" +
                "        <test>Hello World</test>\n" +
                "    </task:property>\n" +
                "    <task:property name=\"injectTo\" value=\"proxy\"/>\n" +
                "</task:task>");

        logViewerClient.clearLogs();
        addScheduledTask(task);
        EmailTestUtils.sendSampleEmail(EmailTestUtils.Constants.PROTOCOL_IMAP);
        TimeUnit.SECONDS.sleep(10);
        assertTrue(Utils.checkForLog(logViewerClient, "<index>0</index>", 10000));
    }

    @AfterClass(alwaysRun = true)
    public void deleteService() throws Exception {

        GreenMailServer.getInstance().clear();
    }

}
