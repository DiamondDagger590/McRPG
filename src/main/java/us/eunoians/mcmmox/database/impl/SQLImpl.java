package us.eunoians.mcmmox.database.impl;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import us.eunoians.mcmmox.database.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Builder(builderClassName = "Builder")
public class SQLImpl implements Database {

  @Getter private Connection connection;
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

}
