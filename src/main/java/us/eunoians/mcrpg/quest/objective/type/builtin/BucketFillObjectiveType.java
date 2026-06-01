package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Built-in objective type for tracking bucket fill progress.
 * <p>
 * Config entries under {@code buckets} are vanilla material names for the resulting filled
 * bucket item (e.g. {@code WATER_BUCKET}, {@code LAVA_BUCKET}). If the list is empty, any
 * bucket fill counts toward progress.
 */
public class BucketFillObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "bucket_fill");

    private final Set<Material> validBuckets;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public BucketFillObjectiveType() {
        this.validBuckets = Set.of();
    }

    private BucketFillObjectiveType(@NotNull Set<Material> validBuckets) {
        this.validBuckets = validBuckets;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public BucketFillObjectiveType parseConfig(@NotNull Section section) {
        Set<Material> buckets = Set.of();
        if (section.contains("buckets")) {
            buckets = section.getStringList("buckets").stream()
                    .map(Material::matchMaterial)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new BucketFillObjectiveType(buckets);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof BucketFillQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof BucketFillQuestContext fillContext)) {
            return 0;
        }

        if (validBuckets.isEmpty()) {
            return 1;
        }

        return validBuckets.contains(fillContext.getBucketFillEvent().getItemStack().getType()) ? 1 : 0;
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
        if (validBuckets.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_BUCKET_FILL_ANY,
                    Map.of("count", count));
        }
        if (validBuckets.size() == 1) {
            String bucketName = validBuckets.iterator().next().name().toLowerCase().replace('_', ' ');
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_BUCKET_FILL_SINGLE,
                    Map.of("count", count, "bucket", bucketName));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_BUCKET_FILL_MULTI_HEADER, Map.of("count", count)));
        for (Material bucket : validBuckets) {
            String bucketName = bucket.name().toLowerCase().replace('_', ' ');
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_BUCKET_FILL_MULTI_ITEM, Map.of("bucket", bucketName)));
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
