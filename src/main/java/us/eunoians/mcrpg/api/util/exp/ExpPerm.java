package us.eunoians.mcrpg.api.util.exp;

import lombok.Getter;
import us.eunoians.mcrpg.types.Skills;

import java.util.HashMap;

public class ExpPerm {

  @Getter private String perm;

  @Getter private HashMap<Skills, Double> expValues;

  @Getter private int priority;

  public ExpPerm(String perm, HashMap<Skills, Double> expValues, int priority){
    this.perm = perm;
    this.expValues = expValues;
    this.priority = priority;
  }
}
