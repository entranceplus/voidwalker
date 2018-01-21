CREATE TABLE IF NOT EXISTS users (
  id serial primary key,
  username VARCHAR not null,
  password VARCHAR not null
);
