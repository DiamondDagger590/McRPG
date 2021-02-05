package us.eunoians.mcrpg.ability.creation;

import com.google.gson.JsonObject;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.api.AbilityHolder;

/**
 * This represents an object that can be used to pass in data for the construction of {@link us.eunoians.mcrpg.ability.Ability}s.
 * <p>
 * Any implementation should implement required interfaces such as {@link ToggleableCreationData} for their specific use cases.
 *
 * @author DiamondDagger590
 */
public abstract class AbilityCreationData {

    /**
     * The {@link AbilityHolder} that has data being created for
     */
    @NotNull
    private final AbilityHolder abilityHolder;

    /**
     * The {@link JsonObject} that contains extra info
     */
    @NonFinal
    private final JsonObject jsonObject;

    public AbilityCreationData(@NotNull AbilityHolder abilityHolder, @NotNull JsonObject jsonObject) {
        this.abilityHolder = abilityHolder;
        this.jsonObject = jsonObject;
    }

    /**
     * Gets the {@link AbilityHolder} that has data being created for
     *
     * @return The {@link AbilityHolder} that has data being created for.
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }

    /**
     * Gets the {@link JsonObject} that contains info required to initialize the ability.
     * <p>
     * This object will automatically be populated with fields relevant to the ability.
     * For example, an {@link us.eunoians.mcrpg.ability.Ability} that implements {@link us.eunoians.mcrpg.ability.TierableAbility}
     * will have {@code "tier"} placed inside of this {@link org.json.simple.JSONObject} already and have it automatically pulled
     * from the database as well as automatically save.
     * <p>
     * This means a lot of these interfaces will provide automated integration for newly implemented abilities and generifies? (is this a word?)
     * the construction of the {@link AbilityCreationData}.
     *
     * @return The {@link JsonObject} that contains info required to initialize the ability
     */
    public JsonObject getJsonObject() {
        return jsonObject;
    }
}
