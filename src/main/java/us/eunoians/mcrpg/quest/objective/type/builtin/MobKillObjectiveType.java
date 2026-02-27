package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Built-in objective type for tracking mob kill progress.
 * <p>
 * Supports both vanilla entity types and custom entities from plugins integrated via McCore's
 * {@link CustomEntityWrapper}. Config entries under {@code entities} can be vanilla entity type
 * names (e.g. {@code ZOMBIE}) or custom entity identifiers from supported plugins. If the list
 * is empty, any mob kill counts toward progress.
 */
public class MobKillObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "mob_kill");

    private final Set<CustomEntityWrapper> validEntities;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public MobKillObjectiveType() {
        this.validEntities = Set.of();
    }

    private MobKillObjectiveType(@NotNull Set<CustomEntityWrapper> validEntities) {
        this.validEntities = validEntities;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public MobKillObjectiveType parseConfig(@NotNull Section section) {
        Set<CustomEntityWrapper> entities = Set.of();
        if (section.contains("entities")) {
            entities = section.getStringList("entities").stream()
                    .map(CustomEntityWrapper::new)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new MobKillObjectiveType(entities);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof MobKillQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof MobKillQuestContext mobContext)) {
            return 0;
        }

        if (validEntities.isEmpty()) {
            return 1;
        }

        return validEntities.contains(mobContext.getEntityWrapper()) ? 1 : 0;
    }

    @NotNull
    @Override
    public String describeObjective(long requiredProgress) {
        if (validEntities.isEmpty()) {
            return "Kill " + requiredProgress + " mobs";
        }
        String targets = validEntities.stream()
                .map(e -> formatName(e.toString()))
                .collect(Collectors.joining(", "));
        return "Kill " + requiredProgress + " " + targets;
    }

    private static String formatName(String raw) {
        String[] parts = raw.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
