package us.eunoians.mcrpg.gui.loadout.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutAbilitySelectGui;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * This slot is used to select an {@link Ability} to go into a player's {@link Loadout}.
 */
public class LoadoutSelectAbilitySlot implements McRPGSlot {

    private final McRPGPlayer mcRPGPlayer;
    private final Loadout loadout;
    private final Ability ability;
    @Nullable
    private final NamespacedKey oldAbilityKey;

    public LoadoutSelectAbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout, @NotNull Ability ability) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.loadout = loadout;
        this.ability = ability;
        this.oldAbilityKey = null;
    }

    public LoadoutSelectAbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout, @NotNull Ability ability, @NotNull NamespacedKey oldAbilityKey) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.loadout = loadout;
        this.ability = ability;
        this.oldAbilityKey = oldAbilityKey;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer corePlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            if (oldAbilityKey != null) {
                loadout.replaceAbility(oldAbilityKey, ability.getAbilityKey());
            } else if (loadout.getRemainingLoadoutSize() > 0) {
                loadout.addAbility(ability.getAbilityKey());
            }
            LoadoutGui loadoutGui = new LoadoutGui(mcRPGPlayer, loadout);
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutGui);
            player.openInventory(loadoutGui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        AbilityItemBuilder abilityItemBuilder = ability.getDisplayItemBuilder(mcRPGPlayer);
        abilityItemBuilder.addDisplayLore(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessages(mcRPGPlayer, LocalizationKey.LOADOUT_ABILITY_SELECT_ABILITY_SELECT_LORE_TO_APPEND));
        return abilityItemBuilder;
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(LoadoutAbilitySelectGui.class);
    }
}
