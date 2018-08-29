package us.eunoians.mcmmox.api.configuration;

/**
 * Represents an object in the configuration file.
 */
public interface Node {

    /**
     * <p>Quarry the default value of the node
     * after the initial file generation.</p>
     *
     * @return default value of the node.
     */
    Object getDefaultValue();

    String[] getComment();

    String key();

}
