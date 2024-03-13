package us.eunoians.mcrpg.ability.attribute;

import org.jetbrains.annotations.NotNull;

/**
 * Any attribute that extends this attribute will be displayed in the ability gui using the format of
 * name: content
 * <p>
 * Ex)
 * Tier: 4
 */
public interface DisplayableAttribute {


    /**
     * Gets the display name for this attribute to use when displaying information to the player
     *
     * @return The display name for this attribute to use when displaying information to the player
     */
    @NotNull
    String getDisplayName();
}
