package us.eunoians.mcrpg.database.tables;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "loadout")
public class Loadout {
  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;
}
