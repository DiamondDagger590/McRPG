package us.eunoians.mcrpg.util.worldguard;

/**
 * Handles parsing two numbers and comparing them
 *
 * @author DiamondDagger590
 */
public class McRPGParser {

  /**
   * Compares two numbers and returns the result
   * @param comparator The comparator. Can be something such as {@code ">"}
   * @param first The first int to be compared
   * @param second The second int to be compared
   * @return {@code true} if the provided integers compare true using the provided comparator
   */
  public boolean evaluate(String comparator, int first, int second) {
    boolean result = false;
    switch(comparator) {
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
