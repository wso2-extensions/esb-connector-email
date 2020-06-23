# List Emails

[[Overview]](#overview)  [[Operations]](#operations)  [[Sample configuration]](#sample-configuration)

### Overview 

The following operations allow you to list emails and retrieve the email body and their attachments.

| Operation | Description |
| ------------- |-------------|
|[List emails using IMAP](#list-emails-using-imap)|Retrieve the emails using IMAP protocol. |
|[List emails using POP3](#list-emails-using-pop3)|Retrieve the emails using POP3 protocol.|
|[Get email body](#get-email-body)|Retrieve email content of a certain email.|
|[Get email attachments](#get-email-attachments)|Retrieve attachment content of a certain email.|

## Operations

This section provides more details on each of the operations.

### List emails using IMAP
We can use `list` operation to retrieve the emails using IMAP protocol / IMAPS which is the secured protocol.

> **NOTE:** To configure the IMAP / IMAPS as the protocol connection, IMAP / IMAPS should be specified as the `connectionType` when initializing the connection as below.

```xml
<email.connection>
    <host></host>
    <port></port>
    <connectionType>IMAP</connectionType>
    <name>imapconnection</name>
    <username></username>
    <password></password>
</email.connection>
``` 

**List email**

When calling the `list` operation the value configured for `name` parameter in the connection configuration (in the local entry configuration) should be added as the `configKey` attribute in the operation as below.
```xml
<email.list configKey="imapconnection">
    <subjectRegex>{json-eval($.subjectRegex)}</subjectRegex>
    <seen>{json-eval($.seen)}</seen>
    <answered>{json-eval($.answered)}</answered>
    <deleted>{json-eval($.deleted)}</deleted>
    <recent>{json-eval($.recent)}</recent>
    <offset>{json-eval($.offset)}</offset>
    <limit>{json-eval($.limit)}</limit>
    <folder>{json-eval($.folder)}</folder>
</email.list>
```

**Parameters**

* deleteAfterRetrieve [optional]: Whether the email should be deleted after retrieving.
* receivedSince [optional]: The date after which to retrieve received emails.
* receivedUntil [optional]: The date until which to retrieve received emails.
* sentSince [optional]: The date after which to retrieve sent emails.
* sentUntil [optional]: The date until which to retrieve sent emails.
* subjectRegex [optional]: Subject Regex to match with the wanted emails.
* fromRegex [optional]: From email address to match with the wanted emails.
* seen [optional]: Whether to retrieve 'seen' or 'not seen' emails.
* answered [optional]: Whether to retrieve 'answered' or 'unanswered' emails.
* deleted [optional]: Whether to retrieve 'deleted' or 'not deleted' emails.
* recent [optional]: Whether to retrieve 'recent' or 'past' emails.
* offset [optional]: The index from which to retrieve emails.
* limit [optional]: The number of emails to be retrieved.
* folder [optional]: Name of the Mailbox folder to retrieve emails from. Default is `INBOX`.

**Sample request**

Following is a sample request that can be handled by the List email operation.

```json
{
	"subjectRegex":"This is the subject",
	"seen": "false",
	"answered":"false",
	"deleted":"false",
	"recent":"false",
	"offset":"0",
	"limit":"2",
	"folder":"INBOX"
}
```

**Sample response**

Given below is a sample response for the List email operation.

```json
{
   "emails": [
       {
          "index": "0",
          "emailID": "<261052089.21592918775566.JavaMail.user@User-MacBook-Pro.local>",
          "to": "wso2test@localhost",
          "from": "wso2@localhost",
          "cc": "wso2test1@localhost,wso2test2@localhost",
          "bcc": "wso2test3@localhost,wso2test4@localhost",
          "replyTo": "wso2@localhost",
          "subject": "This is the subject",
          "attachments": [{
              "index": "0",
              "name": "contacts.csv",
              "contentType": "TEXT/CSV"
          }]
       },
       {
          "index": "1",
          "emailID": "<261052089.21599187755564.JavaMail.user@User-MacBook-Pro.local>",
          "to": "wso2test@localhost",
          "from": "wso2@localhost",
          "cc": "wso2test1@localhost,wso2test2@localhost",
          "bcc": "wso2test3@localhost,wso2test4@localhost",
          "replyTo": "wso2@localhost",
          "subject": "This is the subject"
       }
    ]
}
```

Content of each email and attachments can be obtained as explained in [Get email body](#get-email-body) and [Get email attachments](#get-email-attachments) sections.

### List emails using POP3

We can use `list` operation to retrieve the emails using POP3 protocol / POP3S which is the secured protocol.

> **NOTE:** To configure the POP3 / POP3S as the protocol connection, POP3 / POP3S should be specified as the `connectionType` when initializing the connection as below.

```xml
<email.connection>
    <host></host>
    <port></port>
    <connectionType>POP3</connectionType>
    <name>pop3connection</name>
    <username></username>
    <password></password>
</email.connection>
``` 

**List email**

When calling the `list` operation the value configured for `name` parameter in the connection configuration should be added as the `configKey` attribute in the operation as below.

```xml
<email.list configKey="pop3connection">
    <subjectRegex>{json-eval($.subjectRegex)}</subjectRegex>
    <offset>{json-eval($.offset)}</offset>
    <limit>{json-eval($.limit)}</limit>
</email.list>
```

**Parameters**

* deleteAfterRetrieve [optional]: Whether the email should be deleted after retrieving.
* receivedSince [optional]: The date after which to retrieve received emails.
* receivedUntil [optional]: The date until which to retrieve received emails.
* sentSince [optional]: The date after which to retrieve sent emails.
* sentUntil [optional]: The date until which to retrieve sent emails.
* subjectRegex [optional]: Subject Regex to match with the wanted emails.
* fromRegex [optional]: From email address to match with the wanted emails.
* seen [optional]: Whether to retrieve 'seen' or 'not seen' emails.
* answered [optional]: Whether to retrieve 'answered' or 'unanswered' emails.
* deleted [optional]: Whether to retrieve 'deleted' or 'not deleted' emails.
* recent [optional]: Whether to retrieve 'recent' or 'past' emails.
* offset [optional]: The index from which to retrieve emails.
* limit [optional]: The number of emails to be retrieved.
* folder [optional]: Name of the Mailbox folder to retrieve emails from. Default is `INBOX`.

**Sample request**

Following is a sample request that can be handled by the List email operation.

```json
{
	"subject":"This is the subject",
	"offset":"0",
	"limit":"2"
}
```

**Sample response**

Given below is a sample response for the List email operation.

```json
{
   "emails": [
       {
          "index": "0",
          "emailID": "<261052089.21592918775566.JavaMail.user@User-MacBook-Pro.local>",
          "to": "wso2test@localhost",
          "from": "wso2@localhost",
          "cc": "wso2test1@localhost,wso2test2@localhost",
          "bcc": "wso2test3@localhost,wso2test4@localhost",
          "replyTo": "wso2@localhost",
          "subject": "This is the subject",
          "attachments": [{
              "index": "0",
              "name": "contacts.csv",
              "contentType": "TEXT/CSV"
          }]
       },
       {
          "index": "1",
          "emailID": "<261052089.21599187755564.JavaMail.user@User-MacBook-Pro.local>",
          "to": "wso2test@localhost",
          "from": "wso2@localhost",
          "cc": "wso2test1@localhost,wso2test2@localhost",
          "bcc": "wso2test3@localhost,wso2test4@localhost",
          "replyTo": "wso2@localhost",
          "subject": "This is the subject"
       }
    ]
}
```

### Get Email Body
We can use `getEmailBody` operation to retrieve the email content of a certain email.

> **NOTE: To use this operation, `list` operation MUST be invoked first**. 

Therefore, when the `list` operation is invoked the following response is set in the message context.

```xml
<emails>
   <email>
      <index>0</index>
      <emailID>&lt;261052089.21592918775566.JavaMail.user@User-MacBook-Pro.local&gt;</emailID>
      <to>wso2test@localhost</to>
      <from>wso2@localhost</from>
      <cc>wso2test1@localhost,wso2test2@localhost</cc>
      <bcc>wso2test3@localhost,wso2test4@localhost</bcc>
      <replyTo>wso2@localhost</replyTo>
      <subject>This is the subject</subject>
      <attachments>
        <attachment>
            <index>0</index>
            <name>contacts.csv</name>
            <contentType>TEXT/CSV</contentType>
        </attachment>
      </attachments>
   </email>
   <email>
      <index>0</index>
      <emailID>&lt;261052089.21599187755564.JavaMail.user@User-MacBook-Pro.local&gt;</emailID>
      <to>wso2test@localhost</to>
      <from>wso2@localhost</from>
      <cc>wso2test1@localhost,wso2test2@localhost</cc>
      <bcc>wso2test3@localhost,wso2test4@localhost</bcc>
      <replyTo>wso2@localhost</replyTo>
      <subject>This is the subject</subject>
   </email>
</emails>
``` 

**Get Email Body**

When calling the `getEmailBody` we should specify the email index of the email we need to retrieve according to the response from `list` operation.
```xml
<email.getEmailBody>
    <emailIndex>0</emailIndex>
</email.getEmailBody>
```

**Parameters**

* emailIndex: Index of the email to be retrieved according to the response from `list` operation.

**Sample response**

When `getEmailBody` operation is invoked, below properties are set in the message context.

**Properties**

* EMAIL_ID: Email ID of the email.
* TO: Recipients of the email.
* FROM: Sender of the email.
* SUBJECT: Subject of the email.
* CC: CC Recipients of the email.
* BCC: BCC Recipients of the email.
* REPLY_TO: Reply to Recipients of the email.
* HTML_CONTENT: HTML content of the email.
* TEXT_CONTENT: Text content of the email.

### Get Email Attachments
We can use `getEmailAttachments` operation to retrieve the attachment content.

> **NOTE: To use this operation, `list` operation MUST be invoked first**. 

Therefore, when the `list` operation is invoked the following response is set in the message context.

```xml
<emails>
   <email>
      <index>0</index>
      <emailID>&lt;261052089.21592918775566.JavaMail.user@User-MacBook-Pro.local&gt;</emailID>
      <to>wso2test@localhost</to>
      <from>wso2@localhost</from>
      <cc>wso2test1@localhost,wso2test2@localhost</cc>
      <bcc>wso2test3@localhost,wso2test4@localhost</bcc>
      <replyTo>wso2@localhost</replyTo>
      <subject>This is the subject</subject>
      <attachments>
        <attachment>
            <index>0</index>
            <name>contacts.csv</name>
            <contentType>TEXT/CSV</contentType>
        </attachment>
      </attachments>
   </email>
   <email>
      <index>0</index>
      <emailID>&lt;261052089.21599187755564.JavaMail.user@User-MacBook-Pro.local&gt;</emailID>
      <to>wso2test@localhost</to>
      <from>wso2@localhost</from>
      <cc>wso2test1@localhost,wso2test2@localhost</cc>
      <bcc>wso2test3@localhost,wso2test4@localhost</bcc>
      <replyTo>wso2@localhost</replyTo>
      <subject>This is the subject</subject>
   </email>
</emails>
``` 

**Get Email Attachment**

When calling the `getEmailAttachments` we should specify the email index of the email and the attachment index we need to retrieve according to the response from `list` operation.
```xml
<email.getEmailAttachments>
    <emailIndex>0</emailIndex>
    <attachmentIndex>0</attachmentIndex>
</email.getEmailAttachments>
```

**Parameters**

* emailIndex: Index of the email to be retrieved according to the response from `list` operation.
* attachmentIndex: Index of the attachment to be retrieved according to the response from `list` operation.

**Sample response**

This operation will set the content of the attachment in the message context according to its content type. 

Given below is a sample.

```csv
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"><soapenv:Body><axis2ns3:text xmlns:axis2ns3="http://ws.apache.org/commons/ns/payload">id,firstname,surname,phone,email
1,John1,Doe,096548763,john1.doe@texasComp.com
2,Jane2,Doe,091558780,jane2.doe@texasComp.com
</axis2ns3:text></soapenv:Body></soapenv:Envelope>

```

When `getEmailAttachments` operation is invoked, below properties are set in the message context.

**Properties**

* ATTACHMENT_TYPE: Content Type of the attachment.
* ATTACHMENT_NAME: Name of the attachment.

### Sample configuration

Following example illustrates how to retrieve emails from gmail using email connector.

1. Configure a local entry as below.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<localEntry key="imapconnection" xmlns="http://ws.apache.org/ns/synapse">
    <email.connection>
        <host>imap.gmail.com</host>
        <port>993</port>
        <name>imapconnection</name>
        <username>testuser@gmail.com</username>
        <password>testuser</password>
        <connectionType>IMAPS</connectionType>
        <requireTLS>false</requireTLS>
    </email.connection>
</localEntry>
```

2.Create a sample proxy as below which will iterate through the emails and attachments retrieved.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<proxy name="ListMail" startOnLoad="true" 
transports="http https" xmlns="http://ws.apache.org/ns/synapse">
    <target faultSequence="fault">
        <inSequence>
            <email.list configKey="imapconnection">
                <subjectRegex>{json-eval($.subjectRegex)}</subjectRegex>
                <seen>{json-eval($.seen)}</seen>
                <answered>{json-eval($.answered)}</answered>
                <deleted>{json-eval($.deleted)}</deleted>
                <recent>{json-eval($.recent)}</recent>
                <offset>{json-eval($.offset)}</offset>
                <limit>{json-eval($.limit)}</limit>
                <folder>{json-eval($.folder)}</folder>
            </email.list>
            <log level="full"/>
            <foreach expression="//emails/email">
                <sequence>
                    <property expression="//email/index/text()" name="index1" scope="default" type="STRING"/>
                    <email.getEmailBody>
                        <emailIndex>{$ctx:index1}</emailIndex>
                    </email.getEmailBody>
                    <log level="custom">
                        <property expression="$ctx:TEXT_CONTENT" name="Text Content"/>
                        <property expression="$ctx:HTML_CONTENT" name="HTML Content"/>
                        <property expression="$ctx:EMAIL_ID" name="Email ID"/>
                    </log>
                    <foreach expression="//email/attachments/attachment">
                        <sequence>
                            <property expression="//attachment/index/text()" name="index2" scope="default" type="STRING"/>
                            <email.getEmailAttachments>
                                <emailIndex>{$ctx:index1}</emailIndex>
                                <attachmentIndex>{$ctx:index2}</attachmentIndex>
                            </email.getEmailAttachments>
                            <log level="full"/>
                        </sequence>
                    </foreach>
                </sequence>
            </foreach>
        </inSequence>
        <outSequence/>
    </target>
</proxy>                         
```

3.Create a json file named query.json and copy the configurations given below to it:

```json
{
	"subjectRegex":"This is the subject",
	"seen": "false",
	"answered":"false",
	"deleted":"false",
	"recent":"false",
	"offset":"0",
	"limit":"2",
	"folder":"INBOX"
}
```

4.Replace the username and password in `email.connection` operation in local entry with your values.

5.Execute the following curl command:

```bash
curl http://localhost:8280/services/ListMail -H "Content-Type: application/json" -d @query.json
```
6.You may observe the email content in the log.