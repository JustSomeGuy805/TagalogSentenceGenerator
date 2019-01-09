CREATE SCHEMA tagalog_phrase AUTHORIZATION SA;
CREATE TABLE word (
    word_id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tagalog varchar(45) NOT NULL,
    english varchar(45) NOT NULL,
    tags varchar(100) NOT NULL DEFAULT ''
  );


CREATE TABLE sentence (
    sentence_id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tagalog varchar(100) NOT NULL,
    english varchar(100) NOT NULL
  );
