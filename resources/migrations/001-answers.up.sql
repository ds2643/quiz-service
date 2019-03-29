CREATE TABLE IF NOT EXISTS answers (
  id VARCHAR(25) NOT NULL UNIQUE,
  qid VARCHAR(25) NOT NULL REFERENCES questions(id),
  sid VARCHAR(25) NOT NULL REFERENCES sessions(id),
  response INT,
  primary key(id)
);
