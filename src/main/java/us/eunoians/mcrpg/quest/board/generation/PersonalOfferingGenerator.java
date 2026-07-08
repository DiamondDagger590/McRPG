package us.eunoians.mcrpg.quest.board.generation;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.BoardRotation;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategory;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateEngine;
import us.eunoians.mcrpg.quest.board.template.condition.QuestCompletionHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Stateless collaborator for generating per-player personal offerings. Deterministic seeding
 * ensures the same player sees the same offerings within a rotation period, enabling
 * lazy generation on first board open without persisting generation state.
 */
public final class PersonalOfferingGenerator {

    private final SlotGenerationLogic slotGenerationLogic;

    /**
     * Creates a new personal offering generator with the given slot generation logic.
     *
     * @param slotGenerationLogic the slot generation logic collaborator
     */
    public PersonalOfferingGenerator(@NotNull SlotGenerationLogic slotGenerationLogic) {
        this.slotGenerationLogic = slotGenerationLogic;
    }

    /**
     * Generates personal offerings for a player for a given rotation. Uses configurable
     * source weights to choose between hand-crafted definitions and template-generated quests.
     * Delegates to the overload with no player context (shared-style condition evaluation).
     */
    @NotNull
    public List<BoardOffering> generatePersonalOfferings(
            @NotNull UUID playerUUID,
            @NotNull BoardRotation rotation,
            @NotNull List<BoardSlotCategory> categories,
            int minOfferings,
            @NotNull QuestPool questPool,
            @NotNull QuestRarityRegistry rarityRegistry,
            @NotNull QuestTemplateEngine templateEngine,
            int hcWeight,
            int templateWeight) {
        return generatePersonalOfferings(playerUUID, rotation, categories, minOfferings,
                questPool, rarityRegistry, templateEngine, hcWeight, templateWeight, null);
    }

    /**
     * Generates personal offerings for a player for a given rotation. Uses configurable
     * source weights to choose between hand-crafted definitions and template-generated quests.
     * When {@code completionHistory} is provided, player-dependent conditions (permissions,
     * completion prerequisites) are evaluated against the player during template generation.
     *
     * @param playerUUID        the player's UUID
     * @param rotation          the current rotation
     * @param categories        PERSONAL categories filtered by refresh type and permission
     * @param minOfferings      the effective minimum total personal offerings for this player
     * @param questPool         the quest pool (hand-crafted + templates)
     * @param rarityRegistry    the rarity registry for rolling rarities
     * @param templateEngine    the template engine for generating from templates
     * @param hcWeight          the configured weight for hand-crafted quest selection
     * @param templateWeight    the configured weight for template quest selection
     * @param completionHistory the player's quest completion history for prerequisite evaluation, or {@code null}
     * @return the list of generated personal offerings
     */
    @NotNull
    public List<BoardOffering> generatePersonalOfferings(
            @NotNull UUID playerUUID,
            @NotNull BoardRotation rotation,
            @NotNull List<BoardSlotCategory> categories,
            int minOfferings,
            @NotNull QuestPool questPool,
            @NotNull QuestRarityRegistry rarityRegistry,
            @NotNull QuestTemplateEngine templateEngine,
            int hcWeight,
            int templateWeight,
            @Nullable QuestCompletionHistory completionHistory) {

        List<BoardOffering> offerings = new ArrayList<>();
        int slotIndex = 0;

        for (BoardSlotCategory category : categories) {
            int count = slotGenerationLogic.computeSlotCountForCategory(
                    category, new Random(computeSeed(playerUUID, rotation.getRotationEpoch(), slotIndex)));

            for (int i = 0; i < count; i++) {
                long seed = computeSeed(playerUUID, rotation.getRotationEpoch(), slotIndex);
                Random slotRandom = new Random(seed);

                QuestRarity rarity = rarityRegistry.rollRarity(slotRandom);

                Optional<SlotSelection> selection = questPool.selectForSlot(
                        rarity.getKey(), slotRandom, templateEngine, hcWeight, templateWeight,
                        playerUUID, completionHistory, category.getScopeProviderKey());

                final int currentSlot = slotIndex;
                Optional<BoardOffering> offering = selection.map(
                        sel -> sel.toOffering(rotation, category, currentSlot, playerUUID.toString()));

                if (offering.isPresent() && isDuplicateTemplateOffering(offering.get(), offerings)) {
                    offering = Optional.empty();
                }

                offering.ifPresent(offerings::add);
                slotIndex++;
            }
        }

        return offerings;
    }

    /**
     * Checks whether a candidate offering duplicates an existing template offering
     * in the list. Two template offerings are considered duplicates if they share
     * the same template key AND the same quest definition key (which encodes the
     * resolved pool selection via the deterministic seed hex suffix).
     * Hand-crafted offerings are never considered duplicates of each other.
     *
     * @param candidate the candidate offering to check
     * @param existing  the list of already-generated offerings
     * @return {@code true} if the candidate is a duplicate
     */
    public boolean isDuplicateTemplateOffering(@NotNull BoardOffering candidate,
                                               @NotNull List<BoardOffering> existing) {
        if (!candidate.isTemplateGenerated()) return false;
        NamespacedKey candidateTemplate = candidate.getTemplateKey().orElse(null);
        NamespacedKey candidateDefKey = candidate.getQuestDefinitionKey();
        return existing.stream()
                .filter(BoardOffering::isTemplateGenerated)
                .anyMatch(o -> Objects.equals(o.getTemplateKey().orElse(null), candidateTemplate)
                        && Objects.equals(o.getQuestDefinitionKey(), candidateDefKey));
    }

    /**
     * Computes a deterministic seed for per-player offering generation.
     * Same inputs always produce the same seed, ensuring a player sees the
     * same offerings if they reopen the board within the same rotation.
     *
     * @param playerUUID    the player's UUID
     * @param rotationEpoch the rotation epoch timestamp
     * @param slotIndex     the positional slot index
     * @return the computed seed
     */
    public static long computeSeed(@NotNull UUID playerUUID, long rotationEpoch, int slotIndex) {
        long msb = playerUUID.getMostSignificantBits();
        long lsb = playerUUID.getLeastSignificantBits();
        long hash = msb * 6364136223846793005L + lsb;
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= rotationEpoch * 0xc4ceb9fe1a85ec53L;
        hash += slotIndex * 0x9e3779b97f4a7c15L;
        hash ^= hash >>> 33;
        return hash;
    }
}
