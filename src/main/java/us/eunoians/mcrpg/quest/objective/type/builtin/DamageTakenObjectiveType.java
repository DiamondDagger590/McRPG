package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Built-in objective type for tracking damage taken by the player.
 * <p>
 * Config entries under {@code causes} are {@link EntityDamageEvent.DamageCause} names
 * (e.g. {@code FALL}, {@code FIRE}). If the list is empty, any damage cause counts toward
 * progress. The progress delta equals the rounded final damage amount.
 */
public class DamageTakenObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "damage_taken");

    private final Set<EntityDamageEvent.DamageCause> validCauses;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public DamageTakenObjectiveType() {
        this.validCauses = Set.of();
    }

    private DamageTakenObjectiveType(@NotNull Set<EntityDamageEvent.DamageCause> validCauses) {
        this.validCauses = validCauses;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public DamageTakenObjectiveType parseConfig(@NotNull Section section) {
        Set<EntityDamageEvent.DamageCause> causes = Set.of();
        if (section.contains("causes")) {
            causes = section.getStringList("causes").stream()
                    .map(s -> EntityDamageEvent.DamageCause.valueOf(s.toUpperCase()))
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new DamageTakenObjectiveType(causes);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof DamageTakenQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof DamageTakenQuestContext damageContext)) {
            return 0;
        }

        long delta = Math.round(damageContext.getDamageEvent().getFinalDamage());
        if (delta <= 0) {
            return 0;
        }

        if (validCauses.isEmpty()) {
            return delta;
        }

        return validCauses.contains(damageContext.getDamageEvent().getCause()) ? delta : 0;
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
        if (validCauses.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_DAMAGE_TAKEN_ANY,
                    Map.of("count", count));
        }
        if (validCauses.size() == 1) {
            String causeName = validCauses.iterator().next().name().toLowerCase().replace('_', ' ');
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_DAMAGE_TAKEN_SINGLE,
                    Map.of("count", count, "cause", causeName));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_DAMAGE_TAKEN_MULTI_HEADER, Map.of("count", count)));
        for (EntityDamageEvent.DamageCause cause : validCauses) {
            String causeName = cause.name().toLowerCase().replace('_', ' ');
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_DAMAGE_TAKEN_MULTI_ITEM, Map.of("cause", causeName)));
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
