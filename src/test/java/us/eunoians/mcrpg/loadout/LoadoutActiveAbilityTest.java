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
import us.eunoians.mcrpg.ability.impl.herbalism.InstantIrrigation;
import us.eunoians.mcrpg.ability.impl.herbalism.MassHarvest;
import us.eunoians.mcrpg.ability.impl.herbalism.VerdantSurge;
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

import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@DisplayName("Loadout active ability operations")
@ExtendWith(McRPGPlayerExtension.class)
public class LoadoutActiveAbilityTest extends McRPGBaseTest {

    private MassHarvest massHarvest;
    private VerdantSurge verdantSurge;
    private InstantIrrigation instantIrrigation;
    private YamlDocument mainConfig;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);
        Herbalism herbalism = new Herbalism(mcRPG);
        skillRegistry.register(herbalism);

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

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        massHarvest = new MassHarvest(mcRPG);
        abilityRegistry.register(massHarvest);
        verdantSurge = new VerdantSurge(mcRPG);
        abilityRegistry.register(verdantSurge);
        instantIrrigation = new InstantIrrigation(mcRPG);
        abilityRegistry.register(instantIrrigation);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));
        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);
    }

    private Loadout createLoadoutWithAbilities(@NotNull McRPGPlayer mcRPGPlayer, NamespacedKey... keys) {
        LinkedHashSet<NamespacedKey> abilities = new LinkedHashSet<>();
        for (NamespacedKey key : keys) {
            abilities.add(key);
        }
        return new Loadout(mcRPGPlayer.getUUID(), 1, abilities);
    }

    @Nested
    @DisplayName("getOrderedActiveAbilities")
    class GetOrderedActiveAbilities {

        @DisplayName("returns only ComboActivatable abilities")
        @Test
        public void getOrderedActiveAbilities_returnsOnlyActive(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = createLoadoutWithAbilities(mcRPGPlayer,
                    massHarvest.getAbilityKey(), instantIrrigation.getAbilityKey(), verdantSurge.getAbilityKey());

            List<NamespacedKey> activeAbilities = loadout.getOrderedActiveAbilities();

            assertEquals(2, activeAbilities.size());
            assertTrue(activeAbilities.contains(massHarvest.getAbilityKey()));
            assertTrue(activeAbilities.contains(verdantSurge.getAbilityKey()));
        }

        @DisplayName("preserves insertion order among active abilities")
        @Test
        public void getOrderedActiveAbilities_preservesInsertionOrder(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = createLoadoutWithAbilities(mcRPGPlayer,
                    verdantSurge.getAbilityKey(), instantIrrigation.getAbilityKey(), massHarvest.getAbilityKey());

            List<NamespacedKey> activeAbilities = loadout.getOrderedActiveAbilities();

            assertEquals(List.of(verdantSurge.getAbilityKey(), massHarvest.getAbilityKey()), activeAbilities);
        }

        @DisplayName("returns empty list when no active abilities present")
        @Test
        public void getOrderedActiveAbilities_returnsEmpty_whenNoActiveAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = createLoadoutWithAbilities(mcRPGPlayer, instantIrrigation.getAbilityKey());

            assertTrue(loadout.getOrderedActiveAbilities().isEmpty());
        }

        @DisplayName("returns empty list when loadout is empty")
        @Test
        public void getOrderedActiveAbilities_returnsEmpty_whenLoadoutEmpty(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);

            assertTrue(loadout.getOrderedActiveAbilities().isEmpty());
        }
    }

    @Nested
    @DisplayName("swapActivePositions")
    class SwapActivePositions {

        @DisplayName("swaps two active abilities")
        @Test
        public void swapActivePositions_swapsTwoActiveAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(massHarvest.getAbilityKey());
            loadout.addAbility(verdantSurge.getAbilityKey());

            assertEquals(List.of(massHarvest.getAbilityKey(), verdantSurge.getAbilityKey()), loadout.getOrderedActiveAbilities());

            loadout.swapActivePositions(1, 2);

            assertEquals(List.of(verdantSurge.getAbilityKey(), massHarvest.getAbilityKey()), loadout.getOrderedActiveAbilities());
        }

        @DisplayName("no-op when from and to slots are the same")
        @Test
        public void swapActivePositions_noOp_whenSameSlot(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(massHarvest.getAbilityKey());
            loadout.addAbility(verdantSurge.getAbilityKey());

            loadout.swapActivePositions(1, 1);

            assertEquals(List.of(massHarvest.getAbilityKey(), verdantSurge.getAbilityKey()), loadout.getOrderedActiveAbilities());
        }

        @DisplayName("no-op when fromComboSlot is out of range")
        @Test
        public void swapActivePositions_noOp_whenFromSlotOutOfRange(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(massHarvest.getAbilityKey());
            loadout.addAbility(verdantSurge.getAbilityKey());

            loadout.swapActivePositions(3, 1);

            assertEquals(List.of(massHarvest.getAbilityKey(), verdantSurge.getAbilityKey()), loadout.getOrderedActiveAbilities());
        }

        @DisplayName("no-op when toComboSlot is out of range")
        @Test
        public void swapActivePositions_noOp_whenToSlotOutOfRange(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(massHarvest.getAbilityKey());
            loadout.addAbility(verdantSurge.getAbilityKey());

            loadout.swapActivePositions(1, 3);

            assertEquals(List.of(massHarvest.getAbilityKey(), verdantSurge.getAbilityKey()), loadout.getOrderedActiveAbilities());
        }

        @DisplayName("no-op when fromComboSlot is zero")
        @Test
        public void swapActivePositions_noOp_whenFromSlotZero(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(massHarvest.getAbilityKey());
            loadout.addAbility(verdantSurge.getAbilityKey());

            loadout.swapActivePositions(0, 1);

            assertEquals(List.of(massHarvest.getAbilityKey(), verdantSurge.getAbilityKey()), loadout.getOrderedActiveAbilities());
        }

        @DisplayName("no-op when toComboSlot is negative")
        @Test
        public void swapActivePositions_noOp_whenToSlotNegative(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
            loadout.addAbility(massHarvest.getAbilityKey());
            loadout.addAbility(verdantSurge.getAbilityKey());

            loadout.swapActivePositions(1, -1);

            assertEquals(List.of(massHarvest.getAbilityKey(), verdantSurge.getAbilityKey()), loadout.getOrderedActiveAbilities());
        }

        @DisplayName("preserves passive ability positions when swapping active abilities")
        @Test
        public void swapActivePositions_preservesPassivePositions(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = createLoadoutWithAbilities(mcRPGPlayer,
                    massHarvest.getAbilityKey(), instantIrrigation.getAbilityKey(), verdantSurge.getAbilityKey());

            loadout.swapActivePositions(1, 2);

            List<NamespacedKey> ordered = loadout.getOrderedAbilities();
            assertEquals(verdantSurge.getAbilityKey(), ordered.get(0));
            assertEquals(instantIrrigation.getAbilityKey(), ordered.get(1));
            assertEquals(massHarvest.getAbilityKey(), ordered.get(2));
        }

        @DisplayName("no-op when loadout has no active abilities")
        @Test
        public void swapActivePositions_noOp_whenNoActiveAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            Loadout loadout = createLoadoutWithAbilities(mcRPGPlayer, instantIrrigation.getAbilityKey());

            loadout.swapActivePositions(1, 2);

            assertEquals(List.of(instantIrrigation.getAbilityKey()), loadout.getOrderedAbilities());
        }
    }
}
