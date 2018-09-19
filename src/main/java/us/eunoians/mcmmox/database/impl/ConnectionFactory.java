package us.eunoians.mcmmox.database.impl;


import org.apache.commons.lang.StringUtils;
import us.eunoians.mcmmox.database.Database;
import us.eunoians.mcmmox.database.Driver;

import java.sql.SQLException;

public class ConnectionFactory {

  private Driver driver;
  private String host, database, username, password, path;
  private int port;

  /**
   * Default constructor for {@link ConnectionFactory ConnectionFactory}.
   *
   * <p>Initialize every variable with a default value to prevent
   * {@link NullPointerException NullPointerException}.</p>
   */
  public ConnectionFactory() {
    driver = Driver.SQL;
    port = 0;
    host = database = username = password = path = "";
  }

  /**
   * Set the {@link Driver Driver} for this {@link ConnectionFactory ConnectionFactory}.
   *
   * <p> SQLite only needs the path to be set. SQL needs the database
   * credentials.</p>
   *
   * @param driver {@link Driver Driver} that you wish to use.
   * @return current instance of {@link ConnectionFactory ConnectionFactory}.
   */
  public ConnectionFactory setDriver(Driver driver) {
    this.driver = driver;
    return this;
  }

  /**
   * Set the host for the this {@link ConnectionFactory ConnectionFactory}.
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
   * @return current instance of {@link ConnectionFactory ConnectionFactory}.
   */
  public ConnectionFactory setHost(String host) {
    this.host = host;
    return this;
  }

  /**
   * Set the database for this {@link ConnectionFactory ConnectionFactory}.
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
   * @return current instance of {@link ConnectionFactory ConnectionFactory}.
   */
  public ConnectionFactory setDatabase(String database) {
    this.database = database;
    return this;
  }

  /**
   * Set the username for this {@link ConnectionFactory ConnectionFactory}.
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
   * @return current instance of {@link ConnectionFactory ConnectionFactory}.
   */
  public ConnectionFactory setUsername(String username) {
    this.username = username;
    return this;
  }

  /**
   * Set the password for this {@link ConnectionFactory ConnectionFactory}.
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
   * @return current instance of {@link ConnectionFactory ConnectionFactory}.
   */
  public ConnectionFactory setPassword(String password) {
    this.password = password;
    return this;
  }

  /**
   * Set port for this {@link ConnectionFactory ConnectionFactory}.
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
   * @return current instance of {@link ConnectionFactory ConnectionFactory}.
   */
  public ConnectionFactory setPort(int port) {
    this.port = port;
    return this;
  }

  /**
   * Set the path for this {@link ConnectionFactory ConnectionFactory}.
   *
   * <p> This is the path for the database that will be used for the
   * SQLite connection.</p>
   *
   * <p> Throws {@link java.sql.SQLException SQLException} when used with
   * {@link Driver#SQL SQL} Driver.</p>
   * <p> Throws {@link java.sql.SQLException SQLException} when the provided
   * path is invalid.</p>
   *
   * @param path database path.
   * @return current instance of {@link ConnectionFactory ConnectionFactory}.
   */
  public ConnectionFactory setPath(String path) {
    this.path = path;
    return this;
  }

  /**
   * Build this {@link ConnectionFactory ConnectionFactory}.
   *
   * <p> This just builds the connection that's going to be used to
   * connect to the database. After this, the developer must initialize
   * the tables that are going to be used during the session and manually
   * connect to the database by calling {@link Database#connect()
   * Database#connect()} function. </p>
   *
   * @return {@link Database Database} disconnected representation of
   * the connection.
   */
  public Database build() throws SQLException {
    switch (driver) {
      case SQL: {
        if (!StringUtils.isBlank(path))
          throw new SQLException("Path cannot be used when using SQL driver");
        if(StringUtils.isBlank(host) || StringUtils.isBlank(host) || StringUtils.isBlank(host) || StringUtils.isBlank(host))
          throw new SQLException("One of the credentials provided is blank.");
        return new SQLImpl.Builder().host(host).port(port).database(database).username(username).password(password).build();
      }
      case SQLite: {
        if(!StringUtils.isBlank(host) || !StringUtils.isBlank(host) || !StringUtils.isBlank(host) || !StringUtils.isBlank(host))
          throw new SQLException("Only path can be used when using SQLite driver.");
        if(StringUtils.isBlank(path))
          throw new SQLException("There was no path provided.");
        return new SQLiteImpl.Builder().path(path).build();
      }
      default:
        return null;
    }
  }
}
