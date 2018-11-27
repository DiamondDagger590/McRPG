package us.eunoians.mcmmox.database.impl;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import us.eunoians.mcmmox.database.Database;
import us.eunoians.mcmmox.database.Driver;
import us.eunoians.mcmmox.database.models.Table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLImpl implements Database {

  @Getter
  private Connection connection;
  private String host, database, username, password;
  private int port;

  @Override
  public void connectFunction() throws SQLException, ClassNotFoundException {
    if (StringUtils.isBlank(host) || StringUtils.isBlank(host) ||
            StringUtils.isBlank(host) || StringUtils.isBlank(host))
      throw new SQLException("One of the credentials provided is blank.");
    if (connection != null && !connection.isClosed()) {
      return;
    }
    synchronized (this) {
      if (connection != null && !connection.isClosed()) {
        return;
      }
      Class.forName("com.mysql.jdbc.Driver");
      connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
    }
  }

  @Override
  public void closeConnection() throws SQLException {
    if (connection != null && connection.isClosed())
      connection.close();
  }

  /**
   * Set the host for the this {@link SQLImpl SQLImpl}.
   *
   * <p>Host can be also called the IP address of the database. If the database
   * server is hosted locally, use 'localhost'.</p>
   *
   * <p> Throws {@link java.sql.SQLException SQLException} when used with
   * {@link Driver#SQLite SQLite} Driver.</p>
   * <p> Throws {@link java.sql.SQLException SQLException} when the provided
   * host is invalid.</p>
   *
   * @param host address of the database server.
   * @return current instance of {@link SQLImpl SQLImpl}.
   */
  public SQLImpl setHost(String host) {
    this.host = host;
    return this;
  }

  /**
   * Set the database for this {@link SQLImpl SQLImpl}.
   *
   * <p> Name of database that you wish to connect to. This is not the same as
   * tables.</p>
   *
   * <p> Throws {@link java.sql.SQLException SQLException} when used with
   * {@link Driver#SQLite SQLite} Driver.</p>
   * <p> Throws {@link java.sql.SQLException SQLException} when the provided
   * database is invalid.</p>
   *
   * @param database name of database.
   * @return current instance of {@link SQLImpl SQLImpl}.
   */
  public SQLImpl setDatabase(String database) {
    this.database = database;
    return this;
  }

  /**
   * Set the username for this {@link SQLImpl SQLImpl}.
   *
   * <p> Username is a credential of a user that has access to the database.
   * Username must be provided to successfully connect to the database server.
   * </p>
   *
   * <p> Throws {@link java.sql.SQLException SQLException} when used with
   * {@link Driver#SQLite SQLite} Driver.</p>
   * <p> Throws {@link java.sql.SQLException SQLException} when the provided
   * username is invalid.</p>
   *
   * @param username username of the user that has access to the database.
   * @return current instance of {@link SQLImpl SQLImpl}.
   */
  public SQLImpl setUsername(String username) {
    this.username = username;
    return this;
  }

  /**
   * Set the password for this {@link SQLImpl SQLImpl}.
   *
   * <p> password is a credential of a user that has access to the database.
   * password must be provided to successfully connect to the database server.
   * </p>
   *
   * <p> Throws {@link java.sql.SQLException SQLException} when used with
   * {@link Driver#SQLite SQLite} Driver.</p>
   * <p> Throws {@link java.sql.SQLException SQLException} when the provided
   * password is invalid.</p>
   *
   * @param password password of the user that has access to the database.
   * @return current instance of {@link SQLImpl SQLImpl}.
   */
  public SQLImpl setPassword(String password) {
    this.password = password;
    return this;
  }

  /**
   * Set port for this {@link SQLImpl SQLImpl}.
   *
   * <p>Port specifies which port to use when the class tries to connect
   * to the host. The default host for SQL is '3306'</p>
   *
   * <p> Throws {@link java.sql.SQLException SQLException} when used with
   * {@link Driver#SQLite SQLite} Driver.</p>
   * <p> Throws {@link java.sql.SQLException SQLException} when the provided
   * password is invalid.</p>
   *
   * @param port port of the database.
   * @return current instance of {@link SQLImpl SQLImpl}.
   */
  public SQLImpl setPort(int port) {
    this.port = port;
    return this;
  }

  /**
   * <p> Setup the tables that are going to be used by the class.</p>
   *
   * @param table {@link Table Table} that is going
   *              to be used in this database wrapper.
   */
  public SQLImpl setTables(Table table) {
    this.table.set(table);
    return this;
  }

}
