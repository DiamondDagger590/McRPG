package us.eunoians.mcrpg.gui.experiencebank.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.experiencebank.ExperienceBankGui;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.experience.RedeemableExperienceGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Set;

/**
 * This slot is used by the {@link ExperienceBankGui} in order to open a
 * {@link RedeemableExperienceGui}.
 */
public final class RedeemableExperienceSlot implements McRPGSlot {

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.EXPERIENCE_BANK_GUI_REDEEMABLE_EXPERIENCE_SLOT_DISPLAY_ITEM))
                .addPlaceholders(Map.of("redeemable-experience", Integer.toString(mcRPGPlayer.getExperienceExtras().getRedeemableExperience())));
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        RedeemableExperienceGui redeemableExperienceGui = new RedeemableExperienceGui(mcRPGPlayer);
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI)
                    .trackPlayerGui(player, redeemableExperienceGui);
            player.openInventory(redeemableExperienceGui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(ExperienceBankGui.class);
    }
}
