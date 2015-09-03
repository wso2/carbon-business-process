--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--    http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

--
-- BPEL Related SQL Scripts
--

DROP TABLE ODE_SCHEMA_VERSION;

DROP TABLE ODE_JOB;

DROP TABLE TASK_ATTACHMENT;
DROP TABLE ODE_ACTIVITY_RECOVERY;
DROP TABLE ODE_CORRELATION_SET;
DROP TABLE ODE_CORRELATOR;
DROP TABLE ODE_CORSET_PROP;
DROP TABLE ODE_EVENT;
DROP TABLE ODE_FAULT;
DROP TABLE ODE_MESSAGE;
DROP TABLE ODE_MESSAGE_EXCHANGE;
DROP TABLE ODE_MESSAGE_ROUTE;
DROP TABLE ODE_MEX_PROP;
DROP TABLE ODE_PARTNER_LINK;
DROP TABLE ODE_PROCESS;
DROP TABLE ODE_PROCESS_INSTANCE;
DROP TABLE ODE_SCOPE;
DROP TABLE ODE_XML_DATA;
DROP TABLE ODE_XML_DATA_PROP;
DROP TABLE OPENJPA_SEQUENCE_TABLE;
DROP TABLE STORE_DU;
DROP TABLE STORE_PROCESS;
DROP TABLE STORE_PROCESS_PROP;
DROP TABLE STORE_PROC_TO_PROP;
DROP TABLE STORE_VERSIONS;


--
-- Human Task Related SQL Scripts
--
DROP TABLE HT_DEADLINE;
DROP TABLE HT_DEPLOYMENT_UNIT;
DROP TABLE HT_EVENT;
DROP TABLE HT_GENERIC_HUMAN_ROLE;
DROP TABLE HT_HUMANROLE_ORGENTITY;
DROP TABLE HT_JOB;
DROP TABLE HT_MESSAGE;
DROP TABLE HT_ORG_ENTITY;
DROP TABLE HT_PRESENTATION_ELEMENT;
DROP TABLE HT_PRESENTATION_PARAM;
DROP TABLE HT_TASK;
DROP TABLE HT_TASK_ATTACHMENT;
DROP TABLE HT_TASK_COMMENT;
DROP TABLE HT_VERSIONS;


--
-- Attachment Management Related SQL Scripts
--
DROP TABLE ATTACHMENT;


--
-- B4P Related SQL Scripts
--
DROP TABLE HT_COORDINATION_DATA;