--users-populate.sql

insert into users(username, password, enabled) values('sam','{noop}password',true);
--insert into users(username, password, enabled) values('sam','{bcrypt}$2y$10$z2CdB.iFXUYXCV4xK9aZ.ePWlhpjcbe/f4Mgh0yEEX2tDFr0ISG/S',true);
insert into users(username, password, enabled) values('rebecca','{noop}password',true);
--insert into users(username, password, enabled) values('rebecca','{bcrypt}$2y$10$Wthw.Rw86JEvZvMg6do/o.TVkf2LxUIxKCoNRG95tGHfK5jIAKuOq',true);
insert into users(username, password, enabled) values('woody','{noop}password',true);
--insert into users(username, password, enabled) values('woody','{bcrypt}$2y$10$PVHN8ymXBxM8trllr6tmtO8k1txYewCxmr4RksfdmcYbdgVgZWh42',true);
insert into users(username, password, enabled) values('carla','{noop}password',true);
--insert into users(username, password, enabled) values('carla','{bcrypt}$2y$10$9Cr/mPRAYKsnrJZVi3h1BO.YmVzLUoMe12IdKtyX6ugzXJ9PDjtuG',true);
insert into users(username, password, enabled) values('norm','{noop}password',true);
--insert into users(username, password, enabled) values('norm','{bcrypt}$2y$10$1uEUTtV3BXbJponYUzikKuP4O.H12bV1IHujlqOe55gStuAAswweO',true);
insert into users(username, password, enabled) values('cliff','{noop}password',true);
--insert into users(username, password, enabled) values('cliff','{bcrypt}$2y$10$f6SePSsc3tOf4IJQtHI.EeP5ngCftbBnpawwf.Z6bZYe3b8ZJ/C7a',true)
insert into users(username, password, enabled) values('frasier','{noop}password',true);
--insert into users(username, password, enabled) values('frasier','{bcrypt}$2y$10$.kRSYya9RXN1WQ.69wBLfOPAmbpu5rg50cqsR866cQBtrBjmzEi6a',true);

insert into authorities(username, authority) values('sam','ROLE_ADMIN');
insert into authorities(username, authority) values('rebecca','ROLE_ADMIN');

insert into authorities(username, authority) values('woody','ROLE_CLERK');
insert into authorities(username, authority) values('carla','ROLE_CLERK');

insert into authorities(username, authority) values('norm','ROLE_CUSTOMER');
insert into authorities(username, authority) values('cliff','ROLE_CUSTOMER');
insert into authorities(username, authority) values('frasier','ROLE_CUSTOMER');
insert into authorities(username, authority) values('frasier','PRICE_CHECK');

