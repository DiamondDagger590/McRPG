package us.eunoians.mcrpg.loadout;

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
import us.eunoians.mcrpg.ability.stub.StubPassiveUnlockableAbility;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
class LoadoutActiveAbilitiesTest extends McRPGBaseTest {

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
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_PASSIVE_LOADOUT_SIZE)).thenReturn(2);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));

        Herbalism herbalism = new Herbalism(mcRPG);
        skillRegistry.register(herbalism);

        abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);
    }

    @Nested
    @DisplayName("getOrderedActiveAbilities")
    class GetOrderedActiveAbilities {

        @DisplayName("returns only combo-activatable abilities in insertion order")
        @Test
        void getOrderedActiveAbilities_returnsComboAbilitiesInOrder(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility combo1 = new StubComboActivatableAbility(mcRPG, "combo_1");
            StubComboActivatableAbility combo2 = new StubComboActivatableAbility(mcRPG, "combo_2");
            StubPassiveUnlockableAbility passive = new StubPassiveUnlockableAbility(mcRPG, "passive_1");
            abilityRegistry.register(combo1);
            abilityRegistry.register(combo2);
            abilityRegistry.register(passive);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(combo1.getAbilityKey());
            loadout.addAbility(passive.getAbilityKey());
            loadout.addAbility(combo2.getAbilityKey());

            List<NamespacedKey> activeAbilities = loadout.getOrderedActiveAbilities();

            assertEquals(2, activeAbilities.size());
            assertEquals(combo1.getAbilityKey(), activeAbilities.get(0));
            assertEquals(combo2.getAbilityKey(), activeAbilities.get(1));
        }

        @DisplayName("returns empty list when no combo-activatable abilities present")
        @Test
        void getOrderedActiveAbilities_returnsEmpty_whenNoComboAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            StubPassiveUnlockableAbility passive = new StubPassiveUnlockableAbility(mcRPG, "passive_only");
            abilityRegistry.register(passive);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(passive.getAbilityKey());

            List<NamespacedKey> activeAbilities = loadout.getOrderedActiveAbilities();

            assertTrue(activeAbilities.isEmpty());
        }

        @DisplayName("returns empty list for empty loadout")
        @Test
        void getOrderedActiveAbilities_returnsEmpty_whenLoadoutEmpty(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            List<NamespacedKey> activeAbilities = loadout.getOrderedActiveAbilities();

            assertTrue(activeAbilities.isEmpty());
        }
    }

    @Nested
    @DisplayName("swapActivePositions")
    class SwapActivePositions {

        @DisplayName("swaps two active abilities by combo slot index")
        @Test
        void swapActivePositions_swapsAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility combo1 = new StubComboActivatableAbility(mcRPG, "swap_combo_1");
            StubComboActivatableAbility combo2 = new StubComboActivatableAbility(mcRPG, "swap_combo_2");
            abilityRegistry.register(combo1);
            abilityRegistry.register(combo2);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(combo1.getAbilityKey());
            loadout.addAbility(combo2.getAbilityKey());

            assertEquals(combo1.getAbilityKey(), loadout.getOrderedActiveAbilities().get(0));
            assertEquals(combo2.getAbilityKey(), loadout.getOrderedActiveAbilities().get(1));

            loadout.swapActivePositions(1, 2);

            assertEquals(combo2.getAbilityKey(), loadout.getOrderedActiveAbilities().get(0));
            assertEquals(combo1.getAbilityKey(), loadout.getOrderedActiveAbilities().get(1));
        }

        @DisplayName("no-op when from and to slots are the same")
        @Test
        void swapActivePositions_noOp_whenSameSlot(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility combo1 = new StubComboActivatableAbility(mcRPG, "same_slot_combo");
            abilityRegistry.register(combo1);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(combo1.getAbilityKey());

            loadout.swapActivePositions(1, 1);

            assertEquals(combo1.getAbilityKey(), loadout.getOrderedActiveAbilities().get(0));
        }

        @DisplayName("no-op when from slot is out of range")
        @Test
        void swapActivePositions_noOp_whenFromSlotOutOfRange(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility combo1 = new StubComboActivatableAbility(mcRPG, "oor_from_combo");
            abilityRegistry.register(combo1);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(combo1.getAbilityKey());

            loadout.swapActivePositions(5, 1);

            assertEquals(combo1.getAbilityKey(), loadout.getOrderedActiveAbilities().get(0));
        }

        @DisplayName("no-op when to slot is out of range")
        @Test
        void swapActivePositions_noOp_whenToSlotOutOfRange(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility combo1 = new StubComboActivatableAbility(mcRPG, "oor_to_combo");
            abilityRegistry.register(combo1);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(combo1.getAbilityKey());

            loadout.swapActivePositions(1, 5);

            assertEquals(combo1.getAbilityKey(), loadout.getOrderedActiveAbilities().get(0));
        }

        @DisplayName("no-op when from slot is zero")
        @Test
        void swapActivePositions_noOp_whenFromSlotZero(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility combo1 = new StubComboActivatableAbility(mcRPG, "zero_from");
            StubComboActivatableAbility combo2 = new StubComboActivatableAbility(mcRPG, "zero_from_2");
            abilityRegistry.register(combo1);
            abilityRegistry.register(combo2);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(combo1.getAbilityKey());
            loadout.addAbility(combo2.getAbilityKey());

            loadout.swapActivePositions(0, 1);

            assertEquals(combo1.getAbilityKey(), loadout.getOrderedActiveAbilities().get(0));
            assertEquals(combo2.getAbilityKey(), loadout.getOrderedActiveAbilities().get(1));
        }

        @DisplayName("no-op when from slot is negative")
        @Test
        void swapActivePositions_noOp_whenFromSlotNegative(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility combo1 = new StubComboActivatableAbility(mcRPG, "neg_from");
            StubComboActivatableAbility combo2 = new StubComboActivatableAbility(mcRPG, "neg_from_2");
            abilityRegistry.register(combo1);
            abilityRegistry.register(combo2);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(combo1.getAbilityKey());
            loadout.addAbility(combo2.getAbilityKey());

            loadout.swapActivePositions(-1, 1);

            assertEquals(combo1.getAbilityKey(), loadout.getOrderedActiveAbilities().get(0));
            assertEquals(combo2.getAbilityKey(), loadout.getOrderedActiveAbilities().get(1));
        }

        @DisplayName("swaps correctly when passive abilities are interspersed")
        @Test
        void swapActivePositions_swapsCorrectly_whenPassivesInterspersed(@NotNull McRPGPlayer mcRPGPlayer) {
            StubComboActivatableAbility combo1 = new StubComboActivatableAbility(mcRPG, "interspersed_combo_1");
            StubPassiveUnlockableAbility passive = new StubPassiveUnlockableAbility(mcRPG, "interspersed_passive");
            StubComboActivatableAbility combo2 = new StubComboActivatableAbility(mcRPG, "interspersed_combo_2");
            abilityRegistry.register(combo1);
            abilityRegistry.register(passive);
            abilityRegistry.register(combo2);

            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(combo1.getAbilityKey());
            loadout.addAbility(passive.getAbilityKey());
            loadout.addAbility(combo2.getAbilityKey());

            loadout.swapActivePositions(1, 2);

            List<NamespacedKey> activeAbilities = loadout.getOrderedActiveAbilities();
            assertEquals(combo2.getAbilityKey(), activeAbilities.get(0));
            assertEquals(combo1.getAbilityKey(), activeAbilities.get(1));

            List<NamespacedKey> allAbilities = loadout.getOrderedAbilities();
            assertEquals(combo2.getAbilityKey(), allAbilities.get(0));
            assertEquals(passive.getAbilityKey(), allAbilities.get(1));
            assertEquals(combo1.getAbilityKey(), allAbilities.get(2));
        }
    }
}
