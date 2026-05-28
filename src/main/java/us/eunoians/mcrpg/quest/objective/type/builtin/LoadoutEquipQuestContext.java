package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.loadout.LoadoutAbilityChangeEvent;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Progress context wrapping a {@link LoadoutAbilityChangeEvent}. Carries the key of the
 * ability that was equipped (the "new" ability in the change) and the UUID of the player
 * whose loadout changed.
 * <p>
 * Only constructed for {@link LoadoutAbilityChangeEvent.ChangeReason#EQUIP} and
 * {@link LoadoutAbilityChangeEvent.ChangeReason#SWAP} events — never for unequips.
 */
public class LoadoutEquipQuestContext extends QuestObjectiveProgressContext {

    private final LoadoutAbilityChangeEvent changeEvent;

    /**
     * Creates a context from the given loadout ability change event.
     *
     * @param changeEvent the event that triggered this context
     */
    public LoadoutEquipQuestContext(@NotNull LoadoutAbilityChangeEvent changeEvent) {
        this.changeEvent = changeEvent;
    }

    /**
     * Gets the key of the ability that was equipped to the loadout.
     * For {@link LoadoutAbilityChangeEvent.ChangeReason#SWAP}, this is the new ability
     * that replaced the old one.
     *
     * @return the equipped ability's namespaced key
     */
    @NotNull
    public NamespacedKey getAbilityKey() {
        return changeEvent.getNewAbility().orElseThrow(() ->
                new NoSuchElementException("getAbilityKey() called on a LoadoutEquipQuestContext with no new ability"
                        + " — player UUID: " + changeEvent.getPlayerUUID()
                        + ", loadout slot: " + changeEvent.getLoadoutSlot()));
    }

    /**
     * Gets the UUID of the player whose loadout was changed.
     *
     * @return the player's UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return changeEvent.getPlayerUUID();
    }
}
