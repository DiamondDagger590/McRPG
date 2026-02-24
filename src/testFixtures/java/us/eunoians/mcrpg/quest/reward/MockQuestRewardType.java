package us.eunoians.mcrpg.quest.reward;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A mock implementation of {@link QuestRewardType} that records every player
 * it grants a reward to. Useful for verifying reward granting in tests.
 */
public class MockQuestRewardType implements QuestRewardType {

    private final NamespacedKey key;
    private final NamespacedKey expansionKey;
    private final List<Player> grantedTo = new ArrayList<>();

    public MockQuestRewardType(@NotNull NamespacedKey key, @NotNull NamespacedKey expansionKey) {
        this.key = key;
        this.expansionKey = expansionKey;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @NotNull
    @Override
    public QuestRewardType parseConfig(@NotNull Section section) {
        return this;
    }

    @Override
    public void grant(@NotNull Player player) {
        grantedTo.add(player);
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        return Map.of("type", key.toString());
    }

    @NotNull
    @Override
    public QuestRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        return new MockQuestRewardType(key, expansionKey);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(expansionKey);
    }

    /**
     * Gets the list of players that have been granted this reward.
     *
     * @return an unmodifiable list of players
     */
    @NotNull
    public List<Player> getGrantedTo() {
        return Collections.unmodifiableList(grantedTo);
    }

    /**
     * Gets the number of times this reward has been granted.
     *
     * @return the grant count
     */
    public int getGrantCount() {
        return grantedTo.size();
    }

    /**
     * Resets the grant tracking.
     */
    public void resetGrants() {
        grantedTo.clear();
    }
}
