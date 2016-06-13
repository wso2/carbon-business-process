create table ACT_BPS_SUBSTITUTES (
  USER varchar(255) not null,
  SUBSTITUTE varchar(255) not null,
  SUBSTITUTION_START timestamp not null,
  SUBSTITUTION_END timestamp null,
  ENABLED tinyint default 1,
  CREATED timestamp,
  UPDATED timestamp,
  TRANSITIVE_SUBSTITUTE varchar(255) null,
  TENANT_ID int NOT NULL,
  primary key (USER, TENANT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;