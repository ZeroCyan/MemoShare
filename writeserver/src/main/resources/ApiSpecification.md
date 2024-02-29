Request:
    URI: /api/note
    HTTP Verb: POST
    Example Body:
        {
            "expiration_time_in_minutes": 60,
            "note_contents": "Hello World!"
        }
    Notes:
        - Setting "expiration_time_in_minutes" to 0 means there is no expiration time.

Response:
    HTTP Status:
        201 CREATED if the request was processed successfully.
        400 BAD REQUEST unable to process due to invalid syntax, data or formatting. 
        401 UNAUTHORIZED if the user is unauthenticated or unauthorized
    Response Body Type: JSON
    Example Response Body:
        {
            "shortlink": "https://foo.com/foobar"
        }