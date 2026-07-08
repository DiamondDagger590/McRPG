package us.eunoians.mcrpg.util.filter.key;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.stub.StubComboActivatableAbility;
import us.eunoians.mcrpg.ability.stub.StubInnateAbility;
import us.eunoians.mcrpg.ability.stub.StubPassiveUnlockableAbility;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
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
class AbilityKeyFilterTest extends McRPGBaseTest {

    private AbilityRegistry abilityRegistry;
    private YamlDocument mainConfig;

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

        mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);

        Herbalism herbalism = new Herbalism(mcRPG);
        skillRegistry.register(herbalism);

        abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));
        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);

        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_PASSIVE_LOADOUT_SIZE)).thenReturn(2);
    }

    @Nested
    @DisplayName("AbilityKeyComboActivatableFilter")
    class ComboActivatableFilterTests {

        @DisplayName("comboActivatableOnly=true retains only combo-activatable keys")
        @Test
        void filter_retainsComboActivatable_whenTrue(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility comboAbility = new StubComboActivatableAbility(mcRPG, "combo_ability");
            StubPassiveUnlockableAbility passiveAbility = new StubPassiveUnlockableAbility(mcRPG, "passive_ability");
            abilityRegistry.register(comboAbility);
            abilityRegistry.register(passiveAbility);

            AbilityKeyComboActivatableFilter filter = new AbilityKeyComboActivatableFilter(true);
            Collection<NamespacedKey> result = filter.filter(mcRPGPlayer,
                    List.of(comboAbility.getAbilityKey(), passiveAbility.getAbilityKey()));

            assertEquals(1, result.size());
            assertTrue(result.contains(comboAbility.getAbilityKey()));
        }

        @DisplayName("comboActivatableOnly=false retains only non-combo-activatable keys")
        @Test
        void filter_retainsNonComboActivatable_whenFalse(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility comboAbility = new StubComboActivatableAbility(mcRPG, "combo_ability");
            StubPassiveUnlockableAbility passiveAbility = new StubPassiveUnlockableAbility(mcRPG, "passive_ability");
            abilityRegistry.register(comboAbility);
            abilityRegistry.register(passiveAbility);

            AbilityKeyComboActivatableFilter filter = new AbilityKeyComboActivatableFilter(false);
            Collection<NamespacedKey> result = filter.filter(mcRPGPlayer,
                    List.of(comboAbility.getAbilityKey(), passiveAbility.getAbilityKey()));

            assertEquals(1, result.size());
            assertTrue(result.contains(passiveAbility.getAbilityKey()));
        }

        @DisplayName("returns empty for empty input")
        @Test
        void filter_returnsEmpty_whenInputEmpty(@NotNull McRPGPlayer mcRPGPlayer) {
            AbilityKeyComboActivatableFilter filter = new AbilityKeyComboActivatableFilter(true);
            Collection<NamespacedKey> result = filter.filter(mcRPGPlayer, List.of());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("AbilityKeyUnlockedFilter")
    class UnlockedFilterTests {

        private AbilityKeyUnlockedFilter filter;

        @BeforeEach
        void setUp() {
            filter = new AbilityKeyUnlockedFilter();
        }

        @DisplayName("retains keys for registered unlockable abilities")
        @Test
        void filter_retainsUnlockableKeys(@NotNull McRPGPlayer mcRPGPlayer) {
            StubPassiveUnlockableAbility unlockable = new StubPassiveUnlockableAbility(mcRPG, "unlockable_one");
            StubInnateAbility innate = new StubInnateAbility(mcRPG, "innate_one");
            abilityRegistry.register(unlockable);
            abilityRegistry.register(innate);

            Collection<NamespacedKey> result = filter.filter(mcRPGPlayer,
                    List.of(unlockable.getAbilityKey(), innate.getAbilityKey()));

            assertEquals(1, result.size());
            assertTrue(result.contains(unlockable.getAbilityKey()));
        }

        @DisplayName("excludes unregistered ability keys")
        @Test
        void filter_excludesUnregisteredKeys(@NotNull McRPGPlayer mcRPGPlayer) {
            NamespacedKey unregistered = new NamespacedKey(mcRPG, "unregistered");

            Collection<NamespacedKey> result = filter.filter(mcRPGPlayer, List.of(unregistered));

            assertTrue(result.isEmpty());
        }

        @DisplayName("returns empty for empty input")
        @Test
        void filter_returnsEmpty_whenInputEmpty(@NotNull McRPGPlayer mcRPGPlayer) {
            Collection<NamespacedKey> result = filter.filter(mcRPGPlayer, List.of());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("AbilityKeyInLoadoutFilter")
    class InLoadoutFilterTests {

        @DisplayName("without replacement target, retains keys that can be added")
        @Test
        void filter_retainsAddableKeys_whenNoReplacement(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility active1 = new StubComboActivatableAbility(mcRPG, "active_1");
            StubComboActivatableAbility active2 = new StubComboActivatableAbility(mcRPG, "active_2");
            abilityRegistry.register(active1);
            abilityRegistry.register(active2);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);

            AbilityKeyInLoadoutFilter filter = new AbilityKeyInLoadoutFilter(loadout, null);
            Collection<NamespacedKey> result = filter.filter(mcRPGPlayer,
                    List.of(active1.getAbilityKey(), active2.getAbilityKey()));

            assertEquals(2, result.size());
        }

        @DisplayName("with replacement target, uses canAbilityBeReplacedIntoLoadout")
        @Test
        void filter_usesReplacementLogic_whenReplacementProvided(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility active1 = new StubComboActivatableAbility(mcRPG, "active_replace_1");
            StubComboActivatableAbility active2 = new StubComboActivatableAbility(mcRPG, "active_replace_2");
            abilityRegistry.register(active1);
            abilityRegistry.register(active2);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.equipAbility(active1.getAbilityKey());

            AbilityKeyInLoadoutFilter filter = new AbilityKeyInLoadoutFilter(loadout, active1.getAbilityKey());
            Collection<NamespacedKey> result = filter.filter(mcRPGPlayer, List.of(active2.getAbilityKey()));

            assertEquals(1, result.size());
            assertTrue(result.contains(active2.getAbilityKey()));
        }

        @DisplayName("excludes abilities already in loadout when adding without replacement")
        @Test
        void filter_excludesDuplicates_whenAdding(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility active1 = new StubComboActivatableAbility(mcRPG, "active_dup_1");
            abilityRegistry.register(active1);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.equipAbility(active1.getAbilityKey());

            AbilityKeyInLoadoutFilter filter = new AbilityKeyInLoadoutFilter(loadout, null);
            Collection<NamespacedKey> result = filter.filter(mcRPGPlayer, List.of(active1.getAbilityKey()));

            assertTrue(result.isEmpty());
        }
    }
}
