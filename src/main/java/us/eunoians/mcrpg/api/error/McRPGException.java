package us.eunoians.mcrpg.api.error;

/**
 * This is the abstract version of an {@link Exception} that represents a generic form of all
 * custom errors that this plugin will throw
 *
 * @author DiamondDagger590
 */
public abstract class McRPGException extends Exception {

    public McRPGException(String reason) {
        super(reason);
    }
}
