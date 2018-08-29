package us.eunoians.mcmmox.api.configuration;

import java.util.List;

/**
 * Interface for all of the configuration files.
 */
public interface IConfig {

  /**
   * <p>Get a {@link String String} object from a configuration.</p>
   *
   * @param node Node representation of the object.
   * @return the value of the Node in the configuration.
   */
  String getString(Node node);

  /**
   * <p>Get a {@link Boolean boolean} object from a configuration.</p>
   *
   * @param node Node representation of the object.
   * @return the value of the Node in the configuration.
   */
  boolean getBoolean(Node node);

  /**
   * <p>Get a {@link Integer int} object from a configuration.</p>
   *
   * @param node Node representation of the string object.
   * @return the value of the Node in the configuration.
   */
  int getInt(Node node);

  /**
   * <p>Get a {@link List List<E>} object from a configuration.</p>
   *
   * @param node Node representation of the string object.
   * @return the value of the Node in the configuration.
   */
  List getList(Node node);

  double getDouble(Node node);
}
