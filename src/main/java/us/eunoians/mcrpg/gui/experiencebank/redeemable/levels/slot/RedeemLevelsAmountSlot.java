package us.eunoians.mcrpg.gui.experiencebank.redeemable.levels.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.levels.RedeemableLevelsGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This slot allows a player to redeem a given number of their redeemable levels
 * into a given {@link Skill}.
 */
public class RedeemLevelsAmountSlot implements McRPGSlot {

    private final Skill skill;
    private final int amountToSpend;

    public RedeemLevelsAmountSlot(@NotNull Skill skill, int amountToSpend) {
        this.skill = skill;
        this.amountToSpend = amountToSpend;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.REDEEMABLE_LEVELS_GUI_REDEEM_AMOUNT_BUTTON_DISPLAY_ITEM))
                .addPlaceholders(getPlaceholders(mcRPGPlayer));
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            player.performCommand("mcrpg redeem levels " + skill.getName(mcRPGPlayer)+ " " + Math.min(amountToSpend, mcRPGPlayer.getExperienceExtras().getRedeemableLevels()));
            player.closeInventory();
        });
        return true;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(RedeemableLevelsGui.class);
    }

    @NotNull
    private Map<String, String> getPlaceholders(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("redeemable-levels", Integer.toString(mcRPGPlayer.getExperienceExtras().getRedeemableLevels()));
        placeholders.put("redeemable-levels-to-spend", Integer.toString(amountToSpend));
        placeholders.put("skill", skill.getName(mcRPGPlayer));
        return placeholders;
    }
}
