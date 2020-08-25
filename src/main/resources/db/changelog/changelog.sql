--liquibase formatted sql

--changeset gsiewruk:prerelease
create table dependencytrack
(
    id serial primary key,
    enabled boolean,
    apikey text
);
create table scannertype(
    id serial primary key,
    name text
);
insert into scannertype (name) values ('DependencyTrack'), ('Spotbug');
create table scan (
    id serial primary key,
    scannertype_id int references scannertype(id),
    inserted date,
    running boolean
);