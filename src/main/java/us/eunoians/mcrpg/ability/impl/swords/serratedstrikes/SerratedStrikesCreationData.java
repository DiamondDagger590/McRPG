package us.eunoians.mcrpg.ability.impl.swords.serratedstrikes;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.ability.creation.TierableCreationData;
import us.eunoians.mcrpg.ability.creation.ToggleableCreationData;
import us.eunoians.mcrpg.ability.creation.UnlockableCreationData;
import us.eunoians.mcrpg.api.AbilityHolder;

/**
 * Contains data needed to create {@link SerratedStrikes}.
 *
 * @author DiamondDagger590
 */
public class SerratedStrikesCreationData extends AbilityCreationData implements TierableCreationData, UnlockableCreationData, ToggleableCreationData {

    private final int tier;
    private final boolean unlocked;
    private final boolean toggled;

    public SerratedStrikesCreationData(@NotNull AbilityHolder abilityHolder, @NotNull JsonObject jsonObject) {
        super(abilityHolder, jsonObject);
        this.tier = jsonObject.has("tier") ? jsonObject.get("tier").getAsInt() : 0;
        this.toggled = jsonObject.has("toggled") && jsonObject.get("toggled").getAsBoolean();
        this.unlocked = jsonObject.has("unlocked") && jsonObject.get("unlocked").getAsBoolean();
    }

    /**
     * Gets the tier of the {@link Ability} being created
     *
     * @return The tier of the {@link Ability} being created
     */
    @Override
    public int getTier() {
        return this.tier;
    }

    /**
     * Gets if the {@link Ability} represented by the {@link AbilityCreationData}
     * is toggled
     *
     * @return {@code true} if the {@link Ability} represented by the {@link AbilityCreationData}
     * is toggled
     */
    @Override
    public boolean isToggled() {
        return this.toggled;
    }

    /**
     * Gets if the {@link Ability} being created
     * is unlocked.
     *
     * @return {@code true} if the {@link Ability} being created
     * is unlocked.
     */
    @Override
    public boolean isUnlocked() {
        return this.unlocked;
    }
}
