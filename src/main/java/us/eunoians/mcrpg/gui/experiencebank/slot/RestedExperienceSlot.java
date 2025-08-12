package us.eunoians.mcrpg.gui.experiencebank.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.experiencebank.ExperienceBankGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This slot is used by the {@link ExperienceBankGui} in order to display
 * the player's rested experience count and explain how it works.
 */
public class RestedExperienceSlot implements McRPGSlot {

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.EXPERIENCE_BANK_GUI_RESTED_EXPERIENCE_SLOT_DISPLAY_ITEM))
                .addPlaceholders(getPlaceholders(mcRPGPlayer));
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(ExperienceBankGui.class);
    }

    @NotNull
    private Map<String, String> getPlaceholders(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();
        YamlDocument configFile = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG);
        double boostedExperienceUsageRate = configFile.getDouble(MainConfigFile.RESTED_EXPERIENCE_USAGE_RATE);
        int baseExampleExperience = Integer.parseInt(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessage(mcRPGPlayer, LocalizationKey.EXPERIENCE_BANK_GUI_RESTED_EXPERIENCE_SLOT_EXAMPLE_BASE_AMOUNT));
        int boostedExperienceAmount = (int) (boostedExperienceUsageRate * baseExampleExperience);
        placeholders.put("example-base-amount", Integer.toString(baseExampleExperience));
        placeholders.put("example-boosted-amount", Integer.toString(boostedExperienceAmount));
        placeholders.put("example-consumed-amount", Integer.toString(boostedExperienceAmount - baseExampleExperience));
        placeholders.put("rested-experience-usage-rate", Double.toString(boostedExperienceUsageRate));
        placeholders.put("rested-experience", Float.toString(mcRPGPlayer.getExperienceExtras().getRestedExperience()));
        return placeholders;
    }
}
