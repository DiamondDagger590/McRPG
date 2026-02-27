package us.eunoians.mcrpg.quest.board.distribution;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Stateless utility that computes a live preview of which distribution tiers a player
 * currently qualifies for and what rewards they would receive at their current contribution
 * level. All text is resolved through {@link McRPGLocalizationManager}.
 */
public final class DistributionPreviewResolver {

    private DistributionPreviewResolver() {}

    /**
     * Builds a preview of distribution tiers for the given player based on
     * their current contribution to the quest.
     *
     * @param player         the viewing player (for locale resolution)
     * @param config         the reward distribution configuration
     * @param playerUUID     the player UUID to preview for
     * @param contributions  per-player contribution amounts
     * @param rarityKey      the quest's rarity key (nullable for non-board quests)
     * @param rarityRegistry rarity registry for gate comparisons
     * @param typeRegistry   distribution type registry
     * @param groupMembers   current group members for MEMBERSHIP evaluation
     * @return ordered list of preview entries, one per configured tier
     */
    @NotNull
    public static List<DistributionPreviewEntry> buildPreview(
            @NotNull McRPGPlayer player,
            @NotNull RewardDistributionConfig config,
            @NotNull UUID playerUUID,
            @NotNull Map<UUID, Long> contributions,
            @Nullable NamespacedKey rarityKey,
            @NotNull QuestRarityRegistry rarityRegistry,
            @NotNull RewardDistributionTypeRegistry typeRegistry,
            @NotNull Set<UUID> groupMembers) {

        McRPGLocalizationManager localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        long totalProgress = contributions.values().stream().mapToLong(Long::longValue).sum();
        long playerContribution = contributions.getOrDefault(playerUUID, 0L);
        double playerPercent = totalProgress > 0 ? (double) playerContribution / totalProgress * 100 : 0;
        ContributionSnapshot snapshot = new ContributionSnapshot(contributions, totalProgress, groupMembers, null);

        List<DistributionPreviewEntry> entries = new ArrayList<>();
        for (DistributionTierConfig tier : config.getTiers()) {
            boolean passesRarity = tier.passesRarityGate(rarityKey, rarityRegistry);
            boolean qualifies = false;
            if (passesRarity) {
                Optional<RewardDistributionType> type = typeRegistry.get(tier.getTypeKey());
                if (type.isPresent()) {
                    qualifies = type.get().resolve(snapshot, tier).contains(playerUUID);
                }
            }

            entries.add(new DistributionPreviewEntry(
                    tier.getTierKey(),
                    qualifies,
                    playerContribution,
                    playerPercent,
                    qualifies ? buildRewardPreviewComponents(player, tier, localization) : List.of()
            ));
        }
        return entries;
    }

    @NotNull
    private static List<Component> buildRewardPreviewComponents(
            @NotNull McRPGPlayer player,
            @NotNull DistributionTierConfig tier,
            @NotNull McRPGLocalizationManager localization) {

        return tier.getRewardEntries().stream()
                .<Component>map(entry -> {
                    String rewardType = entry.reward().getKey().value();
                    return entry.reward().getNumericAmount()
                            .stream()
                            .mapToObj(amount -> localization.getLocalizedMessageAsComponent(player,
                                    LocalizationKey.QUEST_BOARD_REWARD_LINE,
                                    Map.of("type", rewardType, "amount", String.valueOf(amount))))
                            .findFirst()
                            .orElseGet(() -> localization.getLocalizedMessageAsComponent(player,
                                    LocalizationKey.QUEST_BOARD_REWARD_LINE_NO_AMOUNT,
                                    Map.of("type", rewardType)));
                })
                .toList();
    }
}
