package org.wso2.carbon.connector.integration.test.email;

import com.google.gson.JsonObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.connector.integration.test.email.utils.EmailTestUtils;
import org.wso2.carbon.connector.integration.test.email.utils.GreenMailServer;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.mail.Flags;
import javax.mail.internet.MimeMessage;

import static org.testng.Assert.assertEquals;

/**
 * Test class for Expunge folder operation
 */
public class ExpungeFolderTest extends ConnectorIntegrationTestBase {

    private Map<String, String> esbRequestHeadersMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        String connectorName = System.getProperty("connector_name") + "-connector-" +
                System.getProperty("connector_version") + ".zip";
        init(connectorName);
        getApiConfigProperties();
        esbRequestHeadersMap.put("Accept-Charset", "UTF-8");
        esbRequestHeadersMap.put("Content-Type", "application/json");
        esbRequestHeadersMap.put("Accept", "application/json");
    }

    @Test(enabled = true, groups = {"wso2.ei"}, description = "Test expunging default folder")
    public void testExpungeDefaultFolder() throws Exception {

        GreenMailServer.getInstance().clear();
        EmailTestUtils.sendSampleEmail(EmailTestUtils.Constants.PROTOCOL_IMAP);
        MimeMessage[] messages = GreenMailServer.getInstance().getReceivedMessages();
        MimeMessage message = messages[0];
        message.setFlag(Flags.Flag.DELETED, true);
        JsonObject payload = new JsonObject();
        HttpResponse response = HttpRequestUtil.doPost(new URL(getProxyServiceURLHttp("ExpungeFolderProxy")),
                payload.toString(), esbRequestHeadersMap);
        assertEquals(response.getData(), "{\"result\":{\"success\":true}}",
                "Email is not deleted.");
        MimeMessage[] newMessages = GreenMailServer.getInstance().getReceivedMessages();
        assertEquals(newMessages.length, 0);
    }

    @Test(enabled = true, groups = {"wso2.ei"}, description = "Test deleting email")
    public void testExpungeFolder() throws Exception {

        GreenMailServer.getInstance().clear();
        EmailTestUtils.sendSampleEmail(EmailTestUtils.Constants.PROTOCOL_IMAP);
        MimeMessage[] messages = GreenMailServer.getInstance().getReceivedMessages();
        MimeMessage message = messages[0];
        message.setFlag(Flags.Flag.DELETED, true);
        JsonObject payload = new JsonObject();
        payload.addProperty("folder", "INBOX");
        HttpResponse response = HttpRequestUtil.doPost(new URL(getProxyServiceURLHttp("ExpungeFolderProxy")),
                payload.toString(), esbRequestHeadersMap);
        assertEquals(response.getData(), "{\"result\":{\"success\":true}}",
                "Email is not deleted.");
        MimeMessage[] newMessages = GreenMailServer.getInstance().getReceivedMessages();
        assertEquals(newMessages.length, 0);
    }

    @AfterClass(alwaysRun = true)
    public void deleteService() throws Exception {

        GreenMailServer.getInstance().clear();
    }

}
