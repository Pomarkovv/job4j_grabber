create table if not exists post(
    id serial primary key,
    name varchar(255),
    link text,
    text text,
    created TIMESTAMP,
    unique(link)
);