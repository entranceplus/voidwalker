CREATE TABLE IF NOT EXISTS posts (
id serial primary key,
url VARCHAR not null,
tags VARCHAR not null,
content text not null,
title VARCHAR not null
);

CREATE TABLE IF NOT EXISTS users (
id serial primary key,
username VARCHAR not null,
password VARCHAR not null
);
