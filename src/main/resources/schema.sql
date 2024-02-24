CREATE TABLE notes (
    short_link CHAR(8) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    path_to_note VARCHAR(255) NOT NULL,
    PRIMARY KEY (short_link)
);
