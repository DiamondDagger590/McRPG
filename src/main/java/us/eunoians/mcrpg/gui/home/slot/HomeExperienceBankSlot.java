package us.eunoians.mcrpg.gui.home.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;
import us.eunoians.mcrpg.gui.experiencebank.ExperienceBankGui;
import us.eunoians.mcrpg.gui.home.HomeGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This slot is used in the {@link HomeGui} to open
 * a new {@link ExperienceBankGui} when clicked.
 */
public class HomeExperienceBankSlot implements McRPGSlot {

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        ExperienceBankGui experienceBankGui = new ExperienceBankGui(mcRPGPlayer);
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(player, experienceBankGui);
            player.openInventory(experienceBankGui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedSection(mcRPGPlayer, LocalizationKey.HOME_GUI_EXPERIENCE_BANK_SLOT_DISPLAY_ITEM))
                .addPlaceholders(getPlaceholders(mcRPGPlayer));
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(HomeGui.class);
    }

    @NotNull
    private Map<String, String> getPlaceholders(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();
        PlayerExperienceExtras experienceExtras = mcRPGPlayer.getExperienceExtras();
        placeholders.put("redeemable-experience", Integer.toString(experienceExtras.getRedeemableExperience()));
        placeholders.put("redeemable-levels", Integer.toString(experienceExtras.getRedeemableLevels()));
        placeholders.put("rested-experience", Float.toString(experienceExtras.getRestedExperience()));
        placeholders.put("boosted-experience",  Integer.toString(experienceExtras.getBoostedExperience()));
        return placeholders;
    }
}
