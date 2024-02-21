CREATE TABLE pastes (
    short_link CHAR(8) NOT NULL,
    expiration_length_in_minutes INT NOT NULL,
    created_at DATETIME NOT NULL,
    path_to_paste VARCHAR(255) NOT NULL,
    PRIMARY KEY (short_link)
);
