package us.eunoians.mcrpg.gui.experiencebank.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.experiencebank.ExperienceBankGui;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.RedeemableLevelsGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Set;

/**
 * This slot is used by the {@link ExperienceBankGui} in order to open a
 * {@link us.eunoians.mcrpg.gui.experiencebank.redeemable.RedeemableLevelsGui}.
 */
public final class RedeemableLevelsSlot implements McRPGSlot {

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.EXPERIENCE_BANK_GUI_REDEEMABLE_LEVELS_SLOT_DISPLAY_ITEM))
                .addPlaceholders(Map.of("redeemable-levels", Integer.toString(mcRPGPlayer.getExperienceExtras().getRedeemableLevels())));
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        RedeemableLevelsGui redeemableLevelsGui = new RedeemableLevelsGui(mcRPGPlayer);
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI)
                    .trackPlayerGui(player, redeemableLevelsGui);
            player.openInventory(redeemableLevelsGui.getInventory());
        });
        return true;
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(ExperienceBankGui.class);
    }
}
