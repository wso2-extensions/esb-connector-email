# Configuring the Email Connector

## Initializing the connector

To use the email connector, add the following `<email.connection>` must be configured in a local entry configuration before carrying out any other operation.

### Connection Configuration
```xml
<email.connection>
    <host></host>
    <port></port>
    <connectionType></connectionType>
    <name></name>
    <username></username>
    <password></password>
</email.connection>
```

**Parameters**

* host: Host name of the mail server.
* port: The port number of the mail server.
* name: Unique name the connection is identified by.
* username: Username used to connect with the mail server.
* password: Password to connect with the mail server.
* connectionType: Email connection type (protocol) that should be used to establish the connection with the server. (IMAP/IMAPS/POP3/POP3S/SMTP/SMTPS)
* readTimeout [optional]: The socket read timeout value.
* connectionTimeout [optional]: The socket connection timeout value.
* writeTimeout [optional]: The socket write timeout value.
* requireTLS [optional]: Whether the connection should be established using TLS. The default value is false. Therefore, for secured protocols SSL will be used by default.
* checkServerIdentity [optional]: Whether server identity should be checked.
* trustedHosts [optional]: Comma separated string of trust host names.
* sslProtocols [optional]: Comma separated string of SSL protocols.
* cipherSuites [optional]: Comma separated string of Cipher Suites.
* maxActiveConnections [optional]: Maximum number of active connections in the pool.
* maxIdleConnections [optional]: Maximum number of idle connections in the pool.
* maxWaitTime [optional]: Maximum number of idle connections in the pool.
* minEvictionTime [optional]: The minimum amount of time an object may sit idle in the pool before it is eligible for eviction.
* evictionCheckInterval [optional]: The number of milliseconds between runs of the object evictor.
* exhaustedAction [optional]: The behavior of the pool when the pool is exhausted. (WHEN_EXHAUSTED_FAIL/WHEN_EXHAUSTED_BLOCK/WHEN_EXHAUSTED_GROW)

## Sample configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<localEntry key="imapconnection" xmlns="http://ws.apache.org/ns/synapse">
<email.connection>
    <host></host>
    <port></port>
    <connectionType></connectionType>
    <name></name>
    <username></username>
    <password></password>
</email.connection>
</localEntry>
```

Now that you have initialized the Email Connector, use the information in the following topics to perform various operations with the connector.

[List Emails](operations/list.md)  
[Send emails](operations/send.md)  
[Delete an email](operations/delete.md)  
[Mark email as Deleted](operations/markAsDeleted.md)  
[Mark email as Read](employeeTimeManagement/employeeTimeManagement.md)  
[Expunge Folder](reporting/reporting.md)  
