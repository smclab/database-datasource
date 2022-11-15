package com.smc.connector.model;

import com.smc.connector.util.Util;

import java.sql.*;
import java.util.*;

public class DatabaseRequest extends Request {
    public DatabaseRequest(Map model) {
        super(model);
        this._jdbcUrl = (String) model.get("url");
        this._database = (String) model.get("database");
        this._serverName = (String) model.get("serverName");
        this._serverPort = (String) model.get("serverPort");
        this._databaseName = (String) model.get("databaseName");
        this._username = (String) model.get("username");
        this._password = (String) model.get("password");
        this._timestamp = (long) model.get("timestamp");
        this._datasourceId = (int) model.get("datasourceId");
        this._scheduleId = (String) model.get("scheduleId");
        this._table = (String) model.get("table");
        this._columns = (List<String>) (model.get("columns"));
        this._query = (String) model.get("query");
    }

    private long manageData(Connection conn, ResultSet result) {
        long row_numbers = 0;
        long end_timestamp = System.currentTimeMillis();

        Util.logger.info("Posting rows");

        try {
            ResultSetMetaData resultMetaData = result.getMetaData();
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            int columnCount = resultMetaData.getColumnCount();

            while (result.next()) {
                Map row_values = new HashMap();
                List<String> tableNames = new ArrayList<>();
                Set<String> primaryKeyNames = new HashSet<>();

                for (int i = 1; i <= columnCount; i++) {
                    var object = result.getObject(i, Class.forName(resultMetaData.getColumnClassName(i)));
                    String columnName = resultMetaData.getColumnName(i);
                    row_values.put(columnName, object);

                    if (this._table == null) {
                        String tableName = resultMetaData.getTableName(i);
                        if (tableNames.contains(tableName) == false) {
                            tableNames.add(tableName);
                        }

                        ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(null, null, tableName);
                        while (primaryKeys.next()) {
                            primaryKeyNames.add(primaryKeys.getString("COLUMN_NAME"));
                        }
                    }
                }

                Map datasourcePayload = new HashMap() {{
                    put("row", row_values);
                }};

                String table;
                if (this._table == null) {
                    table = String.join(" ", tableNames);
                } else {
                    table = this._table;
                    ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(null, null, this._table);
                    while (primaryKeys.next()) {
                        primaryKeyNames.add(primaryKeys.getString("COLUMN_NAME"));
                    }
                }

                String primaryKeyStr = String.join(" ", primaryKeyNames);

                String rawContent = databaseMetaData.getDriverName() + " " + databaseMetaData.getUserName()
                        + " " + databaseMetaData.getDatabaseProductName() + " " +
                        table + " " + primaryKeyStr;

                Map payload = new HashMap() {{
                    put("datasourceId", _datasourceId);
                    put("scheduleId", _scheduleId);
                    put("contentId", primaryKeyStr);
                    put("parsingDate", end_timestamp);
                    put("rawContent", rawContent);
                    put("datasourcePayload", datasourcePayload);
                }};

                // Util.logger.info(datasourcePayload.toString());
                Util.logger.info(payload.toString());
                row_numbers++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Util.logger.error("Problems during accessing results");
        } catch (ClassNotFoundException e) {
            Util.logger.error("Problems during posting of row with id: ");
        }

        Util.logger.info("Posting ended");
        Util.logger.info("Have been posted " + row_numbers + " rows");

        return row_numbers;
    }

    public void extractRecent() {
        String url = this._jdbcUrl;
        if (this._jdbcUrl == null && (this._database == null && this._serverName == null && this._serverPort == null
                && this._databaseName == null)) {
            Util.logger.error("Failed to create url.");
            return;
        } else if (this._jdbcUrl == null) {
            switch (Objects.requireNonNull(this._database).toLowerCase()) {
                case "oracle":
                    // Can't test with Docker
                    url = String.format("jdbc:oracle:thin:[%s/%s]@%s[:%s]:%s", this._username, this._password,
                            this._serverName, this._serverPort, this._databaseName);
                    break;
                case "mysql":
                    // Works
                    url = String.format("jdbc:mysql://%s:%s/%s", this._serverName,
                            this._serverPort, this._databaseName);
                    break;
                case "mssql":
                    // Not Works
                    url = String.format("jdbc:sqlserver://%s:%s;databaseName=%s", this._serverName,
                            this._serverPort, this._databaseName);
                    break;
                case "postgresql":
                    // Works
                    url = String.format("jdbc:postgresql://%s:%s/%s", this._serverName, this._serverPort,
                            this._databaseName);
                    break;
                case "mariadb":
                    // Work
                    url = String.format("jdbc:mariadb://%s:%s/%s", this._serverName, this._serverPort,
                            this._databaseName);
                    break;
                default:
                    Util.logger.error("Failed to identify database: " + this._database);
                    return;
            }
        }

        try(Connection conn = DriverManager.getConnection(url, this._username, this._password)) {
            conn.setReadOnly(true);

            Statement statement = conn.createStatement();
            ResultSet result;
            if (_query == null && _columns == null) {
                result = statement.executeQuery("select * from " + _table);
            } else if(_query == null) {
                result = statement.executeQuery("select " + String.join(" ", _columns) + " from " + _table);
            } else {
                result = statement.executeQuery(this._query);
            }

            manageData(conn, result);
        } catch (SQLException e) {
            e.printStackTrace();
            Util.logger.error("No row extracted. Extraction process aborted.");
            return;
        }
        Util.logger.info("Extraction ended");
        return;
    }

    private final String _jdbcUrl;
    private final String _database;
    private final String _serverName;
    private final String _serverPort;
    private final String _databaseName;
    private final String _username;
    private final String _password;
    private final long _timestamp;
    private final long _datasourceId;
    private final String _scheduleId;
    private final String _table;
    private final List<String> _columns;
    private final String _query;
}
