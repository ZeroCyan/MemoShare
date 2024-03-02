CREATE TABLE MemoDB.Memos (
    short_link CHAR(8) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    path_to_memo VARCHAR(255) NOT NULL,
    PRIMARY KEY (short_link)
);
