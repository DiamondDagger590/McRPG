package us.eunoians.mcmmox.database;

import java.sql.SQLException;

public interface Database {

  /**
   * Connect method for each Database driver.
   */
  void connect() throws SQLException, ClassNotFoundException;


}
