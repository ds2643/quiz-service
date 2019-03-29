CREATE TABLE IF NOT EXISTS rules (
  ruleset VARCHAR(25) NOT NULL UNIQUE,
  minutesallowed INT NOT NULL
);

--;;

INSERT INTO rules (ruleset, minutesallowed)
VALUES
  ("default", 60);
