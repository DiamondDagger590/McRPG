package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
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

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Built-in objective type for tracking distance traveled by the player.
 * <p>
 * Config entries under {@code modes} are {@link TravelMode} names (e.g. {@code FOOT},
 * {@code HORSE}, {@code ELYTRA}). If the list is empty, distance traveled by any mode
 * counts toward progress. The progress delta equals the pre-computed block distance
 * from the context.
 */
public class DistanceTraveledObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "distance_traveled");

    /**
     * Represents the different modes of travel a player can use.
     */
    enum TravelMode {
        FOOT,
        HORSE,
        BOAT,
        MINECART,
        ELYTRA,
        PIG
    }

    private final Set<TravelMode> validModes;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public DistanceTraveledObjectiveType() {
        this.validModes = Set.of();
    }

    private DistanceTraveledObjectiveType(@NotNull Set<TravelMode> validModes) {
        this.validModes = validModes;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public DistanceTraveledObjectiveType parseConfig(@NotNull Section section) {
        Set<TravelMode> modes = Set.of();
        if (section.contains("modes")) {
            modes = section.getStringList("modes").stream()
                    .map(s -> TravelMode.valueOf(s.toUpperCase()))
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(TravelMode.class)));
            modes = Set.copyOf(modes);
        }
        return new DistanceTraveledObjectiveType(modes);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof DistanceTraveledQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof DistanceTraveledQuestContext travelContext)) {
            return 0;
        }

        if (validModes.isEmpty()) {
            return travelContext.getBlockDistance();
        }

        TravelMode detectedMode = detectTravelMode(travelContext.getMoveEvent().getPlayer());
        return validModes.contains(detectedMode) ? travelContext.getBlockDistance() : 0;
    }

    /**
     * Detects the current travel mode of the given player based on their vehicle and
     * movement state.
     *
     * @param player the player to detect the travel mode for
     * @return the detected {@link TravelMode}
     */
    @NotNull
    private TravelMode detectTravelMode(@NotNull Player player) {
        if (player.isGliding()) {
            return TravelMode.ELYTRA;
        }
        if (player.getVehicle() instanceof Horse) {
            return TravelMode.HORSE;
        }
        if (player.getVehicle() instanceof Boat) {
            return TravelMode.BOAT;
        }
        if (player.getVehicle() instanceof Minecart) {
            return TravelMode.MINECART;
        }
        if (player.getVehicle() instanceof Pig) {
            return TravelMode.PIG;
        }
        return TravelMode.FOOT;
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
        if (validModes.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_DISTANCE_TRAVELED_ANY,
                    Map.of("count", count));
        }
        if (validModes.size() == 1) {
            String modeName = validModes.iterator().next().name().toLowerCase().replace('_', ' ');
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_DISTANCE_TRAVELED_SINGLE,
                    Map.of("count", count, "mode", modeName));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_DISTANCE_TRAVELED_MULTI_HEADER, Map.of("count", count)));
        for (TravelMode mode : validModes) {
            String modeName = mode.name().toLowerCase().replace('_', ' ');
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_DISTANCE_TRAVELED_MULTI_ITEM, Map.of("mode", modeName)));
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
