package us.eunoians.mcrpg.mutex;

public abstract class Mutexable implements Lockable {

    private boolean locked;

    public Mutexable() {
        this.locked = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean setLocked(boolean locked) {

        if(this.locked == locked){
            return false;
        }

        this.locked = locked;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isLocked() {
        return locked;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean lock() {
        return Lockable.super.lock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean unlock() {
        return Lockable.super.unlock();
    }
}
