-- users-populate.sql
-- plaintext password
--insert into users(username, password, enabled) values ('user1', '{noop}password',true);
-- Cryptographic hash password
insert into users(username, password, enabled) values ('user1', '{bcrypt}$2y$10$lg6W9cVctqCV/XXkj.ULmehT32uu2S9UsDc40RcJL3KmrbYeSHGDu',true);
--insert into users(username, password, enabled) values ( 'user2', '{noop}password', true);
insert into users(username, password, enabled) values ( 'user2', '{bcrypt}$2y$10$oi0OVqSsfJ6876/3It4/meoofhHnLIgboAdSKpt1.9udtYW5UOeum', true);
--insert into users(username, password, enabled) values ('user3','{noop}password', true);
insert into users(username, password, enabled) values ('user3','{bcrypt}$2y$10$n8TVcm4.8ictWBisf.qinOAwtmKrv3pIl.MuJQypSMD.x4f9rLns2', true);


insert into authorities(username,authority) values  ('user1', 'known');
insert into authorities(username, authority) values ('user2','known');
insert into authorities(username, authority) values ('user3', 'known');