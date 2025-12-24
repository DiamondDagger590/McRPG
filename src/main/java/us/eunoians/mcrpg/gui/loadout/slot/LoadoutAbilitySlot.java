package us.eunoians.mcrpg.gui.loadout.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutAbilitySelectGui;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * This slot is used to represent a spot in a player's {@link Loadout}.
 * <p>
 * The slot can be empty or represent an {@link Ability} in the loadout.
 * When clicked, it will open a {@link LoadoutAbilitySelectGui} for a player
 * to select a new ability to go into this slot.
 */
public class LoadoutAbilitySlot implements McRPGSlot {

    private final Loadout loadout;
    @Nullable
    private final Ability ability;

    public LoadoutAbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        this.loadout = loadout;
        this.ability = null;
    }

    public LoadoutAbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout, @NotNull Ability ability) {
        this.loadout = loadout;
        this.ability = ability;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            LoadoutAbilitySelectGui loadoutAbilitySelectGui;
            if (ability != null) {
                loadoutAbilitySelectGui = new LoadoutAbilitySelectGui(mcRPGPlayer, loadout, ability.getAbilityKey());
            } else {
                loadoutAbilitySelectGui = new LoadoutAbilitySelectGui(mcRPGPlayer, loadout);
            }
            mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutAbilitySelectGui);
            player.openInventory(loadoutAbilitySelectGui.getInventory());
        });

        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        ItemBuilder itemBuilder;
        if (ability != null) {
            itemBuilder = ability.getDisplayItemBuilder(mcRPGPlayer).addDisplayLore(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedMessages(mcRPGPlayer, LocalizationKey.LOADOUT_GUI_ABILITY_SLOT_ADDITIONAL_LORE));
        } else {
            itemBuilder = ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedSection(mcRPGPlayer, LocalizationKey.LOADOUT_GUI_FREE_ABILITY_SLOT_DISPLAY_ITEM));
        }
       return itemBuilder;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(LoadoutGui.class);
    }
}
