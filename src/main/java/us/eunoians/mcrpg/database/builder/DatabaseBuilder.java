/*
 * MIT License
 *
 * Copyright (c) 2019 Ethan Bacurio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package us.eunoians.mcrpg.database.builder;

import lombok.Getter;

import java.sql.SQLException;

public class DatabaseBuilder {

  @Getter private String path;
  @Getter private String driverName;

  private String connectionURL;

  public DatabaseBuilder(DatabaseDriver databaseDriver) {
    path = "";
    connectionURL = databaseDriver.getConnectionURL();

    tryDriverName(databaseDriver.getDatabaseDriverClass());
  }

  public DatabaseBuilder setPath(String path) {
    this.path = path;
    return this;
  }

  private void tryDriverName(String driverName) {

    try {
      Class.forName(driverName).newInstance();
      this.driverName = driverName.split("\\.")[1];
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  public String getConnectionURL() {
    return String.format(connectionURL, driverName, path);
  }

  public Database build() throws SQLException {
    if(FastStrings.isBlank(driverName)) throw new SQLException("The driver name was left empty!");
    connectionURL = getConnectionURL();

    return new FlatDatabase(this);
  }
}
