package us.eunoians.mcrpg.skill.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.component.attack.ConfigurableOnAttackLevelableComponent;

/**
 * This component handles calculating how much experience can be given when
 * a skill holder attacks another entity.
 */
final class SwordsLevelOnAttackComponent extends ConfigurableOnAttackLevelableComponent {

    @NotNull
    @Override
    public YamlDocument getSkillConfiguration() {
        return RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.SWORDS_CONFIG);
    }

    @NotNull
    @Override
    public Route getAllowedItemsForExperienceGainRoute() {
        return SwordsConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN;
    }

    @NotNull
    @Override
    public String getEntityExperienceHeader() {
        return SwordsConfigFile.ENTITY_EXPERIENCE_HEADER;
    }

    @NotNull
    @Override
    public Swords getSkill() {
        return (Swords) RegistryAccess.registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkill(Swords.SWORDS_KEY);
    }
}
