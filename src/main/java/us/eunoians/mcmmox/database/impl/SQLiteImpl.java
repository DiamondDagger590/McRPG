package us.eunoians.mcmmox.database.impl;

import us.eunoians.mcmmox.database.Database;
import us.eunoians.mcmmox.database.Table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public class SQLiteImpl implements Database {


  @Override
  public void connectFunction() throws SQLException, ClassNotFoundException {

  }

  @Override
  public void closeConnection() throws SQLException {

  }

  @Override
  public void initializeTables(Collection<? super Table> tables) {

  }

  @Override
  public Connection getConnection() {
    return null;
  }
}
