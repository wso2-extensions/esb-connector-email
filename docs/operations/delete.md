# Delete Emails

[[Overview]](#overview)  [[Operations]](#operations)  [[Sample configuration]](#sample-configuration)

### Overview 

The following operation allow you to delete emails using the IMAP / IMAPS protocol.

| Operation | Description |
| ------------- |-------------|
|[Delete email](#delete-email)|Delete email. |

## Operations

This section provides more details on each of the operations.

### Delete email
We can use `delete` operation to delete emails using the IMAP / IMAPS which is the secured protocol.

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

**Delete email**

When calling the `delete` operation the value configured for `name` parameter in the connection configuration (in the local entry configuration) should be added as the `configKey` attribute in the operation as below.
```xml
<email.delete configKey="imapconnection">
    <folder>{json-eval($.folder)}</folder>
    <emailID>{json-eval($.emailID)}</emailID>
</email.delete>
```

**Parameters**

* emailID: Email ID of the email to delete.
* folder [optional]: Name of the mailbox folder from which to delete the emails. Default is `INBOX`.

**Sample request**

Following is a sample request that can be handled by the Delete Email operation.

```json
{
	"folder":"Inbox",
	"emailID": "<296045440.2.15945432523040@localhost>"
}
```

**Sample response**

Given below is a sample response for the Delete Email operation.

```json
{
  "result": {
    "success": "true"
  }
}
```

### Sample configuration

Following example illustrates how to delete an email in gmail.

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

2.Create a sample proxy as below.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<proxy name="DeleteMail" startOnLoad="true" transports="http https" xmlns="http://ws.apache.org/ns/synapse">
    <target faultSequence="fault">
        <inSequence>
            <email.delete configKey="imapconnection">
                <folder>{json-eval($.folder)}</folder>
                <emailID>{json-eval($.emailID)}</emailID>
            </email.delete>
        </inSequence>
        <outSequence/>
    </target>
</proxy>                      
```

3.Create a json file named query.json and copy the configurations given below to it:

```json
{
	"folder":"Inbox",
	"emailID": "<296045440.2.15945432523040@localhost>"
}
```
4.Replace the username and password in `email.connection` operation in local entry with your values.

5.Execute the following curl command:

```bash
curl http://localhost:8280/services/DeleteMail -H "Content-Type: application/json" -d @query.json
```

6.The email will be deleted.

> NOTE: Gmail does not permanently delete the email in this process. It will be still found in 'All Mail'.
