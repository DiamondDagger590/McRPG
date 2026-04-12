package us.eunoians.mcrpg.command.loadout;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.impl.herbalism.MassHarvest;
import us.eunoians.mcrpg.ability.impl.herbalism.VerdantSurge;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests for the package-private {@link LoadoutCopyCommand#filterAccessibleAbilities} helper,
 * which decides which of the admin's loadout abilities are eligible to be placed into a
 * target player's loadout slot.
 */
@ExtendWith(McRPGPlayerExtension.class)
public class LoadoutCopyCommandFilterTest extends McRPGBaseTest {

    private MassHarvest massHarvest;
    private VerdantSurge verdantSurge;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();

        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);
        var herbalism = new Herbalism(mcRPG);
        skillRegistry.register(herbalism);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        YamlDocument herbalismConfig = mock(YamlDocument.class);
        YamlDocument mainConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);
        when(herbalismConfig.getString(HerbalismConfigFile.LEVEL_UP_EQUATION)).thenReturn("5");
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_ACTIVE_LOADOUT_SIZE, 3)).thenReturn(3);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        massHarvest = new MassHarvest(mcRPG);
        abilityRegistry.register(massHarvest);
        verdantSurge = new VerdantSurge(mcRPG);
        abilityRegistry.register(verdantSurge);

        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);
    }

    @DisplayName("filterAccessibleAbilities returns empty set when source loadout has no abilities")
    @Test
    public void filterAccessibleAbilities_returnsEmpty_whenSourceLoadoutIsEmpty(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder target = mcRPGPlayer.asSkillHolder();
        Loadout emptySource = new Loadout(UUID.randomUUID(), 1);

        Set<NamespacedKey> result = LoadoutCopyCommand.filterAccessibleAbilities(emptySource, target);

        assertTrue(result.isEmpty());
    }

    @DisplayName("filterAccessibleAbilities skips an ability the target does not have available")
    @Test
    public void filterAccessibleAbilities_skipsAbility_whenTargetDoesNotHaveItAvailable(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder target = mcRPGPlayer.asSkillHolder();
        // massHarvest is NOT added to target's available abilities
        Loadout source = new Loadout(UUID.randomUUID(), 1, Set.of(massHarvest.getAbilityKey()));

        Set<NamespacedKey> result = LoadoutCopyCommand.filterAccessibleAbilities(source, target);

        assertTrue(result.isEmpty());
    }

    @DisplayName("filterAccessibleAbilities skips an unlockable ability the target has available but has not unlocked")
    @Test
    public void filterAccessibleAbilities_skipsAbility_whenTargetHasItAvailableButNotUnlocked(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder target = mcRPGPlayer.asSkillHolder();
        target.addAvailableAbility(massHarvest); // available, but the unlock attribute is not set to true

        Loadout source = new Loadout(UUID.randomUUID(), 1, Set.of(massHarvest.getAbilityKey()));

        Set<NamespacedKey> result = LoadoutCopyCommand.filterAccessibleAbilities(source, target);

        assertTrue(result.isEmpty());
    }

    @DisplayName("filterAccessibleAbilities includes an unlockable ability the target has available and has unlocked")
    @Test
    public void filterAccessibleAbilities_includesAbility_whenTargetHasItAvailableAndUnlocked(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder target = mcRPGPlayer.asSkillHolder();
        target.addAvailableAbility(massHarvest);
        AbilityData abilityData = target.getAbilityData(massHarvest).orElseThrow();
        abilityData.addAttribute(new AbilityUnlockedAttribute(true));

        Loadout source = new Loadout(UUID.randomUUID(), 1, Set.of(massHarvest.getAbilityKey()));

        Set<NamespacedKey> result = LoadoutCopyCommand.filterAccessibleAbilities(source, target);

        assertEquals(Set.of(massHarvest.getAbilityKey()), result);
    }

    @DisplayName("filterAccessibleAbilities includes only accessible abilities when source has a mix")
    @Test
    public void filterAccessibleAbilities_includesOnlyAccessible_whenSourceHasMixedAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder target = mcRPGPlayer.asSkillHolder();
        // massHarvest: available AND unlocked
        target.addAvailableAbility(massHarvest);
        AbilityData abilityData = target.getAbilityData(massHarvest).orElseThrow();
        abilityData.addAttribute(new AbilityUnlockedAttribute(true));
        // verdantSurge: NOT added to target's available abilities

        Set<NamespacedKey> sourceAbilities = new HashSet<>();
        sourceAbilities.add(massHarvest.getAbilityKey());
        sourceAbilities.add(verdantSurge.getAbilityKey());
        Loadout source = new Loadout(UUID.randomUUID(), 1, sourceAbilities);

        Set<NamespacedKey> result = LoadoutCopyCommand.filterAccessibleAbilities(source, target);

        assertEquals(Set.of(massHarvest.getAbilityKey()), result);
    }

    @DisplayName("filterAccessibleAbilities returns all abilities when target has all of them available and unlocked")
    @Test
    public void filterAccessibleAbilities_returnsAll_whenTargetHasAccessToAll(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder target = mcRPGPlayer.asSkillHolder();
        target.addAvailableAbility(massHarvest);
        AbilityData massHarvestData = target.getAbilityData(massHarvest).orElseThrow();
        massHarvestData.addAttribute(new AbilityUnlockedAttribute(true));
        target.addAvailableAbility(verdantSurge);
        AbilityData verdantSurgeData = target.getAbilityData(verdantSurge).orElseThrow();
        verdantSurgeData.addAttribute(new AbilityUnlockedAttribute(true));

        Set<NamespacedKey> sourceAbilities = new HashSet<>();
        sourceAbilities.add(massHarvest.getAbilityKey());
        sourceAbilities.add(verdantSurge.getAbilityKey());
        Loadout source = new Loadout(UUID.randomUUID(), 1, sourceAbilities);

        Set<NamespacedKey> result = LoadoutCopyCommand.filterAccessibleAbilities(source, target);

        assertEquals(sourceAbilities, result);
    }
}
