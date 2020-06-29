package org.wso2.carbon.connector.integration.test.email.utils;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static javax.mail.Part.INLINE;

/**
 * Handles the greenmail server
 */
public class GreenMailServer {

    private static GreenMailServer greenMailServer;
    private GreenMail greenMail;
    private GreenMailUser sender;

    private GreenMailServer() {

        this.greenMail = new GreenMail();
    }

    public static synchronized GreenMailServer getInstance() {

        if (greenMailServer == null) {
            greenMailServer = new GreenMailServer();
        }
        return greenMailServer;
    }

    /**
     * Start the server
     */
    public void startServer() {

        greenMail.start();
        // set default user
        greenMail.setUser("wso2@localhost", "wso2", "wso2");
        sender = greenMail.setUser("wso2test@localhost", "wso2test", "wso2test");
    }

    /**
     * Stop the server
     */
    public void stopServer() {

        greenMail.stop();
    }

    /**
     * Retrieve all received messages
     *
     * @return
     */
    public MimeMessage[] getReceivedMessages() {

        return greenMail.getReceivedMessages();
    }

    /**
     * Sends an email
     *
     * @param subject      subject of the email
     * @param from         sender
     * @param toAddresses  to addresses
     * @param ccAddresses  cc addresses
     * @param bccAddresses bcc addresses
     * @param content      content of the email
     * @param protocol     protocol
     * @throws Exception if failed to send the email
     */
    public void sendEmail(String subject, String from, String toAddresses, String ccAddresses,
                          String bccAddresses, String content, String protocol) throws Exception {

        MimeMessage message = createMimeMessage(subject, from, toAddresses, ccAddresses, bccAddresses, content,
                protocol);
        sender.deliver(message);
    }

    /**
     * Creates a message
     *
     * @param subject      subject of the email
     * @param from         sender
     * @param toAddresses  to addresses
     * @param ccAddresses  cc addresses
     * @param bccAddresses bcc addresses
     * @param content      content of the email
     * @param protocol     protocol
     * @return
     * @throws Exception if failed to create message
     */
    private MimeMessage createMimeMessage(String subject, String from, String toAddresses, String ccAddresses,
                                          String bccAddresses, String content, String protocol)
            throws Exception {

        ServerSetup setup = null;
        if (protocol.equals("imap")) {
            setup = greenMail.getImap().getServerSetup();
        } else if (protocol.equals("pop3")) {
            setup = greenMail.getPop3().getServerSetup();
        }
        Session session = GreenMailUtil.getSession(setup);
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setSubject(subject);
        mimeMessage.setSentDate(new Date());
        mimeMessage.setFrom(InternetAddress.parse(from)[0]);
        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddresses));
        if (!StringUtils.isEmpty(ccAddresses)) {
            mimeMessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddresses));
        }
        if (!StringUtils.isEmpty(bccAddresses)) {
            mimeMessage.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bccAddresses));
        }
        mimeMessage.setDisposition(INLINE);
        mimeMessage.setContent(content, "text/html; charset=utf-8");

        return mimeMessage;
    }

    /**
     * Clears all mails in server
     */
    public void clear() throws Exception {

        greenMail.purgeEmailFromAllMailboxes();
    }
}
