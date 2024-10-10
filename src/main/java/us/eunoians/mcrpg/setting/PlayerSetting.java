package us.eunoians.mcrpg.setting;

import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.gui.slot.setting.PlayerSettingSlot;

import java.util.Optional;

/**
 * A player setting represents some sort of player controlled configuration,
 * allowing players to tailor their gameplay experience.
 * <p>
 * Every {@link us.eunoians.mcrpg.expansion.content.McRPGContentPack} can provide settings
 * that will automatically be handled by McRPG.
 * <p>
 * Player settings are handled by a chain of {@link LinkedNode}s, where a player
 * can click through the options for the setting with the next node representing the next
 * setting option for the player.
 * <p>
 * It is assumed that all player settings will be enums, requiring enums to provide
 * the ability to access other values of said enum through methods like {@link #name()}
 * and {@link #fromString(String)}.
 */
public interface PlayerSetting extends McRPGContent {

    /**
     * Gets the {@link NamespacedKey} that represents this setting.
     *
     * @return The {@link NamespacedKey} that represents this setting.
     */
    @NotNull
    NamespacedKey getSettingKey();

    /**
     * Gets the first player setting in a series of {@link LinkedNode}s
     * for a player.
     *
     * @return The first player setting in a series of {@link LinkedNode}s
     * for a player.
     */
    @NotNull
    LinkedNode<? extends PlayerSetting> getFirstSetting();

    /**
     * Gets the {@link LinkedNode} containing the next player setting.
     *
     * @return The {@link LinkedNode} containing the next player setting.
     */
    @NotNull
    LinkedNode<? extends PlayerSetting> getNextSetting();

    /**
     * Gets a {@link PlayerSettingSlot} for this player setting to be used in a {@link us.eunoians.mcrpg.gui.PlayerSettingGui}
     * for the given player.
     *
     * @param player The {@link McRPGPlayer} viewing this setting.
     * @return A {@link PlayerSettingSlot} for this player setting to be used in a {@link us.eunoians.mcrpg.gui.PlayerSettingGui}
     * for the given player.
     */
    @NotNull
    PlayerSettingSlot<? extends PlayerSetting> getSettingSlot(@NotNull McRPGPlayer player);

    /**
     * Handles a {@link McRPGPlayer}'s setting being updated. The current player setting object is the "new" setting,
     * while the "old" one is passed in as a parameter.
     *
     * @param player     The {@link McRPGPlayer} that had their setting updated.
     * @param oldSetting An {@link Optional} containing the old setting if there was one.
     */
    void onSettingChange(@NotNull McRPGPlayer player, @NotNull Optional<PlayerSetting> oldSetting);

    /**
     * Gets a setting instance from the provided string.
     *
     * @param setting The string of the setting.
     * @return An {@link Optional} containing the setting matching the string,
     * or empty if no matches are found.
     */
    @NotNull
    Optional<? extends PlayerSetting> fromString(@NotNull String setting);

    /**
     * Gets the string name of this setting instance.
     *
     * @return The string name of this setting instance.
     */
    @NotNull
    String name();
}
