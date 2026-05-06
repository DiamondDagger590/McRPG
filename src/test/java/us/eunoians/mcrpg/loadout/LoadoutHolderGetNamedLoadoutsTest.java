package us.eunoians.mcrpg.loadout;

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
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class LoadoutHolderGetNamedLoadoutsTest extends McRPGBaseTest {

    private static final int MAX_SLOTS = 3;

    private YamlDocument mainConfig;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();

        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        mainConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(MAX_SLOTS);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);
    }

    @DisplayName("getNamedLoadouts returns empty list when no loadouts have been created yet")
    @Test
    public void getNamedLoadouts_returnsEmpty_whenNoLoadoutsLoaded(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();

        List<Loadout> named = holder.getNamedLoadouts();

        assertTrue(named.isEmpty());
    }

    @DisplayName("getNamedLoadouts returns empty list when a loadout exists but still has the default display")
    @Test
    public void getNamedLoadouts_returnsEmpty_whenLoadoutHasDefaultDisplay(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1); // triggers slot creation with default display (shouldSaveDisplay() == false)

        List<Loadout> named = holder.getNamedLoadouts();

        assertTrue(named.isEmpty());
    }

    @DisplayName("getNamedLoadouts returns the loadout when its display name has been set")
    @Test
    public void getNamedLoadouts_returnsLoadout_whenDisplayNameSet(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1).getDisplay().setDisplayName("mining");

        List<Loadout> named = holder.getNamedLoadouts();

        assertEquals(1, named.size());
        assertEquals(1, named.get(0).getLoadoutSlot());
    }

    @DisplayName("getNamedLoadouts returns all named loadouts when multiple slots have custom names")
    @Test
    public void getNamedLoadouts_returnsAll_whenMultipleSlotsNamed(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1).getDisplay().setDisplayName("pvp");
        holder.getLoadout(2).getDisplay().setDisplayName("mining");

        List<Loadout> named = holder.getNamedLoadouts();

        assertEquals(2, named.size());
    }

    @DisplayName("getNamedLoadouts excludes default-display slots even when some are customised")
    @Test
    public void getNamedLoadouts_excludesDefaultDisplay_whenMixedLoadouts(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1).getDisplay().setDisplayName("combat"); // customised
        holder.getLoadout(2); // default display, not customised

        List<Loadout> named = holder.getNamedLoadouts();

        assertEquals(1, named.size());
        assertEquals(1, named.get(0).getLoadoutSlot());
    }

    @DisplayName("getNamedLoadouts excludes a named loadout whose slot exceeds the current max")
    @Test
    public void getNamedLoadouts_excludesOutOfBoundsSlot_whenMaxReduced(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        // Access slot MAX_SLOTS (valid under current max) and give it a name
        holder.getLoadout(MAX_SLOTS).getDisplay().setDisplayName("old-slot");
        // Reduce the max so slot MAX_SLOTS is no longer in range
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(MAX_SLOTS - 1);

        List<Loadout> named = holder.getNamedLoadouts();

        assertTrue(named.isEmpty());
    }

    @DisplayName("getNamedLoadouts does not auto-create slots as a side effect")
    @Test
    public void getNamedLoadouts_doesNotAutoCreateSlots_whenCalled(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        int loadoutCountBefore = holder.getLoadedLoadoutCount();

        holder.getNamedLoadouts();

        assertEquals(loadoutCountBefore, holder.getLoadedLoadoutCount());
    }
}
