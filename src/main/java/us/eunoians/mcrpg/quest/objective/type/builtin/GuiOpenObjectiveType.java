package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;
import java.util.Optional;

/**
 * Built-in objective type for tracking when a player opens a specific GUI.
 * <p>
 * The required GUI type is configured via the {@code gui-type} key, which must be a valid
 * namespaced key matching the key of the target GUI. Because this is purely event-driven,
 * auto-complete is not supported.
 */
public class GuiOpenObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "gui_open");

    private final NamespacedKey guiType;

    /**
     * Creates an unconfigured base instance for registry registration.
     * The {@code guiType} field is {@code null} until {@link #parseConfig} is called.
     */
    public GuiOpenObjectiveType() {
        this.guiType = null;
    }

    private GuiOpenObjectiveType(@NotNull NamespacedKey guiType) {
        this.guiType = guiType;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public GuiOpenObjectiveType parseConfig(@NotNull Section section) {
        String rawGuiType = section.getString("gui-type", "");
        NamespacedKey parsedGuiType = NamespacedKey.fromString(rawGuiType);
        if (parsedGuiType == null) {
            McRPG.getInstance().getLogger().warning(
                    "gui_open objective has invalid or missing 'gui-type': '"
                            + rawGuiType + "' — objective will never match");
            parsedGuiType = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unknown");
        }
        return new GuiOpenObjectiveType(parsedGuiType);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof GuiOpenQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof GuiOpenQuestContext guiContext)) {
            return 0;
        }
        if (guiType == null) {
            return 0;
        }
        return guiContext.getGuiKey().filter(guiType::equals).isPresent() ? 1 : 0;
    }

    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String guiName = guiType != null ? formatKeySegment(guiType.getKey()) : "unknown";
        return localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_GUI_OPEN_FORMAT,
                Map.of("gui", guiName));
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    /**
     * Formats the last segment of a namespaced key into a human-readable title-cased string,
     * replacing underscores with spaces.
     *
     * @param key the raw key segment (e.g. {@code "ability_gui"})
     * @return a formatted string (e.g. {@code "Ability Gui"})
     */
    @NotNull
    private String formatKeySegment(@NotNull String key) {
        String[] words = key.replace('_', ' ').split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (!sb.isEmpty()) {
                    sb.append(' ');
                }
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1).toLowerCase());
                }
            }
        }
        return sb.toString();
    }
}
