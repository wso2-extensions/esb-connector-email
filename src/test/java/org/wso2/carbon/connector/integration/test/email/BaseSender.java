package org.wso2.carbon.connector.integration.test.email;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.connector.integration.test.email.utils.GreenMailServer;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;

/**
 * Base class to start and stop GreenMail server
 */
public class BaseSender extends ConnectorIntegrationTestBase {

    @BeforeTest(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        GreenMailServer.getInstance().startServer();
    }

    @AfterTest(alwaysRun = true)
    public void cleanUp() throws Exception {
        GreenMailServer.getInstance().stopServer();
    }
}
