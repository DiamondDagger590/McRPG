package us.eunoians.mcrpg.util.filter.core;

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
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.stub.StubActiveUnlockableAbility;
import us.eunoians.mcrpg.ability.stub.StubInnateAbility;
import us.eunoians.mcrpg.ability.stub.StubPassiveUnlockableAbility;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.util.filter.ability.ActiveAbilityFilter;
import us.eunoians.mcrpg.util.filter.ability.UnlockableAbilityFilter;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
class McRPGChainPlayerContextFilterTest extends McRPGBaseTest {

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

        Herbalism herbalism = new Herbalism(mcRPG);
        skillRegistry.register(herbalism);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);
    }

    @DisplayName("chains multiple filters in sequence")
    @Test
    void filter_chainsFiltersSequentially(@NotNull McRPGPlayer mcRPGPlayer) {
        Ability activeUnlockable = new StubActiveUnlockableAbility(mcRPG, "active_chain");
        Ability passiveUnlockable = new StubPassiveUnlockableAbility(mcRPG, "passive_chain");
        Ability innate = new StubInnateAbility(mcRPG, "innate_chain");

        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
        abilityRegistry.register(activeUnlockable);
        abilityRegistry.register(passiveUnlockable);
        abilityRegistry.register(innate);

        McRPGChainPlayerContextFilter<Ability> chain = new McRPGChainPlayerContextFilter<>(
                new UnlockableAbilityFilter(),
                new ActiveAbilityFilter()
        );

        Collection<Ability> result = chain.filter(mcRPGPlayer, List.of(activeUnlockable, passiveUnlockable, innate));

        assertEquals(1, result.size());
        assertTrue(result.contains(activeUnlockable));
    }

    @DisplayName("single filter in chain behaves like standalone filter")
    @Test
    void filter_singleFilterBehavesLikeStandalone(@NotNull McRPGPlayer mcRPGPlayer) {
        Ability activeUnlockable = new StubActiveUnlockableAbility(mcRPG, "single_active");
        Ability innate = new StubInnateAbility(mcRPG, "single_innate");

        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
        abilityRegistry.register(activeUnlockable);
        abilityRegistry.register(innate);

        McRPGChainPlayerContextFilter<Ability> chain = new McRPGChainPlayerContextFilter<>(
                new UnlockableAbilityFilter()
        );

        Collection<Ability> result = chain.filter(mcRPGPlayer, List.of(activeUnlockable, innate));

        assertEquals(1, result.size());
        assertTrue(result.contains(activeUnlockable));
    }

    @DisplayName("empty input returns empty through chain")
    @Test
    void filter_returnsEmpty_whenInputEmpty(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGChainPlayerContextFilter<Ability> chain = new McRPGChainPlayerContextFilter<>(
                new UnlockableAbilityFilter(),
                new ActiveAbilityFilter()
        );

        Collection<Ability> result = chain.filter(mcRPGPlayer, List.of());

        assertTrue(result.isEmpty());
    }
}
