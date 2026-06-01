package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Built-in objective type for tracking projectile launch progress.
 * <p>
 * Config entries under {@code projectiles} are {@link EntityType} names for the projectile
 * entity (e.g. {@code ARROW}, {@code SNOWBALL}). If the list is empty, any projectile
 * launch counts toward progress.
 */
public class LaunchProjectileObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "launch_projectile");

    private final Set<EntityType> validProjectiles;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public LaunchProjectileObjectiveType() {
        this.validProjectiles = Set.of();
    }

    private LaunchProjectileObjectiveType(@NotNull Set<EntityType> validProjectiles) {
        this.validProjectiles = validProjectiles;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public LaunchProjectileObjectiveType parseConfig(@NotNull Section section) {
        Set<EntityType> projectiles = Set.of();
        if (section.contains("projectiles")) {
            projectiles = section.getStringList("projectiles").stream()
                    .map(s -> EntityType.valueOf(s.toUpperCase()))
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));
            projectiles = Set.copyOf(projectiles);
        }
        return new LaunchProjectileObjectiveType(projectiles);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof LaunchProjectileQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof LaunchProjectileQuestContext launchContext)) {
            return 0;
        }

        if (validProjectiles.isEmpty()) {
            return 1;
        }

        return validProjectiles.contains(launchContext.getLaunchEvent().getEntity().getType()) ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves a localized description via the player's locale chain.
     */
    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String count = String.valueOf(requiredProgress);
        if (validProjectiles.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_LAUNCH_PROJECTILE_ANY,
                    Map.of("count", count));
        }
        if (validProjectiles.size() == 1) {
            String projectileName = validProjectiles.iterator().next().name().toLowerCase().replace('_', ' ');
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_LAUNCH_PROJECTILE_SINGLE,
                    Map.of("count", count, "projectile", projectileName));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_LAUNCH_PROJECTILE_MULTI_HEADER, Map.of("count", count)));
        for (EntityType projectile : validProjectiles) {
            String projectileName = projectile.name().toLowerCase().replace('_', ' ');
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_LAUNCH_PROJECTILE_MULTI_ITEM, Map.of("projectile", projectileName)));
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
