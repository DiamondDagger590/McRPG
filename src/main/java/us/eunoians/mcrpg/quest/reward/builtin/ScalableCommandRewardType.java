package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * A variant of {@link CommandRewardType} that supports an {@code {amount}} placeholder
 * in the command template. When used in a split-mode distribution tier, the
 * {@code {amount}} token is replaced with the scaled amount at grant time.
 * <p>
 * Config format:
 * <pre>
 * type: mcrpg:scalable_command
 * command: "give {player} diamond {amount}"
 * base-amount: 10
 * </pre>
 */
public final class ScalableCommandRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "scalable_command");

    private final String commandTemplate;
    private final long baseAmount;
    private final String displayLabel;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public ScalableCommandRewardType() {
        this.commandTemplate = "";
        this.baseAmount = 0;
        this.displayLabel = "";
    }

    private ScalableCommandRewardType(@NotNull String commandTemplate, long baseAmount, @NotNull String displayLabel) {
        this.commandTemplate = commandTemplate;
        this.baseAmount = baseAmount;
        this.displayLabel = displayLabel;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public String describeForDisplay() {
        if (!displayLabel.isEmpty()) {
            return displayLabel + " (x" + baseAmount + ")";
        }
        return "Scaled Reward (x" + baseAmount + ")";
    }

    @NotNull
    @Override
    public QuestRewardType withAmountMultiplier(double multiplier) {
        long scaled = Math.max(1, Math.round(baseAmount * multiplier));
        return new ScalableCommandRewardType(commandTemplate, scaled, displayLabel);
    }

    @NotNull
    @Override
    public OptionalLong getNumericAmount() {
        return OptionalLong.of(baseAmount);
    }

    @Override
    public void grant(@NotNull Player player) {
        if (commandTemplate.isEmpty()) {
            return;
        }
        String resolved = commandTemplate
                .replace("{player}", player.getName())
                .replace("{amount}", String.valueOf(baseAmount));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
    }

    @NotNull
    @Override
    public ScalableCommandRewardType parseConfig(@NotNull Section section) {
        return new ScalableCommandRewardType(
                section.getString("command", ""),
                section.getLong("base-amount", 0L),
                section.getString("display", "")
        );
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("command", commandTemplate);
        map.put("base-amount", baseAmount);
        if (!displayLabel.isEmpty()) {
            map.put("display", displayLabel);
        }
        return map;
    }

    @NotNull
    @Override
    public ScalableCommandRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String cmd = config.getOrDefault("command", "").toString();
        long amt = config.containsKey("base-amount") ? ((Number) config.get("base-amount")).longValue() : 0;
        String label = config.getOrDefault("display", "").toString();
        return new ScalableCommandRewardType(cmd, amt, label);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
