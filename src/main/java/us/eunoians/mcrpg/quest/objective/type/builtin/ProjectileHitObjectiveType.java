package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Built-in objective type for tracking projectile hit progress.
 * <p>
 * Supports dual filtering: by projectile type (e.g. {@code ARROW}, {@code TRIDENT}) and by
 * hit entity type via McCore's {@link CustomEntityWrapper}. Config entries under
 * {@code projectiles} are {@link EntityType} names for the projectile. Config entries under
 * {@code entities} filter by the entity that was hit. Only hits on entities count (block/miss
 * hits are ignored). If both lists are empty, any entity hit by any projectile counts.
 */
public class ProjectileHitObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "projectile_hit");

    private final Set<EntityType> validProjectiles;
    private final Set<CustomEntityWrapper> validEntities;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public ProjectileHitObjectiveType() {
        this.validProjectiles = Set.of();
        this.validEntities = Set.of();
    }

    private ProjectileHitObjectiveType(@NotNull Set<EntityType> validProjectiles,
                                       @NotNull Set<CustomEntityWrapper> validEntities) {
        this.validProjectiles = validProjectiles;
        this.validEntities = validEntities;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public ProjectileHitObjectiveType parseConfig(@NotNull Section section) {
        Set<EntityType> projectiles = Set.of();
        if (section.contains("projectiles")) {
            projectiles = section.getStringList("projectiles").stream()
                    .map(s -> EntityType.valueOf(s.toUpperCase()))
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));
            projectiles = Set.copyOf(projectiles);
        }
        Set<CustomEntityWrapper> entities = Set.of();
        if (section.contains("entities")) {
            entities = section.getStringList("entities").stream()
                    .map(CustomEntityWrapper::new)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new ProjectileHitObjectiveType(projectiles, entities);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof ProjectileHitQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof ProjectileHitQuestContext hitContext)) {
            return 0;
        }

        Entity hitEntity = hitContext.getProjectileHitEvent().getHitEntity();
        if (hitEntity == null) {
            return 0;
        }

        if (!validProjectiles.isEmpty()
                && !validProjectiles.contains(hitContext.getProjectileHitEvent().getEntity().getType())) {
            return 0;
        }

        if (!validEntities.isEmpty()
                && !validEntities.contains(new CustomEntityWrapper(hitEntity))) {
            return 0;
        }

        return 1;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves a localized description via the player's locale chain. The primary description
     * focuses on the projectile type filter.
     */
    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String count = String.valueOf(requiredProgress);
        if (validProjectiles.isEmpty() && validEntities.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_PROJECTILE_HIT_ANY,
                    Map.of("count", count));
        }
        if (validProjectiles.size() == 1) {
            String projectileName = validProjectiles.iterator().next().name().toLowerCase().replace('_', ' ');
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_PROJECTILE_HIT_SINGLE,
                    Map.of("count", count, "projectile", projectileName));
        }
        if (!validProjectiles.isEmpty()) {
            StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_PROJECTILE_HIT_MULTI_HEADER, Map.of("count", count)));
            for (EntityType projectile : validProjectiles) {
                String projectileName = projectile.name().toLowerCase().replace('_', ' ');
                sb.append("\n").append(localization.getLocalizedMessage(player,
                        LocalizationKey.QUEST_OBJECTIVE_PROJECTILE_HIT_MULTI_ITEM, Map.of("projectile", projectileName)));
            }
            return sb.toString();
        }
        // Only entity filter set, describe using entity names
        if (validEntities.size() == 1) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_PROJECTILE_HIT_SINGLE,
                    Map.of("count", count, "projectile", validEntities.iterator().next().entityName()));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_PROJECTILE_HIT_MULTI_HEADER, Map.of("count", count)));
        for (CustomEntityWrapper entity : validEntities) {
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_PROJECTILE_HIT_MULTI_ITEM, Map.of("projectile", entity.entityName())));
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
