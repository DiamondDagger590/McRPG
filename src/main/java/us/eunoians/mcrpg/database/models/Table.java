package us.eunoians.mcrpg.database.models;

import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Table {

  public static final String TABLE_PREFIX = "mcmmox_";

  private Connection connection;
  private String tableName;
  private String cols;

  public Table(Connection connection, String tableName, Column... columns) {
    this.connection = connection;
    this.tableName = TABLE_PREFIX + tableName;
    this.cols = StringUtils.join(Arrays.stream(columns).map(Column::toString).collect(Collectors.toList()), ",");
  }

  public void initializeTable() {
    try {
      String statement = String.format("CREATE TABLE IF NOT EXISTS `%s` ( %s )", tableName, cols);
      connection.createStatement().execute(statement);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
