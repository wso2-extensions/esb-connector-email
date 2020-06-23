# Expunge Mail Folder

[[Overview]](#overview)  [[Operations]](#operations)  [[Sample configuration]](#sample-configuration)

### Overview 

The following operation allow you to permanently delete the emails marked for deletion in a certain folder.

| Operation | Description |
| ------------- |-------------|
|[Expunge Folder](#expunge-folder)|Expunge folder. |

## Operations

This section provides more details on each of the operations.

### Expunge Folder
We can use `expungeFolder` operation to to permanently delete the emails marked for deletion using the IMAP / IMAPS which is the secured protocol.

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

**Expunge Folder**

When calling the `expungeFolder` operation the value configured for `name` parameter in the connection configuration (in the local entry configuration) should be added as the `configKey` attribute in the operation as below.
```xml
<email.expungeFolder configKey="imapconnection">
    <folder>{json-eval($.folder)}</folder>
</email.expungeFolder>
```

**Parameters**

* folder [optional]: Name of the mailbox folder where the email is. Default is `INBOX`.

**Sample request**

Following is a sample request that can be handled by the Expunge Folder operation.

```json
{
	"folder":"Inbox"
}
```

**Sample response**

Given below is a sample response for the Expunge Folder operation.

```json
{
  "result": {
    "success": "true"
  }
}
```

### Sample configuration

Following example illustrates how to expunge a folder in gmail.

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
<proxy name="ExpungeFolder" startOnLoad="true" transports="http https" xmlns="http://ws.apache.org/ns/synapse">
    <target faultSequence="fault">
        <inSequence>
            <email.expungeFolder configKey="imapconnection">
                <folder>{json-eval($.folder)}</folder>
            </email.expungeFolder>
        </inSequence>
        <outSequence/>
    </target>
</proxy>                      
```

3.Create a json file named query.json and copy the configurations given below to it:

```json
{
	"folder":"Inbox"
}
```
4.Replace the username and password in `email.connection` operation in local entry with your values.

5.Execute the following curl command:

```bash
curl http://localhost:8280/services/ExpungeFolder -H "Content-Type: application/json" -d @query.json
```

6.The emails marked for deletion in the folder will be deleted permanently.
