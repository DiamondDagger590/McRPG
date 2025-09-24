package us.eunoians.mcrpg.gui.experiencebank.redeemable.experience.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.chat.redeemable.RedeemableExperienceChatResponse;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.experience.RedeemableExperienceGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This slot allows a player to type a custom amount of their redeemable experience
 * to be redeemed into a given {@link Skill}
 * via a {@link RedeemableExperienceChatResponse} prompt.
 */
public class RedeemExperienceCustomSlot implements McRPGSlot {

    private final Skill skill;

    public RedeemExperienceCustomSlot(@NotNull Skill skill) {
        this.skill = skill;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.REDEEMABLE_EXPERIENCE_GUI_REDEEM_CUSTOM_BUTTON_DISPLAY_ITEM))
                .addPlaceholders(getPlaceholders(mcRPGPlayer));
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            // Close inventory
            player.closeInventory();
            // Prompt the player for a response
            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_EXPERIENCE_GUI_REDEEM_CUSTOM_PROMPT_MESSAGE, getPlaceholders(mcRPGPlayer)));
            // Start the response
            RedeemableExperienceChatResponse redeemableExperienceChatResponse = new RedeemableExperienceChatResponse(mcRPGPlayer, skill);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.CHAT_RESPONSE).addPendingResponse(player.getUniqueId(), redeemableExperienceChatResponse);
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
        placeholders.put("skill", skill.getName(mcRPGPlayer));
        return placeholders;
    }
}
