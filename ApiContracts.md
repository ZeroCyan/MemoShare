# API specification for MemoShare

#### This document describes the MemoShare application. It outlines the architecture, endpoints, request methods, request and response payloads,error handling, and any other relevant details necessary for developers to integrate with and utilize the different server APIs effectively.

## Architecture

#### MemoShare consists of 3 main applications. A webserver, acting as a reverse proxy, communicating with a WriteServer and a ReadServer.

#### The WriteServer and ReadServer both communicate with 2 types of data storage systems. Currently one system is a MYSQL database, the other a locally installed Azure Blob Storage (Azurite).

#### -----------------

#### A GET request received by the WebServer will be forwarded to the ReadServer, which will fetch the relevant data from the data stores and send it towards the WebServer.

#### A POST request received by the WebServer will be forwarded to the WriteServer, which will handle the storage of the payload.

### TODO: Insert image

## API specification

Response Status Codes:

    200 OK: The request was successful, and the resource is included in the response.
    404 Not Found: The requested resource could not be found on server B.
    500 Internal Server Error: Server B encountered an unexpected condition that prevented it from fulfilling the request.

Additional Response Status Codes:

    400 Bad Request: The request from the WebServer is malformed or incorrect.
    401 Unauthorized: The WebServer lacks proper authentication credentials for the request.
    403 Forbidden: The ReadServer refuses to fulfill the request, likely due to lack of permissions.
    406 Not Acceptable: The requested resource exists but cannot be provided in the requested format (mime type).
    405 Method Not Allowed: The HTTP method used by server A is not supported for the requested resource.
    429 Too Many Requests: Server B is rate-limiting requests from server A.


Response Headers:

    Content-Type: Indicates the media type of the resource being returned.

Request Headers:

    Accept: Specifies the media types that the WebServer is willing to receive from the ReadServer.

Error Response Body (for 404, 406, 500, and potentially others):

    A Problem Detail (cf. [RFC7807](https://datatracker.ietf.org/doc/html/rfc7807)) JSON object, describing the error, including relevant details such as error code, message, and possibly additional information for troubleshooting.

E.g.: 
```json
{
  "type":"about:blank",
  "title":"Not Found",
  "status":404,
  "detail":"The requested resource is not found.",
  "instance":"/api/note"
}
```

