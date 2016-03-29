///**
// *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
//package org.wso2.carbon.bpmn.core.db;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.nio.charset.Charset;
//import java.sql.Connection;
//import java.sql.DatabaseMetaData;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.SQLWarning;
//import java.sql.Statement;
//import java.util.StringTokenizer;
//import javax.sql.DataSource;
//
//public class DatabaseCreator {
//	private static Log log = LogFactory.getLog(DatabaseCreator.class);
//	private DataSource dataSource;
//	private String delimiter = ";";
//	private Connection conn = null;
//	private Statement statement;
//
//	public DatabaseCreator(DataSource dataSource) {
//		this.dataSource = dataSource;
//	}
//
//	public void createRegistryDatabase() throws Exception {
//		try {
//			this.conn = this.dataSource.getConnection();
//			this.conn.setAutoCommit(false);
//			this.statement = this.conn.createStatement();
//			this.executeSQLScript();
//			this.conn.commit();
//			if(log.isTraceEnabled()) {
//				log.trace("Registry tables are created successfully.");
//			}
//		} catch (SQLException var10) {
//			String msg = "Failed to create database tables for registry resource store. " + var10.getMessage();
//			log.fatal(msg, var10);
//			throw new Exception(msg, var10);
//		} finally {
//			try {
//				if(this.conn != null) {
//					this.conn.close();
//				}
//			} catch (SQLException var9) {
//				log.error("Failed to close database connection.", var9);
//			}
//
//		}
//
//	}
//
//	public boolean isDatabaseStructureCreated(String checkSQL) {
//		try {
//			if(log.isTraceEnabled()) {
//				log.trace("Running a query to test the database tables existence.");
//			}
//
//			this.conn = this.dataSource.getConnection();
//
//			try {
//				this.statement = this.conn.createStatement();
//				ResultSet e = this.statement.executeQuery(checkSQL);
//				e.close();
//			} finally {
//				try {
//					if(this.statement != null) {
//						this.statement.close();
//					}
//				} finally {
//					if(this.conn != null) {
//						this.conn.close();
//					}
//
//				}
//
//			}
//
//			return true;
//		} catch (SQLException var20) {
//			return false;
//		}
//	}
//
//	private void executeSQL(final String sql) throws Exception {
//		if(!"".equals(sql.trim())) {
//			ResultSet resultSet = null;
//
//			try {
//				if(log.isDebugEnabled()) {
//					log.debug("SQL : " + sql);
//				}
//
//				boolean updateCount = false;
//				int updateCountTotal = 0;
//				boolean e = this.statement.execute(sql);
//				int updateCount1 = this.statement.getUpdateCount();
//				resultSet = this.statement.getResultSet();
//
//				do {
//					if(!e && updateCount1 != -1) {
//						updateCountTotal += updateCount1;
//					}
//
//					e = this.statement.getMoreResults();
//					if(e) {
//						updateCount1 = this.statement.getUpdateCount();
//						resultSet = this.statement.getResultSet();
//					}
//				} while(e);
//
//				if(log.isDebugEnabled()) {
//					log.debug(sql + " : " + updateCountTotal + " rows affected");
//				}
//
//				for(SQLWarning warning = this.conn.getWarnings(); warning != null; warning = warning.getNextWarning()) {
//					log.debug(warning + " sql warning");
//				}
//
//				this.conn.clearWarnings();
//			} catch (SQLException var15) {
//				if(!var15.getSQLState().equals("X0Y32") && !var15.getSQLState().equals("42710")) {
//					throw new Exception("Error occurred while executing : " + sql, var15);
//				}
//
//				if(log.isDebugEnabled()) {
//					log.info("Table Already Exists. Hence, skipping table creation");
//				}
//			} finally {
//				if(resultSet != null) {
//					try {
//						resultSet.close();
//					} catch (SQLException var14) {
//						log.error("Error occurred while closing result set.", var14);
//					}
//				}
//
//			}
//
//		}
//	}
//
//	public static String getDatabaseType(Connection conn) throws Exception {
//		String type = null;
//
//		String msg;
//		try {
//			if(conn != null && !conn.isClosed()) {
//				DatabaseMetaData e = conn.getMetaData();
//				msg = e.getDatabaseProductName();
//				type = getDatabaseType((String)msg);
//			}
//
//			return type;
//		} catch (SQLException var4) {
//			msg = "Failed to create registry database." + var4.getMessage();
//			log.fatal(msg, var4);
//			throw new Exception(msg, var4);
//		}
//	}
//
//	public static String getDatabaseType(String dbUrl) throws Exception {
//		String type = null;
//
//		try {
//			if(dbUrl != null) {
//				if(dbUrl.matches("(?i).*hsql.*")) {
//					type = "hsql";
//				} else if(dbUrl.matches("(?i).*derby.*")) {
//					type = "derby";
//				} else if(dbUrl.matches("(?i).*mysql.*")) {
//					type = "mysql";
//				} else if(dbUrl.matches("(?i).*oracle.*")) {
//					type = "oracle";
//				} else if(dbUrl.matches("(?i).*microsoft.*")) {
//					type = "mssql";
//				} else if(dbUrl.matches("(?i).*h2.*")) {
//					type = "h2";
//				} else if(dbUrl.matches("(?i).*db2.*")) {
//					type = "db2";
//				} else if(dbUrl.matches("(?i).*postgresql.*")) {
//					type = "postgresql";
//				} else if(dbUrl.matches("(?i).*openedge.*")) {
//					type = "openedge";
//				} else {
//					if(!dbUrl.matches("(?i).*informix.*")) {
//						String e = "Unsupported database: " + dbUrl + ". Database will not be created automatically by the WSO2 Registry. " + "Please create the database using appropriate database scripts for " + "the database.";
//						throw new Exception(e);
//					}
//
//					type = "informix";
//				}
//			}
//
//			return type;
//		} catch (SQLException var4) {
//			String msg = "Failed to create registry database." + var4.getMessage();
//			log.fatal(msg, var4);
//			throw new Exception(msg, var4);
//		}
//	}
//
//	private void executeSQLScript() throws Exception {
//		String databaseType = getDatabaseType((Connection)this.conn);
//		boolean keepFormat = false;
//		if("oracle".equals(databaseType)) {
//			this.delimiter = "/";
//		} else if("db2".equals(databaseType)) {
//			this.delimiter = "/";
//		} else if("openedge".equals(databaseType)) {
//			this.delimiter = "/";
//			keepFormat = true;
//		}
//
//		String dbscriptName = this.getDbScriptLocation(databaseType);
//		StringBuffer sql = new StringBuffer();
//		BufferedReader reader = null;
//
//		try {
//			FileInputStream e = new FileInputStream(dbscriptName);
//			reader = new BufferedReader(new InputStreamReader(e, Charset.defaultCharset()));
//
//			while(true) {
//				String line;
//				if((line = reader.readLine()) == null) {
//					if(sql.length() > 0) {
//						this.executeSQL(sql.toString());
//					}
//					break;
//				}
//
//				line = line.trim();
//				if(!keepFormat) {
//					if(line.startsWith("//") || line.startsWith("--")) {
//						continue;
//					}
//
//					StringTokenizer st = new StringTokenizer(line);
//					if(st.hasMoreTokens()) {
//						String token = st.nextToken();
//						if("REM".equalsIgnoreCase(token)) {
//							continue;
//						}
//					}
//				}
//
//				sql.append(keepFormat?"\n":" ").append(line);
//				if(!keepFormat && line.indexOf("--") >= 0) {
//					sql.append("\n");
//				}
//
//				if(checkStringBufferEndsWith(sql, this.delimiter)) {
//					this.executeSQL(sql.substring(0, sql.length() - this.delimiter.length()));
//					sql.replace(0, sql.length(), "");
//				}
//			}
//		} catch (IOException var13) {
//			log.error("Error occurred while executing SQL script for creating registry database", var13);
//			throw new Exception("Error occurred while executing SQL script for creating registry database", var13);
//		} finally {
//			if(reader != null) {
//				reader.close();
//			}
//
//		}
//
//	}
//
//	protected String getDbScriptLocation(String databaseType) {
//		String scriptName = databaseType + ".sql";
//		if(log.isDebugEnabled()) {
//			log.debug("Loading database script from :" + scriptName);
//		}
//
//		String carbonHome = System.getProperty("carbon.home");
//		return carbonHome + "/dbscripts/" + scriptName;
//	}
//
//	public static boolean checkStringBufferEndsWith(StringBuffer buffer, String suffix) {
//		if(suffix.length() > buffer.length()) {
//			return false;
//		} else {
//			int endIndex = suffix.length() - 1;
//
//			for(int bufferIndex = buffer.length() - 1; endIndex >= 0; --endIndex) {
//				if(buffer.charAt(bufferIndex) != suffix.charAt(endIndex)) {
//					return false;
//				}
//
//				--bufferIndex;
//			}
//
//			return true;
//		}
//	}
//}
//
