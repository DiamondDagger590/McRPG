package us.eunoians.mcrpg.ability.combo;

/**
 * Represents a single click input in a combo sequence.
 * <p>
 * All combos must start with {@link #RIGHT} — left-click-only sequences can
 * never accidentally initiate a combo during normal gameplay.
 */
public enum ComboInput {
    RIGHT,
    LEFT
}
