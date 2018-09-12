package us.eunoians.mcmmox.database;

import java.sql.ResultSet;

public interface Table {

  /**
   * <p> Get the row of a table using any of its column. </p>
   *
   * Usage sample:
   * <pre>
   *   getRow("id", 10);
   * </pre>
   *
   * translates to...
   *
   * <p> SELECT * FROM 'table' WHERE 'id' = 10 </p>
   *
   * @param target   <p> Target column where you want match your argument. </p>
   * @param argument <p> Argument that will be used to match a column. </p>
   * @return ResultSet of the query.
   */
  ResultSet getRow(String target, String argument);
}
