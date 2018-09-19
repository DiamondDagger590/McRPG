package us.eunoians.mcmmox.database.impl;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import us.eunoians.mcmmox.database.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Builder(builderClassName = "Builder")
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

}
