# Send Emails

[[Overview]](#overview)  [[Operations]](#operations)  [[Sample configuration]](#sample-configuration)

### Overview 

The following operation allow you to send emails using the SMTP / SMTPS protocol.

| Operation | Description |
| ------------- |-------------|
|[Send email](#send-email)|Send emails. |

## Operations

This section provides more details on each of the operations.

### Send email
We can use `send` operation to send emails using the SMTP / SMTPS which is the secured protocol.

> **NOTE:** To configure the SMTP / SMTPS as the protocol connection, SMTP / SMTPS should be specified as the `connectionType` when initializing the connection as below.

```xml
<email.connection>
    <host></host>
    <port></port>
    <connectionType>SMTP</connectionType>
    <name>smtpconnection</name>
    <username></username>
    <password></password>
</email.connection>
``` 

**Send email**

When calling the `send` operation the value configured for `name` parameter in the connection configuration (in the local entry configuration) should be added as the `configKey` attribute in the operation as below.
```xml
<email.send configKey="smtpconnection">
    <from>{json-eval($.from)}</from>
    <to>{json-eval($.to)}</to>
    <subject>{json-eval($.subject)}</subject>
    <content>{json-eval($.content)}</content>
    <contentType>{json-eval($.contentType)}</contentType>
    <encoding>{json-eval($.encoding)}</encoding>
    <attachments>{json-eval($.attachments)}</attachments>
    <contentTransferEncoding>{json-eval($.contentTransferEncoding)}</contentTransferEncoding>
</email.send>
```

**Parameters**

* from: The 'From' address of the message sender.
* to: The recipient addresses of 'To' (primary) type.
* cc [optional]: The recipient addresses of 'CC' (carbon copy) type.
* bcc [optional]: The recipient addresses of 'BCC' (blind carbon copy) type.
* replyTo [optional]: The email addresses to which to reply to this email.
* subject [optional]: The subject of the email.
* content [optional]: Body of the message in any format.
* contentType [optional]: Content Type of the body text.
* encoding [optional]: The character encoding of the body.
* attachments [optional]: The attachments that are sent along with the email body.
* contentTransferEncoding [optional]: The index from which to retrieve emails.
* limit [optional]: The number of emails to be retrieved.
* folder [optional]: Encoding used to indicate the type of transformation that is used to represent the body in an acceptable manner for transport.

**Sample request**

Following is a sample request that can be handled by the Send email operation.

```json
{
	"from": "user1@gmail.com",
	"to": "user2@gmail.com",
	"subject": "This is the subject",
	"content": "This is the body",
	"attachments": "/Users/user1/Desktop/contacts.csv",
	"contentType": "text/html",
	"encoding": "UTF-8",
	"contentTransferEncoding": "Base64"
}
```

**Sample response**

Given below is a sample response for the Send email operation.

```json
{
  "result": {
    "success": "true"
  }
}
```

### Sample configuration

Following example illustrates how to send an email using gmail.

1. Configure a local entry as below.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<localEntry key="smtpconnection" xmlns="http://ws.apache.org/ns/synapse">
    <email.connection>
        <host>smtp.gmail.com</host>
        <port>465</port>
        <name>smtpconnection</name>
        <username>testuser@gmail.com</username>
        <password>testuser</password>
        <connectionType>SMTPS</connectionType>
        <requireTLS>false</requireTLS>
    </email.connection>
</localEntry>
```

2.Create a sample proxy as below.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<proxy name="SendMail" startOnLoad="true" transports="http https" xmlns="http://ws.apache.org/ns/synapse">
    <target faultSequence="fault">
        <inSequence>
            <log level="full"/>
            <email.send configKey="smtpconnection">
                <from>{json-eval($.from)}</from>
                <to>{json-eval($.to)}</to>
                <subject>{json-eval($.subject)}</subject>
                <content>{json-eval($.content)}</content>
                <contentType>{json-eval($.contentType)}</contentType>
                <encoding>{json-eval($.encoding)}</encoding>
                <attachments>{json-eval($.attachments)}</attachments>
                <contentTransferEncoding>{json-eval($.contentTransferEncoding)}</contentTransferEncoding>
            </email.send>
            <log level="full"/>
            <respond/>
        </inSequence>
        <outSequence/>
    </target>
</proxy>                       
```

3.Create a json file named query.json and copy the configurations given below to it:

```json
{
	"from": "testuser@gmail.com",
	"to": "testuser2@gmail.com",
	"subject": "This is the subject",
	"content": "This is the body",
	"attachments": "/Users/user1/Desktop/contacts.csv",
	"contentType": "text/html",
	"encoding": "UTF-8",
	"contentTransferEncoding": "Base64"
}
```
4.Replace the username and password in `email.connection` operation in local entry with your values.

5.Execute the following curl command:

```bash
curl http://localhost:8280/services/SendMail -H "Content-Type: application/json" -d @query.json
```

6.The email will be sent.
