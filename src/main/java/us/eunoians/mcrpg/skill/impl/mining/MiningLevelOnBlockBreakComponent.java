package us.eunoians.mcrpg.skill.impl.mining;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.component.block.ConfigurableOnBlockBreakLevelableComponent;

/**
 * This component handles calculating how much experience can be given when
 * a skill holder breaks a block.
 */
final class MiningLevelOnBlockBreakComponent extends ConfigurableOnBlockBreakLevelableComponent {

    @NotNull
    @Override
    public YamlDocument getSkillConfiguration() {
        return RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MINING_CONFIG);
    }

    @NotNull
    @Override
    public Route getAllowedItemsForExperienceGainRoute() {
        return MiningConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN;
    }

    @NotNull
    @Override
    public String getBlockExperienceHeader() {
        return MiningConfigFile.BLOCK_EXPERIENCE_HEADER;
    }

    @NotNull
    @Override
    public Mining getSkill() {
        return (Mining) RegistryAccess.registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkill(Mining.MINING_KEY);
    }
}

