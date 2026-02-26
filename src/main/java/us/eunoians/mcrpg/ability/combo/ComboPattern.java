package us.eunoians.mcrpg.ability.combo;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static us.eunoians.mcrpg.ability.combo.ComboInput.LEFT;
import static us.eunoians.mcrpg.ability.combo.ComboInput.RIGHT;

/**
 * Represents one of the three hard-coded combo patterns that map to an active ability slot.
 * <p>
 * All patterns start with RIGHT so that left-click-only sequences (mining, combat)
 * can never accidentally initiate a combo.
 * <p>
 * Slot assignment is slot-tied: the pattern is determined by the slot index, not the ability.
 * Whichever ability a player puts in slot 1 always activates with {@link #SLOT_1}.
 */
public enum ComboPattern {

    /** Slot 1: Right-Right-Right — aggressive burst feel */
    SLOT_1(1, RIGHT, RIGHT, RIGHT),

    /** Slot 2: Right-Right-Left — setup and release feel */
    SLOT_2(2, RIGHT, RIGHT, LEFT),

    /** Slot 3: Right-Left-Right — deliberate alternating feel */
    SLOT_3(3, RIGHT, LEFT, RIGHT);

    private final int slotIndex;
    private final List<ComboInput> inputs;

    ComboPattern(int slotIndex, @NotNull ComboInput... inputs) {
        this.slotIndex = slotIndex;
        this.inputs = List.of(inputs);
    }

    /**
     * Returns the 1-based slot index this pattern corresponds to.
     */
    public int getSlotIndex() {
        return slotIndex;
    }

    /**
     * Returns the full ordered list of inputs for this pattern.
     */
    @NotNull
    public List<ComboInput> getInputs() {
        return inputs;
    }

    /**
     * Returns the total number of clicks required to complete this pattern.
     */
    public int getLength() {
        return inputs.size();
    }

    /**
     * Returns {@code true} if the provided sequence is a valid non-empty prefix of this pattern.
     * An empty sequence is not considered a valid prefix.
     */
    public boolean isValidPrefix(@NotNull List<ComboInput> current) {
        if (current.isEmpty() || current.size() > inputs.size()) {
            return false;
        }
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i) != inputs.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if the provided sequence exactly matches this pattern.
     */
    public boolean isCompleteMatch(@NotNull List<ComboInput> current) {
        return current.size() == inputs.size() && isValidPrefix(current);
    }

    /**
     * Finds the {@link ComboPattern} whose slot index matches the given value, or {@code null}.
     */
    public static ComboPattern forSlot(int slotIndex) {
        for (ComboPattern pattern : values()) {
            if (pattern.slotIndex == slotIndex) {
                return pattern;
            }
        }
        return null;
    }

    /**
     * Returns all patterns in slot order.
     */
    @NotNull
    public static List<ComboPattern> allPatterns() {
        return Arrays.asList(values());
    }
}
