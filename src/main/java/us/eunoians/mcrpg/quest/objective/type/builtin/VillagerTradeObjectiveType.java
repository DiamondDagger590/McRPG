package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
 * Built-in objective type for tracking villager trade progress.
 * <p>
 * Supports both vanilla materials and custom items from plugins integrated via McCore's
 * {@link CustomItemWrapper}. Config entries under {@code items} filter by the trade result
 * item. If the list is empty, any villager trade counts toward progress. The progress delta
 * equals the result stack amount of the trade.
 */
public class VillagerTradeObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "villager_trade");

    private final Set<CustomItemWrapper> validItems;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public VillagerTradeObjectiveType() {
        this.validItems = Set.of();
    }

    private VillagerTradeObjectiveType(@NotNull Set<CustomItemWrapper> validItems) {
        this.validItems = validItems;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public VillagerTradeObjectiveType parseConfig(@NotNull Section section) {
        Set<CustomItemWrapper> items = Set.of();
        if (section.contains("items")) {
            items = section.getStringList("items").stream()
                    .map(CustomItemWrapper::new)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new VillagerTradeObjectiveType(items);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof VillagerTradeQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof VillagerTradeQuestContext tradeContext)) {
            return 0;
        }

        int delta = tradeContext.getTradeEvent().getTrade().getResult().getAmount();

        if (validItems.isEmpty()) {
            return delta;
        }

        CustomItemWrapper resultItem = new CustomItemWrapper(tradeContext.getTradeEvent().getTrade().getResult());
        return validItems.contains(resultItem) ? delta : 0;
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
        if (validItems.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_VILLAGER_TRADE_ANY,
                    Map.of("count", count));
        }
        if (validItems.size() == 1) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_VILLAGER_TRADE_SINGLE,
                    Map.of("count", count, "item", resolveItemDisplayName(validItems.iterator().next())));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_VILLAGER_TRADE_MULTI_HEADER, Map.of("count", count)));
        for (CustomItemWrapper item : validItems) {
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_VILLAGER_TRADE_MULTI_ITEM, Map.of("item", resolveItemDisplayName(item))));
        }
        return sb.toString();
    }

    /**
     * Resolves a human-readable display name from a {@link CustomItemWrapper}.
     *
     * @param wrapper the item wrapper to resolve
     * @return the custom item identifier or a formatted material name
     */
    @NotNull
    private String resolveItemDisplayName(@NotNull CustomItemWrapper wrapper) {
        return wrapper.customItem()
                .orElseGet(() -> wrapper.material()
                        .map(Material::name)
                        .map(name -> name.toLowerCase().replace('_', ' '))
                        .orElse("unknown"));
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
