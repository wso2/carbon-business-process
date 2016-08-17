create table ACT_BPS_SUBSTITUTES (
    USERNAME varchar(255) not null,
    SUBSTITUTE varchar(255) not null,
    TASK_LIST varchar(1000),
    SUBSTITUTION_START timestamp not null,
    SUBSTITUTION_END timestamp,
    ENABLED integer default 1,
    CREATED timestamp,
    UPDATED timestamp,
    TRANSITIVE_SUBSTITUTE varchar(255),
    TENANT_ID INT NOT NULL,
    primary key (USERNAME, TENANT_ID)
);