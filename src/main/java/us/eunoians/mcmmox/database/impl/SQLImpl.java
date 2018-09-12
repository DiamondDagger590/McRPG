package us.eunoians.mcmmox.database.impl;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.database.Database;
import us.eunoians.mcmmox.database.Table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;

public class SQLImpl implements Database {

  @Getter private Connection connection;
  @Setter private String host, database, username, password;
  @Setter private int port;

  public void setTables(Collection<Table> table) {
    tables.set(table);
  }

  @Override
  public void connectFunction() throws SQLException, ClassNotFoundException {
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

  @Override
  public void initializeTables(Collection<? super Table> tables) {

  }


}
