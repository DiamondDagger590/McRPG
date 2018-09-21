package us.eunoians.mcmmox.database;

import us.eunoians.mcmmox.database.models.Table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p> Wrapper class to represent a database schema</p>
 */
public interface Database {

  AtomicReference<Table> table = new AtomicReference<>();

  /**
   * Connect method for different kinds of JDBC driver.
   *
   * @throws SQLException           <P> An exception that provides information on a database access
   *                                error or other errors.</p>
   * @throws ClassNotFoundException <p> An exception that provides an error stacktrace when
   *                                the JDBC driver could not be found</p>
   */
  void connectFunction() throws SQLException, ClassNotFoundException;

  /**
   * Close connection with database.
   *
   * @throws SQLException <P> An exception that provides information on a database access
   *                      error or other errors.</p>
   */
  void closeConnection() throws SQLException;


  /**
   * <p> A connection (session) with a specific
   * database. SQL statements are executed and results are returned
   * within the context of a connection.
   * </p>
   *
   * @return Current connection of the database.
   */
  Connection getConnection();



  /**
   * <p> Connect method to invoke {@link Database#connectFunction() Database#connectFunction()} when
   * connection have been built. </p>
   *
   * @throws SQLException           <P> An exception that provides information on a database access
   *                                error or other errors.</p>
   * @throws ClassNotFoundException <p> An exception that provides an error stacktrace when
   *                                the JDBC driver could not be found</p>
   */
  default Database connect() throws SQLException, ClassNotFoundException {
    this.connectFunction();
    this.initializeTables(table.get());
    return this;
  }

  /**
   * <p> Initializes tables that are set.</p>
   *
   * @param table <p>{@link Table table} that will be used in the connection. </p>
   */
  default void initializeTables(Table table) {
    table.initializeTable();
  }
}
