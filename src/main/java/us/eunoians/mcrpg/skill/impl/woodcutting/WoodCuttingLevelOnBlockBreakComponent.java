package us.eunoians.mcrpg.skill.impl.woodcutting;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.component.block.ConfigurableOnBlockBreakLevelableComponent;

final class WoodCuttingLevelOnBlockBreakComponent extends ConfigurableOnBlockBreakLevelableComponent {

    @NotNull
    @Override
    public YamlDocument getSkillConfiguration() {
        return RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.WOODCUTTING_CONFIG);
    }

    @NotNull
    @Override
    public Route getAllowedItemsForExperienceGainRoute() {
        return WoodcuttingConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN;
    }

    @NotNull
    @Override
    public String getBlockExperienceHeader() {
        return WoodcuttingConfigFile.BLOCK_EXPERIENCE_HEADER;
    }

    @NotNull
    @Override
    public WoodCutting getSkill() {
        return (WoodCutting) RegistryAccess.registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkill(WoodCutting.WOODCUTTING_KEY);
    }
}
