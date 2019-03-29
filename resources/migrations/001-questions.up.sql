CREATE TABLE IF NOT EXISTS questions (
  id VARCHAR(25) NOT NULL UNIQUE,
  content mediumtext,
  primary key(id)
);

--;;

INSERT INTO questions (id, content)
VALUES
  ("1","first question body"),
  ("2", "second question body"),
  ("3","third question body");
