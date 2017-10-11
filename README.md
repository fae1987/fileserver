# fileserver
A file server, which allows the following operations - put, get, delete, search for files
FileId is a Long number, representing unique file content identifier.

# GET
HTTP GET  request to localhost:8080/{fileId}

# PUT
HTTP POST request to localhost:8080/, putting in headers a header 'FileName:{nameOfFile}'

# DELETE
HTTP DELETE request to localhost:8080/{fileId}

# SEARCH
HTTP GET request to localhost:8080/search?filename={filename}
Returns and empty map or a map with fileid as a key and filename as a value

