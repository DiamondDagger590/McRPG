package us.eunoians.mcrpg.worldguard;

public class McRPGParser {


  public boolean evaluate(String sign, int first, int second) {
    boolean result = false;
    switch(sign) {
      case ">":
        if(first > second) {
          result = true;
        }
        break;
      case ">=":
      case "=>":
        if(first >= second) {
          result = true;
        }
        break;
      case "<":
        if(first < second) {
          result = true;
        }
        break;
      case "<=":
      case "=<":
        if(first <= second) {
          result = true;
        }
        break;
    }
    return result;
  }
}
