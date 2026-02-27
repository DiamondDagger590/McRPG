package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionMetadata;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Board-specific metadata attached to a {@link us.eunoians.mcrpg.quest.definition.QuestDefinition}.
 * <p>
 * Controls whether a quest is eligible for the quest board, which rarity tiers it can appear
 * under, which refresh types it supports, and optional acceptance cooldown behavior.
 * <p>
 * For hand-crafted quests in Phase 1, rarity affects <b>appearance frequency only</b> — which
 * slots the quest can appear in. Difficulty/reward multipliers are cosmetic; mechanical scaling
 * only applies to template-generated quests (Phase 2+).
 *
 * @param boardEligible         whether this quest can appear on the board
 * @param supportedRarities     the set of rarity keys this quest is eligible for
 * @param supportedRefreshTypes the set of refresh type strings (e.g. "DAILY", "WEEKLY") this quest is eligible for;
 *                              empty means all refresh types are accepted
 * @param acceptanceCooldown    optional cooldown after accepting this quest before it can appear again
 * @param cooldownScope         the scope of the cooldown ({@code "GLOBAL"}, {@code "PLAYER"}, or {@code "SCOPE_ENTITY"}),
 *                              or {@code null} if no cooldown
 */
public record BoardMetadata(
        boolean boardEligible,
        @NotNull Set<NamespacedKey> supportedRarities,
        @NotNull Set<String> supportedRefreshTypes,
        @Nullable Duration acceptanceCooldown,
        @Nullable String cooldownScope
) implements QuestDefinitionMetadata {

    public static final NamespacedKey METADATA_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "board");

    @Override
    @NotNull
    public NamespacedKey getMetadataKey() {
        return METADATA_KEY;
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("board-eligible", boardEligible);
        map.put("supported-rarities", supportedRarities.stream()
                .map(NamespacedKey::toString)
                .toList());
        if (!supportedRefreshTypes.isEmpty()) {
            map.put("supported-refresh-types", List.copyOf(supportedRefreshTypes));
        }
        if (acceptanceCooldown != null) {
            map.put("acceptance-cooldown-ms", acceptanceCooldown.toMillis());
        }
        if (cooldownScope != null) {
            map.put("cooldown-scope", cooldownScope);
        }
        return map;
    }

    /**
     * Deserializes a {@link BoardMetadata} from a previously serialized map.
     *
     * @param data the serialized map
     * @return the deserialized metadata
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static BoardMetadata deserialize(@NotNull Map<String, Object> data) {
        boolean eligible = (boolean) data.getOrDefault("board-eligible", true);

        Set<NamespacedKey> rarities = new LinkedHashSet<>();
        Object raritiesObj = data.get("supported-rarities");
        if (raritiesObj instanceof List<?> list) {
            for (Object item : list) {
                rarities.add(NamespacedKey.fromString(item.toString()));
            }
        }

        Set<String> refreshTypes = new LinkedHashSet<>();
        Object refreshObj = data.get("supported-refresh-types");
        if (refreshObj instanceof List<?> rtList) {
            for (Object item : rtList) {
                refreshTypes.add(item.toString().toUpperCase());
            }
        }

        Duration cooldown = null;
        Object cooldownMs = data.get("acceptance-cooldown-ms");
        if (cooldownMs instanceof Number number) {
            cooldown = Duration.ofMillis(number.longValue());
        }

        String scope = data.get("cooldown-scope") instanceof String s ? s : null;

        return new BoardMetadata(eligible, Set.copyOf(rarities), Set.copyOf(refreshTypes), cooldown, scope);
    }
}
