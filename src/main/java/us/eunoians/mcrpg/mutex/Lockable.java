package us.eunoians.mcrpg.mutex;

public interface Lockable {

    public boolean isLocked();

    public boolean setLocked(boolean locked);

    public default boolean lock(){
        return setLocked(true);
    }

    public default boolean unlock(){
        return setLocked(false);
    }
}
