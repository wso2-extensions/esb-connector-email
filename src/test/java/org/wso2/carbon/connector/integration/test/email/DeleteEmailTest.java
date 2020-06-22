package org.wso2.carbon.connector.integration.test.email;

import com.google.gson.JsonObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.connector.integration.test.email.utils.EmailTestUtils;
import org.wso2.carbon.connector.integration.test.email.utils.GreenMailServer;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;
import org.wso2.esb.integration.common.utils.Utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.MimeMessage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test class for Delete email operation
 */
public class DeleteEmailTest extends ConnectorIntegrationTestBase {

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

    @Test(enabled = true, groups = {"wso2.ei"}, description = "Test deleting email")
    public void testDeleteEmail() throws Exception {

        GreenMailServer.getInstance().clear();
        EmailTestUtils.sendSampleEmail(EmailTestUtils.Constants.PROTOCOL_IMAP);
        MimeMessage[] messages = GreenMailServer.getInstance().getReceivedMessages();
        MimeMessage message = messages[0];
        JsonObject payload = new JsonObject();
        payload.addProperty("emailID", message.getMessageID());
        payload.addProperty("folder", "INBOX");
        HttpResponse response = HttpRequestUtil.doPost(new URL(getProxyServiceURLHttp("DeleteEmailProxy")),
                payload.toString(), esbRequestHeadersMap);
        assertEquals(response.getData(), "{\"result\":{\"success\":true}}",
                "Email is not deleted.");
        MimeMessage[] newMessages = GreenMailServer.getInstance().getReceivedMessages();
        assertEquals(newMessages.length, 0);
    }

    @Test(enabled = true, groups = {"wso2.ei"}, description = "Test deleting email without specifying folder")
    public void testDeleteEmailDefaultFolder() throws Exception {

        GreenMailServer.getInstance().clear();
        EmailTestUtils.sendSampleEmail(EmailTestUtils.Constants.PROTOCOL_IMAP);
        MimeMessage[] messages = GreenMailServer.getInstance().getReceivedMessages();
        MimeMessage message = messages[0];
        JsonObject payload = new JsonObject();
        payload.addProperty("emailID", message.getMessageID());
        HttpResponse response = HttpRequestUtil.doPost(new URL(getProxyServiceURLHttp("DeleteEmailProxy")),
                payload.toString(), esbRequestHeadersMap);
        assertEquals(response.getData(), "{\"result\":{\"success\":true}}",
                "Email is not deleted.");
        MimeMessage[] newMessages = GreenMailServer.getInstance().getReceivedMessages();
        assertEquals(newMessages.length, 0);
    }

    @Test(enabled = true, groups = {"wso2.ei"}, description = "Test deleting email without Email ID")
    public void testDeleteEmailWithoutEmailID() throws Exception {

        GreenMailServer.getInstance().clear();
        EmailTestUtils.sendSampleEmail(EmailTestUtils.Constants.PROTOCOL_IMAP);
        sendJsonRestRequest(getProxyServiceURLHttp("DeleteEmailProxy"), "POST",
                esbRequestHeadersMap, null);
        assertTrue(Utils.checkForLog(logViewerClient,
                "ERROR_CODE = 700204, ERROR_MESSAGE = EMAIL:INVALID_CONFIGURATION", 10000));
    }

    @AfterClass(alwaysRun = true)
    public void deleteService() throws Exception {

        GreenMailServer.getInstance().clear();
    }
}
