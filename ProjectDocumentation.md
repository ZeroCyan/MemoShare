# Project Documentation: 

## Project name: MemoShare
## A PasteBin Clone

## Overview:
The PasteBin clone project aims to replicate the functionality of PasteBin, providing users with a platform to store 
and share text snippets or code snippets. The system architecture consists of a WebServer acting as a reverse proxy, 
a ReadServer for handling GET requests, a WriteServer for handling POST requests, 
and integration with a Blob storage (currently Azurite, a locally installed Azure cloud storage emulator) 
and a metadata store (currently a MySQL database).

## Architecture:
The system architecture follows a microservices-based approach with separate components responsible for specific tasks:
- **WebServer**: Receives HTTP requests from end-users and forwards them to the appropriate server based on the request type.
- **ReadServer**: Handles GET requests by retrieving stored text snippets from the Blob storage and metadata from the MySQL database.
- **WriteServer**: Handles POST requests by storing new text snippets in the Blob storage and metadata in the MySQL database.
- **Blob Storage**: Stores the actual text snippets or code snippets uploaded by users.
- **MySQL Database**: Stores metadata associated with each text snippet, including information such as snippet ID, creation timestamp, expiration timestamp, and access permissions.

## Components:
- **WebServer:**
    - Endpoints:
      - GET /api/note?shortlink=xxxxxxxx: For retrieving a specific snippet.
      - POST /api/note: For creating a new snippet.
- **ReadServer:**
    - Endpoints:      
      - GET /api/note?shortlink=: Retrieves a specific snippet.
- **WriteServer:**
    - Endpoints:
      - POST /api/snippets: Stores a new snippet.
- **Blob Storage:**
    - Provider: Local Azurite installation
    - Configuration: Configured to store text snippets uploaded by users.
- **MySQL Database:**
    Refer to the [MYSQL section](#mysql-section) for more information.

## API Specification:
- **Endpoints:**
    - `GET /api/snippets/{snippetId}`
        - Description: Retrieves a specific snippet.
        - Parameters:
            - snippetId (string): The ID of the snippet to retrieve.
        - Response:
            - Status Code 200: Successfully retrieved the snippet.
            - Status Code 404: Snippet not found.
        - Example Request: `GET /api/snippets/123`
        - Example Response:
          ```json
          {
            "id": "123",
            "content": "Example snippet content",
            "createdAt": "2024-03-09T12:00:00Z",
            "expiresAt": "2024-03-16T12:00:00Z",
            "accessPermissions": "public"
          }
          ```
    - `POST /api/snippets`
        - Description: Creates a new snippet.
        - Request Body:
          ```json
          {
            "content": "New snippet content",
            "expiresAt": "2024-03-16T12:00:00Z",
            "accessPermissions": "public"
          }
          ```
        - Response:
            - Status Code 201: Successfully created the snippet.
            - Status Code 400: Invalid request body.
        - Example Request:
          ```json
          POST /api/snippets
          {
            "content": "New snippet content",
            "expiresAt": "2024-03-16T12:00:00Z",
            "accessPermissions": "public"
          }
          ```
        - Example Response:
          ```json
          {
            "id": "456",
            "createdAt": "2024-03-09T12:00:00Z"
          }
          ```

## Deployment:
- Deployment of the WebServer, ReadServer, and WriteServer can be done using containerization technologies such as Docker.
- Configuration files and environment variables should be provided for specifying connection details to Blob storage and MySQL database.
 11

## MYSQL <a id="mysql-section"></a>
- Database name: MemoDB
- Table specification:
```sql
TABLE MemoDB.Memos (
    short_link CHAR(8) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    path_to_memo VARCHAR(255) NOT NULL,
    PRIMARY KEY (short_link)
);
```