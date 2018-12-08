package us.eunoians.mcrpg.database.impl;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import us.eunoians.mcrpg.database.Database;
import us.eunoians.mcrpg.database.Driver;
import us.eunoians.mcrpg.database.models.Table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteImpl implements Database {

  @Getter private Connection connection;
  private String path;

  @Override
  public void connectFunction() throws SQLException, ClassNotFoundException {
    if (StringUtils.isBlank(path))
      throw new SQLException("The path for the database was left blank.");
      if (connection != null && !connection.isClosed()) {
        return;
      }
    synchronized (this) {
      if (connection != null && !connection.isClosed()) {
        return;
      }
      Class.forName("com.mysql.jdbc.Driver");
      connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", path));
    }
  }

  @Override
  public void closeConnection() throws SQLException {
    if (connection != null && connection.isClosed())
      connection.close();
  }

  /**
   * Set the path for this {@link SQLiteImpl SQLiteImpl}.
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
   * @return current instance of {@link SQLImpl SQLImpl}.
   */
  public SQLiteImpl setPath(String path) {
    this.path = path;
    return this;
  }


  /**
   * <p> Setup the tables that are going to be used by the class.</p>
   *
   * @param table {@link Table Table} that is going
   *              to be used in this database wrapper.
   */
  public SQLiteImpl setTables(Table table) {
    this.table.set(table);
    return this;
  }

}
