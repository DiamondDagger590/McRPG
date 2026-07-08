package us.eunoians.mcrpg.gui.tutorial.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.gui.tutorial.DisableTutorialConfirmGui;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.quest.source.builtin.TutorialQuestSource;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.DisableTutorialSetting;

import java.util.Set;

/**
 * Confirm button in {@link DisableTutorialConfirmGui}.
 * On click:
 * <ol>
 *   <li>Sets the tutorial setting to {@link DisableTutorialSetting#DISABLED}</li>
 *   <li>Abandons the tutorial chain via {@link QuestChainManager#abandonChain}</li>
 *   <li>Closes the player's inventory</li>
 * </ol>
 */
public class DisableTutorialConfirmSlot implements McRPGSlot {

    /**
     * Handles the click: disables tutorials, abandons the chain, and closes the GUI.
     *
     * @param mcRPGPlayer the player who clicked
     * @param clickType   the type of click
     * @return {@code true} always
     */
    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.setPlayerSetting(DisableTutorialSetting.DISABLED);

        QuestChainManager chainManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_CHAIN);
        chainManager.abandonChain(mcRPGPlayer.getUUID(), TutorialQuestSource.TUTORIAL_CHAIN_KEY);

        mcRPGPlayer.getAsBukkitPlayer().ifPresent(Player::closeInventory);
        return true;
    }

    /**
     * Builds the display item for the confirm button.
     *
     * @param mcRPGPlayer the player to build the item for
     * @return the confirm button item
     */
    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager locManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        return ItemBuilder.from(locManager.getLocalizedSection(
                        mcRPGPlayer, LocalizationKey.DISABLE_TUTORIAL_CONFIRM_SLOT_DISPLAY_ITEM))
                .applyTagReplacements(locManager.getPaletteReplacements());
    }

    /**
     * Returns the GUI types this slot is valid in.
     *
     * @return singleton set containing {@link DisableTutorialConfirmGui}
     */
    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(DisableTutorialConfirmGui.class);
    }
}
