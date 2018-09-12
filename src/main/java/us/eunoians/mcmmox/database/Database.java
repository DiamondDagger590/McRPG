package us.eunoians.mcmmox.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p> Wrapper class to represent a database schema</p>
 */
public interface Database {

  AtomicReference<Collection<Table>> tables = new AtomicReference<>();

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

  // TODO: 9/11/18 make default because of redundancy.
  /**
   * <p> Tables that are going to be used with the database connection.</p>
   *
   * @param tables <p> Collection of {@link Table table} that will be used in the connection. </p>
   */
  void initializeTables(Collection<? super Table> tables);

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
  default void connect() throws SQLException, ClassNotFoundException {
    this.connectFunction();
    this.initializeTables(tables.get());
  }
}
