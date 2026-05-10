package us.eunoians.mcrpg.ability.combo;

 import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;

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
     * Returns the 1-based slot index this pattern is permanently bound to.
     * Slot 1 always activates with {@link #SLOT_1}, slot 2 with {@link #SLOT_2}, and so on —
     * regardless of which ability the player has placed in that slot.
     *
     * @return The 1-based slot index for this pattern.
     */
    public int getSlotIndex() {
        return slotIndex;
    }

    /**
     * Returns the full ordered list of {@link ComboInput}s for this pattern.
     *
     * @return An immutable list of {@link ComboInput}s in activation order.
     */
    @NotNull
    public List<ComboInput> getInputs() {
        return inputs;
    }

    /**
     * Returns the total number of clicks required to complete this pattern.
     *
     * @return The number of clicks required to complete this pattern.
     */
    public int getLength() {
        return inputs.size();
    }

    /**
     * Returns {@code true} if the provided sequence is a valid non-empty prefix of this pattern.
     * An empty sequence is not considered a valid prefix.
     *
     * @param current The input sequence to test.
     * @return {@code true} if {@code current} is a valid non-empty prefix of this pattern's input list.
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
     * Returns {@code true} if the provided sequence exactly matches this pattern in both
     * length and input order.
     *
     * @param current The input sequence to test.
     * @return {@code true} if {@code current} is a complete match for this pattern.
     */
    public boolean isCompleteMatch(@NotNull List<ComboInput> current) {
        return current.size() == inputs.size() && isValidPrefix(current);
    }

    /**
     * Finds the {@link ComboPattern} whose 1-based slot index matches the given value.
     *
     * @param slotIndex The 1-based slot index to look up.
     * @return The matching {@link ComboPattern}, or {@code null} if no pattern has that slot index.
     */
    @Nullable
    public static ComboPattern forSlot(int slotIndex) {
        for (ComboPattern pattern : values()) {
            if (pattern.slotIndex == slotIndex) {
                return pattern;
            }
        }
        return null;
    }

    /**
     * Returns the {@link Route} for this pattern's localizable display string.
     * <p>
     * The display string (e.g. {@code "<yellow>R<dark_gray> → <yellow>R<dark_gray> → <yellow>R"}) is
     * defined in the locale YAML under {@code loadout-gui.active-combo-slot.pattern.*}, giving
     * server owners full control over the colours and characters used.
     *
     * @return The {@link Route} for this pattern's display string.
     */
    @NotNull
    public Route getLocalizationKey() {
        return switch (this) {
            case SLOT_1 -> LocalizationKey.LOADOUT_GUI_ACTIVE_COMBO_SLOT_PATTERN_SLOT_1;
            case SLOT_2 -> LocalizationKey.LOADOUT_GUI_ACTIVE_COMBO_SLOT_PATTERN_SLOT_2;
            case SLOT_3 -> LocalizationKey.LOADOUT_GUI_ACTIVE_COMBO_SLOT_PATTERN_SLOT_3;
        };
    }

    /**
     * Returns all defined patterns in declaration (slot) order.
     * <p>
     * The returned list mirrors the enum's natural order: {@link #SLOT_1}, {@link #SLOT_2},
     * {@link #SLOT_3}. Use this instead of {@link #values()} when an ordered {@link List} is
     * needed without an array copy at each call site.
     *
     * @return An immutable-backed list of all {@link ComboPattern} values in slot order.
     */
    @NotNull
    public static List<ComboPattern> allPatterns() {
        return Arrays.asList(values());
    }
}
