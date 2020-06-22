package org.wso2.carbon.connector.integration.test.email.utils;

import static java.lang.String.format;

/**
 * Common operations used in email tests
 */
public final class EmailTestUtils {

    public static final class Constants {
        public static final String SUBJECT = "This is the subject";
        public static final String TO = "wso2test@localhost";
        public static final String CC = "wso2test1@localhost,wso2test2@localhost";
        public static final String BCC = "wso2test3@localhost,wso2test4@localhost";
        public static final String FROM = "wso2@localhost";
        public static final String HTML_CONTENT = "<h1>Hello WSO2.....!</h1>";
        public static final String SUBJECT_LOG = format("subject = %s", SUBJECT);
        public static final String TO_LOG = format("to = %s", TO);
        public static final String FROM_LOG = format("from = %s", FROM);
        public static final String CC_LOG = format("cc = %s", CC);
        public static final String BCC_LOG = format("bcc = %s", BCC);
        public static final String HTML_CONTENT_LOG = format("htmlContent = %s", HTML_CONTENT);

        // Protocol
        public static final String PROTOCOL_IMAP = "imap";
        public static final String PROTOCOL_POP3 = "pop3";
    }

    /**
     * Sends a text email
     *
     * @throws Exception
     */
    public static void sendSampleEmail(String protocol) throws Exception {

        GreenMailServer.getInstance().sendEmail(Constants.SUBJECT, Constants.FROM, Constants.TO, Constants.CC,
                Constants.BCC, Constants.HTML_CONTENT, protocol);
    }

}
