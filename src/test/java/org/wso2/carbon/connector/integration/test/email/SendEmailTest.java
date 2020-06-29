package org.wso2.carbon.connector.integration.test.email;

import com.google.gson.JsonObject;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.connector.integration.test.email.utils.EmailTestUtils;
import org.wso2.carbon.connector.integration.test.email.utils.GreenMailServer;
import org.wso2.carbon.connector.pojo.EmailMessage;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;
import org.wso2.connector.integration.test.base.RestResponse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.testng.Assert.assertEquals;

/**
 * Test class for send email operation
 */
public class SendEmailTest extends ConnectorIntegrationTestBase {

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

    @Test(enabled = true, groups = {"wso2.ei"}, description = "Test email send operation")
    public void testEmailSend() throws Exception {

        GreenMailServer.getInstance().clear();
        RestResponse<JSONObject> eiRestResponse = sendJsonRestRequest(
                getProxyServiceURLHttp("SendEmailProxy"), "POST",
                esbRequestHeadersMap, "sendEmail.json");
        assertEquals(eiRestResponse.getBody().getJSONObject("result").getString("success"), "true",
                "Email send is not successful.");
        MimeMessage[] messages = GreenMailServer.getInstance().getReceivedMessages();
        assertEquals(messages.length, 1, "Email not received by server.");
        MimeMessage message = messages[0];
        assertEquals(message.getSubject(), EmailTestUtils.Constants.SUBJECT, "Incorrect email subject.");
        assertEquals(message.getAllRecipients()[0].toString(), EmailTestUtils.Constants.TO,
                "Incorrect email recipient.");
        assertEquals(message.getFrom()[0].toString(), EmailTestUtils.Constants.FROM, "Incorrect email sender.");
    }

    @Test(enabled = true, groups = {"wso2.ei"}, description = "Test email send operation with multiple recipients")
    public void testEmailSendWithMultipleRecipients() throws Exception {

        GreenMailServer.getInstance().clear();
        RestResponse<JSONObject> eiRestResponse = sendJsonRestRequest(
                getProxyServiceURLHttp("SendEmailProxy"), "POST",
                esbRequestHeadersMap, "sendEmailMultipleRecipients.json");
        assertEquals(eiRestResponse.getBody().getJSONObject("result").getString("success"), "true",
                "Email send is not successful.");
        MimeMessage[] messages = GreenMailServer.getInstance().getReceivedMessages();
        MimeMessage message = messages[0];
        assertEquals(InternetAddress.toString(message.getAllRecipients()),
                "wso2test@localhost, wso2test2@localhost", "Incorrect email recipient.");
    }

    @Test(enabled = true, groups = {"wso2.ei"}, description = "Test email send operation for emails with attachments")
    public void testEmailSendWithAttachments() throws Exception {

        GreenMailServer.getInstance().clear();
        JsonObject payload = new JsonObject();
        payload.addProperty("from", EmailTestUtils.Constants.FROM);
        payload.addProperty("to", EmailTestUtils.Constants.TO);
        payload.addProperty("subject", EmailTestUtils.Constants.SUBJECT);
        payload.addProperty("content", "Hello WSO2.....!");
        payload.addProperty("attachments", pathToResourcesDirectory + "contacts.csv");
        HttpResponse response = HttpRequestUtil.doPost(new URL(getProxyServiceURLHttp("SendEmailProxy")),
                payload.toString(), esbRequestHeadersMap);

        assertEquals(response.getData(), "{\"result\":{\"success\":true}}",
                "Email is not sent.");

        MimeMessage[] messages = GreenMailServer.getInstance().getReceivedMessages();
        assertEquals(messages.length, 1, "Mail not received");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(javax.mail.Message.class.getClassLoader());
            EmailMessage parsedEmail = new EmailMessage(messages[0]);
            assertEquals(parsedEmail.getAttachments().size(), 1);
            assertEquals(parsedEmail.getSubject(), EmailTestUtils.Constants.SUBJECT, "Incorrect email subject.");
            assertEquals(parsedEmail.getTo(), EmailTestUtils.Constants.TO, "Incorrect email recipient.");
            assertEquals(parsedEmail.getFrom(), EmailTestUtils.Constants.FROM, "Incorrect email sender.");
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    @AfterClass(alwaysRun = true)
    public void deleteService() throws Exception {

        GreenMailServer.getInstance().clear();
    }

}
