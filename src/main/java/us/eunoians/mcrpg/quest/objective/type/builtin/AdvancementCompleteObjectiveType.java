package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Built-in objective type for tracking advancement completion progress.
 * <p>
 * Config entries under {@code advancements} are advancement keys in namespaced format
 * (e.g. {@code minecraft:story/iron_tools}). If the list is empty, any advancement
 * completion counts toward progress. Supports retroactive progress via
 * {@link #checkInitialProgress(Player)} for advancements already completed before the
 * quest was assigned.
 */
public class AdvancementCompleteObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "advancement_complete");

    private final Set<String> validAdvancements;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public AdvancementCompleteObjectiveType() {
        this.validAdvancements = Set.of();
    }

    private AdvancementCompleteObjectiveType(@NotNull Set<String> validAdvancements) {
        this.validAdvancements = validAdvancements;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public AdvancementCompleteObjectiveType parseConfig(@NotNull Section section) {
        Set<String> advancements = Set.of();
        if (section.contains("advancements")) {
            advancements = section.getStringList("advancements").stream()
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new AdvancementCompleteObjectiveType(advancements);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof AdvancementCompleteQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof AdvancementCompleteQuestContext advContext)) {
            return 0;
        }

        if (validAdvancements.isEmpty()) {
            return 1;
        }

        String advancementKey = advContext.getAdvancementEvent().getAdvancement().getKey().toString();
        return validAdvancements.contains(advancementKey) ? 1 : 0;
    }

    /**
     * Checks whether any advancements tracked by this objective were already completed
     * by the player before the quest was assigned, granting retroactive initial progress.
     *
     * @param player the player starting the quest
     * @return the number of already-completed advancements that match this objective's filter
     */
    @Override
    public long checkInitialProgress(@NotNull Player player) {
        long count = 0;
        if (validAdvancements.isEmpty()) {
            Iterator<Advancement> iterator = Bukkit.advancementIterator();
            while (iterator.hasNext()) {
                Advancement advancement = iterator.next();
                if (advancement.getDisplay() != null && player.getAdvancementProgress(advancement).isDone()) {
                    count++;
                }
            }
        } else {
            for (String key : validAdvancements) {
                NamespacedKey namespacedKey = NamespacedKey.fromString(key);
                if (namespacedKey == null) {
                    continue;
                }
                Advancement advancement = Bukkit.getAdvancement(namespacedKey);
                if (advancement != null && player.getAdvancementProgress(advancement).isDone()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves a localized description via the player's locale chain. Uses the path portion
     * of the advancement key as the display name.
     */
    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String count = String.valueOf(requiredProgress);
        if (validAdvancements.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_ANY,
                    Map.of("count", count));
        }
        if (validAdvancements.size() == 1) {
            String advancementName = extractDisplayName(validAdvancements.iterator().next());
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_SINGLE,
                    Map.of("count", count, "advancement", advancementName));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_MULTI_HEADER, Map.of("count", count)));
        for (String advancement : validAdvancements) {
            String advancementName = extractDisplayName(advancement);
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_MULTI_ITEM, Map.of("advancement", advancementName)));
        }
        return sb.toString();
    }

    /**
     * Extracts a human-readable display name from an advancement key by taking the path
     * portion after the colon and replacing slashes and underscores with spaces.
     *
     * @param advancementKey the full namespaced advancement key (e.g. {@code minecraft:story/iron_tools})
     * @return the formatted display name (e.g. {@code story/iron tools})
     */
    @NotNull
    private String extractDisplayName(@NotNull String advancementKey) {
        int colonIndex = advancementKey.indexOf(':');
        String path = colonIndex >= 0 ? advancementKey.substring(colonIndex + 1) : advancementKey;
        return MiniMessage.miniMessage().escapeTags(path.replace('_', ' '));
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
