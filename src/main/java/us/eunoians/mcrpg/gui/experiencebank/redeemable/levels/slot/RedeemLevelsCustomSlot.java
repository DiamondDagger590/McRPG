package us.eunoians.mcrpg.gui.experiencebank.redeemable.levels.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.chat.redeemable.RedeemableLevelsChatResponse;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.levels.RedeemableLevelsGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This slot allows a player to type a custom amount of their redeemable levels
 * to be redeemed into a given {@link Skill}
 * via a {@link RedeemableLevelsChatResponse} prompt.
 */
public class RedeemLevelsCustomSlot implements McRPGSlot {

    private final Skill skill;

    public RedeemLevelsCustomSlot(@NotNull Skill skill) {
        this.skill = skill;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.REDEEMABLE_LEVELS_GUI_REDEEM_CUSTOM_BUTTON_DISPLAY_ITEM))
                .addPlaceholders(getPlaceholders(mcRPGPlayer));
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            // Close inventory
            player.closeInventory();
            // Prompt the player for a response
            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_LEVELS_GUI_REDEEM_CUSTOM_PROMPT_MESSAGE));
            // Start the response
            RedeemableLevelsChatResponse redeemableLevelsChatResponse = new RedeemableLevelsChatResponse(mcRPGPlayer, skill);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.CHAT_RESPONSE).addPendingResponse(player.getUniqueId(), redeemableLevelsChatResponse);
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
        placeholders.put("skill", skill.getName(mcRPGPlayer));
        return placeholders;
    }
}
