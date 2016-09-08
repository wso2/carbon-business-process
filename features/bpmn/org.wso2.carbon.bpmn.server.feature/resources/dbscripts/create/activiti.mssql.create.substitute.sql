create table ACT_BPS_SUBSTITUTES (
  USERNAME nvarchar(255) not null,
  SUBSTITUTE nvarchar(255) not null,
  TASK_LIST nvarchar(1000),
  SUBSTITUTION_START datetime not null,
  SUBSTITUTION_END datetime,
  ENABLED tinyint default 1,
  CREATED datetime,
  UPDATED datetime,
  TRANSITIVE_SUBSTITUTE nvarchar(255),
  TENANT_ID int NOT NULL,
  primary key (USERNAME, TENANT_ID)
);