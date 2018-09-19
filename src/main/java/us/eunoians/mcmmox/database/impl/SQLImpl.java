package us.eunoians.mcmmox.database.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import us.eunoians.mcmmox.database.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLImpl implements Database {

  @Getter private Connection connection;
  @Setter private String host, database, username, password;
  @Setter private int port;

  @Override
  public void connectFunction() throws SQLException, ClassNotFoundException {
    if(StringUtils.isBlank(host) || StringUtils.isBlank(host) ||
            StringUtils.isBlank(host) || StringUtils.isBlank(host))
      // TODO: 9/13/18 throw some exception here!
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
