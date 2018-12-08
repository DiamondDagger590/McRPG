package us.eunoians.mcrpg.database.models;

import lombok.Builder;
import lombok.Getter;
import us.eunoians.mcrpg.database.DataType;

@Builder @Getter
public class Column {

  @Builder.Default private String name = "row";
  @Builder.Default private DataType dataType = DataType.TEXT;
  @Builder.Default private String argument = "NOT NULL";

  public String toString(){
    return String.format("%s %s %s", getName(), getDataType(), getArgument());
  }
}
