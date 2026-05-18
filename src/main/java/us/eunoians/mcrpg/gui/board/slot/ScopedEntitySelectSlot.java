package us.eunoians.mcrpg.gui.board.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.BoardGuiMode;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.gui.board.ScopedEntitySelectorGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Slot in the {@link ScopedEntitySelectorGui} representing a single scope entity
 * (e.g., a Land). Clicking opens the {@link QuestBoardGui} in
 * {@link BoardGuiMode#SCOPED} mode.
 */
public class ScopedEntitySelectSlot implements McRPGSlot {

    private final NamespacedKey scopeProviderKey;
    private final String entityId;
    private final String displayName;

    public ScopedEntitySelectSlot(@NotNull NamespacedKey scopeProviderKey,
                                  @NotNull String entityId,
                                  @NotNull String displayName) {
        this.scopeProviderKey = scopeProviderKey;
        this.entityId = entityId;
        this.displayName = displayName;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            QuestBoardGui gui = new QuestBoardGui(mcRPGPlayer, BoardGuiMode.SCOPED);
            RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI)
                    .trackPlayerGui(player, gui);
            player.openInventory(gui.getInventory());
        });
        return true;
    }

    /**
     * Builds the display item from the locale section, resolving palette tags and
     * substituting {@code <scope_name>} with this slot's entity display name.
     *
     * @param mcRPGPlayer the player viewing the slot
     * @return the constructed {@link ItemBuilder}
     */
    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        ItemBuilder itemBuilder = ItemBuilder.from(
                localizationManager.getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_BOARD_GROUP_ENTITY_SELECT_SLOT_DISPLAY_ITEM));
        Map<String, String> replacements = new HashMap<>(localizationManager.getPaletteReplacements());
        replacements.put("<scope_name>", displayName);
        itemBuilder.applyTagReplacements(replacements);
        return itemBuilder;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(ScopedEntitySelectorGui.class);
    }
}
