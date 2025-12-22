package us.eunoians.mcrpg.loadout;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import us.eunoians.mcrpg.exception.loadout.InvalidAbilityForLoadoutException;
import us.eunoians.mcrpg.exception.loadout.LoadoutMaxSizeExceededException;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class LoadoutTest extends McRPGBaseTest {

    private Herbalism herbalism;
    private InstantIrrigation instantIrrigation;
    private MassHarvest massHarvest;
    private VerdantSurge verdantSurge;
    private YamlDocument herbalismConfig;
    private YamlDocument mainConfig;

    @BeforeEach
    public void setup(){
        server.getPluginManager().clearEvents();
        SkillRegistry skillRegistry = new SkillRegistry(mcRPG);
        RegistryAccess.registryAccess().register(skillRegistry);
        herbalism = new Herbalism(mcRPG);
        skillRegistry.register(herbalism);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);
        herbalismConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);
        when(herbalismConfig.getString(HerbalismConfigFile.LEVEL_UP_EQUATION)).thenReturn("5");

        mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
//        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
//        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        instantIrrigation = new InstantIrrigation(mcRPG);
        abilityRegistry.register(instantIrrigation);
        massHarvest = new MassHarvest(mcRPG);
        abilityRegistry.register(massHarvest);
        verdantSurge = new VerdantSurge(mcRPG);
        abilityRegistry.register(verdantSurge);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));
        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);
    }

    @DisplayName("Given a player loadout, when getting the loadout holder, then it returns the player's UUID")
    @Test
    public void getLoadoutHolder_returnsUuid_whenPlayerLoadout(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        assertEquals(mcRPGPlayer.getUUID(), mcRPGPlayer.asSkillHolder().getLoadout(1).getLoadoutHolder());
    }

    @DisplayName("Given two player loadouts, when getting the loadout slot, then it returns the correct slot number")
    @Test
    public void getLoadoutSlot_returnsSlotNumber_whenMultipleLoadouts(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        assertEquals(1, mcRPGPlayer.asSkillHolder().getLoadout(1).getLoadoutSlot());
        assertEquals(2, mcRPGPlayer.asSkillHolder().getLoadout(2).getLoadoutSlot());
    }

    @DisplayName("Given a valid ability and available space, when adding an ability, then it is added to the loadout")
    @Test
    public void addAbility_addsAbility_whenValidAbility(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        assertDoesNotThrow(() -> loadout.addAbility(massHarvest.getAbilityKey()));
        assertTrue(loadout.getAbilities().contains(massHarvest.getAbilityKey()));
    }

    @DisplayName("Given a loadout with max size of zero, when adding an ability, then it throws LoadoutMaxSizeExceededException and adds nothing")
    @Test
    public void addAbility_throwsException_whenLoadoutMaxSizeExceeded(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(0);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        assertThrows(LoadoutMaxSizeExceededException.class, () -> loadout.addAbility(massHarvest.getAbilityKey()));
        assertTrue(loadout.getAbilities().isEmpty());
    }

    @DisplayName("Given an ability that is not valid for the loadout, when adding an ability, then it throws InvalidAbilityForLoadoutException")
    @Test
    public void addAbility_throwsException_whenAbilityInvalidForLoadout(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        assertThrows(InvalidAbilityForLoadoutException.class, () -> loadout.addAbility(instantIrrigation.getAbilityKey()));
    }

    @DisplayName("Given an ability in the loadout, when removing the ability, then it is removed from the loadout")
    @Test
    public void removeAbility_removesAbility_whenAbilityInLoadout(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        loadout.addAbility(massHarvest.getAbilityKey());
        assertTrue(loadout.getAbilities().contains(massHarvest.getAbilityKey()));
        loadout.removeAbility(massHarvest.getAbilityKey());
        assertTrue(loadout.getAbilities().isEmpty());
    }

    @DisplayName("Given an existing ability in the loadout, when replacing it with another ability, then the old is removed and the new is added")
    @Test
    public void replaceAbility_replacesAbility_whenDifferentAbilityProvided(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        loadout.addAbility(massHarvest.getAbilityKey());
        assertTrue(loadout.getAbilities().contains(massHarvest.getAbilityKey()));
        assertFalse(loadout.getAbilities().contains(verdantSurge.getAbilityKey()));
        loadout.replaceAbility(massHarvest.getAbilityKey(), verdantSurge.getAbilityKey());
        assertTrue(loadout.getAbilities().contains(verdantSurge.getAbilityKey()));
        assertFalse(loadout.getAbilities().contains(massHarvest.getAbilityKey()));
    }

    @DisplayName("Given an ability present in the loadout, when checking isAbilityInLoadout, then it returns true")
    @Test
    public void isAbilityInLoadout_returnsTrue_whenAbilityPresent(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        loadout.addAbility(massHarvest.getAbilityKey());
        assertTrue(loadout.isAbilityInLoadout(massHarvest.getAbilityKey()));
    }

    @DisplayName("Given an ability not present in the loadout, when checking isAbilityInLoadout, then it returns false")
    @Test
    public void isAbilityInLoadout_returnsFalse_whenAbilityAbsent(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        assertFalse(loadout.isAbilityInLoadout(massHarvest.getAbilityKey()));
    }

    @DisplayName("Given a loadout with one ability, when getting abilities, then it returns all abilities in the loadout")
    @Test
    public void getAbilities_returnsAbilities_whenAbilityAdded(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        loadout.addAbility(massHarvest.getAbilityKey());
        assertEquals(1, loadout.getAbilities().size());
        assertTrue(loadout.getAbilities().contains(massHarvest.getAbilityKey()));
    }

    @DisplayName("Given an empty loadout with max size five, when getting remaining size, then it returns five")
    @Test
    public void getRemainingLoadoutSize_returnsFive_whenLoadoutEmpty(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        assertEquals(5, loadout.getRemainingLoadoutSize());
    }

    @DisplayName("Given a loadout with one ability and max size five, when getting remaining size, then it returns four")
    @Test
    public void getRemainingLoadoutSize_returnsFour_whenOneAbilityAdded(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        loadout.addAbility(massHarvest.getAbilityKey());
        assertEquals(4, loadout.getRemainingLoadoutSize());
    }

    @DisplayName("Given a loadout with abilities, when copying to a new UUID, then it creates a distinct loadout with the same abilities and slot")
    @Test
    public void copyLoadout_copiesLoadout_whenNewUuidProvided(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        loadout.addAbility(massHarvest.getAbilityKey());

        UUID uuid = UUID.fromString("79734b2b-4323-44b7-a310-69065a5276c6");
        Loadout copy = loadout.copyLoadout(uuid, 1);

        assertNotEquals(loadout, copy);
        assertEquals(mcRPGPlayer.getUUID(), loadout.getLoadoutHolder());
        assertEquals(uuid, copy.getLoadoutHolder());
        assertTrue(loadout.getAbilities().contains(massHarvest.getAbilityKey()));
        assertTrue(copy.getAbilities().contains(massHarvest.getAbilityKey()));
        assertEquals(1, loadout.getAbilities().size());
        assertEquals(1, copy.getAbilities().size());
        assertEquals(1, loadout.getLoadoutSlot());
        assertEquals(1, copy.getLoadoutSlot());
    }

    @DisplayName("Given a new display, when setting it on a loadout, then getDisplay returns the new display")
    @Test
    public void getDisplay_returnsNewDisplay_whenLoadoutDisplaySet(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        LoadoutDisplay loadoutDisplay = new LoadoutDisplay(Material.ACACIA_BOAT, "test");
        assertNotEquals(loadout.getDisplay(), loadoutDisplay);
        loadout.setLoadoutDisplay(loadoutDisplay);
        assertEquals(loadout.getDisplay(), loadoutDisplay);
    }

    @DisplayName("Given a default display, when checking shouldSaveDisplay, then it returns false")
    @Test
    public void shouldSaveDisplay_returnsFalse_whenDisplayIsDefault(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        assertFalse(loadout.shouldSaveDisplay());
    }

    @DisplayName("Given a non-default display item, when checking shouldSaveDisplay, then it returns true")
    @Test
    public void shouldSaveDisplay_returnsTrue_whenDisplayNotDefault(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        loadout.getDisplay().setDisplayItem(Material.ACACIA_BOAT);
        assertTrue(loadout.shouldSaveDisplay());
    }

    @DisplayName("Given ability already in another loadout, when checking canAbilityBeAddedToLoadout, then it returns false for the first and true for the second")
    @Test
    public void canAbilityBeAddedToLoadout_returnsFalse_whenAbilityAlreadyInOtherLoadout(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout1 = mcRPGPlayer.asSkillHolder().getLoadout(1);
        Loadout loadout2 = mcRPGPlayer.asSkillHolder().getLoadout(2);
        assertDoesNotThrow(() -> loadout1.addAbility(massHarvest.getAbilityKey()));
        assertFalse(loadout1.canAbilityBeAddedToLoadout(verdantSurge.getAbilityKey()));
        assertTrue(loadout2.canAbilityBeAddedToLoadout(verdantSurge.getAbilityKey()));
    }

    @DisplayName("Given two different abilities, when checking canAbilityBeReplacedIntoLoadout, then it returns true")
    @Test
    public void canAbilityBeReplacedIntoLoadout_returnsTrue_whenAbilitiesDifferent(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        loadout.addAbility(massHarvest.getAbilityKey());
        assertTrue(loadout.canAbilityBeReplacedIntoLoadout(massHarvest.getAbilityKey(), verdantSurge.getAbilityKey()));
    }

    @DisplayName("Given the same ability as both old and new, when checking canAbilityBeReplacedIntoLoadout, then it returns false")
    @Test
    public void canAbilityBeReplacedIntoLoadout_returnsFalse_whenAbilitiesSame(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        Loadout loadout = mcRPGPlayer.asSkillHolder().getLoadout(1);
        loadout.addAbility(massHarvest.getAbilityKey());
        assertFalse(loadout.canAbilityBeReplacedIntoLoadout(massHarvest.getAbilityKey(), massHarvest.getAbilityKey()));
    }
}
