-- users-populate.sql

insert into users(username, password, enabled) values ('user1', '{noop}password',true);
insert into users(username, password, enabled) values ( 'user2', '{noop}password', true);
insert into users(username, password, enabled) values ('user3','{noop}password', true);


insert into authorities(username,authority) values  ('user1', 'known');
insert into authorities(username, authority) values ('user2','known');
insert into authorities(username, authority) values ('user3', 'known');