PRAGMA foreign_keys = ON;

drop table if exists user;
create table user (
  id integer primary key autoincrement,
  name text not null,
  email text not null unique,
  auth_token text not null
);

drop table if exists list;
create table list (
  id integer primary key autoincrement,
  owner_id integer references user(id) on delete cascade,
  name text not null
);

drop table if exists list_item;
create table list_item (
  id integer primary key autoincrement,
  list_id integer references list(id) on delete cascade,
  value text not null,
  checked boolean
);

drop table if exists list_member;
create table list_member (
  id integer primary key autoincrement,
  list_id integer references list(id) on delete cascade,
  user_id integer references user(id) on delete cascade
);

drop table if exists list_item_relation;
create table list_item_relation (
  id integer primary key autoincrement,
  list_id integer references list(id) on delete cascade,
  item_id integer references list_item(id) on delete cascade
);
