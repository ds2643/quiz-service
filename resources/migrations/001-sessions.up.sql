CREATE TABLE IF NOT EXISTS sessions (
  id VARCHAR(25) NOT NULL UNIQUE,
  firstname VARCHAR(32),
  lastname VARCHAR(32),
  email VARCHAR(64),
  starttime TIME,
  endtime TIME,
  iscomplete BOOL,
  primary key(id)
);
