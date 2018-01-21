CREATE TABLE IF NOT EXISTS posts (
  id serial primary key,
  url VARCHAR not null,
  tags VARCHAR not null,
  content text not null,
  title VARCHAR not null
);
