package us.eunoians.mcrpg.util.filter.skill;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
class SkillHolderDataPresentFilterTest extends McRPGBaseTest {

    private SkillHolderDataPresentFilter filter;
    private Herbalism herbalism;

    @BeforeEach
    void setup() {
        server.getPluginManager().clearEvents();

        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        YamlDocument herbalismConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);
        when(herbalismConfig.getString(HerbalismConfigFile.LEVEL_UP_EQUATION)).thenReturn("5");

        YamlDocument mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_PASSIVE_LOADOUT_SIZE)).thenReturn(2);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));

        herbalism = new Herbalism(mcRPG);
        skillRegistry.register(herbalism);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);

        filter = new SkillHolderDataPresentFilter();
    }

    @DisplayName("retains skills with holder data present")
    @Test
    void filter_retainsSkillsWithData(@NotNull McRPGPlayer mcRPGPlayer) {
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        skillHolder.addSkillHolderData(herbalism);

        Collection<Skill> result = filter.filter(mcRPGPlayer, List.of(herbalism));

        assertEquals(1, result.size());
        assertTrue(result.contains(herbalism));
    }

    @DisplayName("excludes skills without holder data")
    @Test
    void filter_excludesSkillsWithoutData(@NotNull McRPGPlayer mcRPGPlayer) {
        Collection<Skill> result = filter.filter(mcRPGPlayer, List.of(herbalism));

        assertTrue(result.isEmpty());
    }
}
