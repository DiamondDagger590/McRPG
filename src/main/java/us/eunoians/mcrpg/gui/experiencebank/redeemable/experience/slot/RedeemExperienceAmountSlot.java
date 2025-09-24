package us.eunoians.mcrpg.gui.experiencebank.redeemable.experience.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.experience.RedeemableExperienceGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This slot allows a player to redeem a given amount of their redeemable experience
 * into a given {@link Skill}.
 */
public class RedeemExperienceAmountSlot implements McRPGSlot {

    private final Skill skill;
    private final int amountToSpend;

    public RedeemExperienceAmountSlot(@NotNull Skill skill, int amountToSpend) {
        this.skill = skill;
        this.amountToSpend = amountToSpend;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.REDEEMABLE_EXPERIENCE_GUI_REDEEM_AMOUNT_BUTTON_DISPLAY_ITEM))
                .addPlaceholders(getPlaceholders(mcRPGPlayer));
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            player.performCommand("mcrpg redeem experience " + skill.getName(mcRPGPlayer)+ " " + Math.min(amountToSpend, mcRPGPlayer.getExperienceExtras().getRedeemableExperience()));
            player.closeInventory();
        });
        return true;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(RedeemableExperienceGui.class);
    }

    @NotNull
    private Map<String, String> getPlaceholders(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("redeemable-experience", Integer.toString(mcRPGPlayer.getExperienceExtras().getRedeemableExperience()));
        placeholders.put("redeemable-experience-to-spend", Integer.toString(amountToSpend));
        placeholders.put("skill", skill.getName(mcRPGPlayer));
        return placeholders;
    }
}
