create table ACT_BPS_SUBSTITUTES (
    USERNAME varchar(255) not null,
    SUBSTITUTE varchar(255) not null,
    TASK_LIST varchar(1000),
    UPDATED timestamp on UPDATE CURRENT_TIMESTAMP,
    SUBSTITUTION_START timestamp not null,
    SUBSTITUTION_END timestamp null,
    ENABLED tinyint default 1,
    CREATED timestamp,
    TRANSITIVE_SUBSTITUTE varchar(255) null,
    TENANT_ID int NOT NULL,
    primary key (USERNAME, TENANT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;