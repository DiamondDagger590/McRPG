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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class LoadoutHolderResolutionTest extends McRPGBaseTest {

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

    @DisplayName("Given input '1', when resolving loadout, then Found is returned for slot 1")
    @Test
    public void resolveLoadout_returnsFound_whenInputIsValidSlotNumber(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();

        LoadoutResolution result = holder.resolveLoadout("1");

        assertInstanceOf(LoadoutResolution.Found.class, result);
        assertEquals(1, ((LoadoutResolution.Found) result).loadout().getLoadoutSlot());
    }

    @DisplayName("Given input '3' and max 3 slots, when resolving loadout, then Found is returned for slot 3")
    @Test
    public void resolveLoadout_returnsFound_whenInputIsLastValidSlot(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();

        LoadoutResolution result = holder.resolveLoadout(Integer.toString(MAX_SLOTS));

        assertInstanceOf(LoadoutResolution.Found.class, result);
        assertEquals(MAX_SLOTS, ((LoadoutResolution.Found) result).loadout().getLoadoutSlot());
    }

    @DisplayName("Given input '99' and max 3 slots, when resolving loadout, then NotFound is returned")
    @Test
    public void resolveLoadout_returnsNotFound_whenSlotExceedsMax(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();

        LoadoutResolution result = holder.resolveLoadout("99");

        assertInstanceOf(LoadoutResolution.NotFound.class, result);
    }

    @DisplayName("Given a loadout named 'mining loadout', when resolving with exact name, then Found is returned")
    @Test
    public void resolveLoadout_returnsFound_whenExactNameMatches(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1).getDisplay().setDisplayName("mining loadout");

        LoadoutResolution result = holder.resolveLoadout("mining loadout");

        assertInstanceOf(LoadoutResolution.Found.class, result);
        assertEquals(1, ((LoadoutResolution.Found) result).loadout().getLoadoutSlot());
    }

    @DisplayName("Given a loadout named 'Mining Loadout', when resolving with lowercase exact name, then Found is returned (case-insensitive)")
    @Test
    public void resolveLoadout_returnsFound_whenExactNameMatchesCaseInsensitive(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1).getDisplay().setDisplayName("Mining Loadout");

        LoadoutResolution result = holder.resolveLoadout("mining loadout");

        assertInstanceOf(LoadoutResolution.Found.class, result);
        assertEquals(1, ((LoadoutResolution.Found) result).loadout().getLoadoutSlot());
    }

    @DisplayName("Given a loadout named 'mining loadout', when resolving with substring 'mining', then Found is returned")
    @Test
    public void resolveLoadout_returnsFound_whenSubstringMatchesUniquely(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1).getDisplay().setDisplayName("mining loadout");

        LoadoutResolution result = holder.resolveLoadout("mining");

        assertInstanceOf(LoadoutResolution.Found.class, result);
        assertEquals(1, ((LoadoutResolution.Found) result).loadout().getLoadoutSlot());
    }

    @DisplayName("Given two loadouts containing 'mining' in their names, when resolving with 'mining', then Ambiguous is returned")
    @Test
    public void resolveLoadout_returnsAmbiguous_whenSubstringMatchesMultiple(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1).getDisplay().setDisplayName("mining loadout");
        holder.getLoadout(2).getDisplay().setDisplayName("mining skills");

        LoadoutResolution result = holder.resolveLoadout("mining");

        assertInstanceOf(LoadoutResolution.Ambiguous.class, result);
        assertEquals(2, ((LoadoutResolution.Ambiguous) result).matches().size());
    }

    @DisplayName("Given no loadout with a matching name, when resolving with a name string, then NotFound is returned")
    @Test
    public void resolveLoadout_returnsNotFound_whenNoNameMatches(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1).getDisplay().setDisplayName("combat build");

        LoadoutResolution result = holder.resolveLoadout("mining");

        assertInstanceOf(LoadoutResolution.NotFound.class, result);
    }

    @DisplayName("Given input '2' with slot index priority, when resolving with matching slot number, then Found for slot 2 even if names also contain '2'")
    @Test
    public void resolveLoadout_prefersSlotIndex_overNameMatch(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        // Slot 1's name contains "2" — slot index should win for input "2"
        holder.getLoadout(1).getDisplay().setDisplayName("build 2");

        LoadoutResolution result = holder.resolveLoadout("2");

        // Should resolve to slot 2 by index, not slot 1 by substring
        assertInstanceOf(LoadoutResolution.Found.class, result);
        assertEquals(2, ((LoadoutResolution.Found) result).loadout().getLoadoutSlot());
    }

    @DisplayName("Given a loadout with a MiniMessage-tagged name, when resolving by plain text, then Found is returned")
    @Test
    public void resolveLoadout_stripsFormattingTags_whenMatchingByName(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        // Store a name with MiniMessage formatting
        holder.getLoadout(1).getDisplay().setDisplayName("<red>combat</red>");

        // Resolve using the plain-text version of the name
        LoadoutResolution result = holder.resolveLoadout("combat");

        assertInstanceOf(LoadoutResolution.Found.class, result);
        assertEquals(1, ((LoadoutResolution.Found) result).loadout().getLoadoutSlot());
    }

    @DisplayName("Given exact name collision across two loadouts, when resolving, then Ambiguous is returned")
    @Test
    public void resolveLoadout_returnsAmbiguous_whenExactNameMatchesMultiple(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1).getDisplay().setDisplayName("pvp");
        holder.getLoadout(2).getDisplay().setDisplayName("pvp");

        LoadoutResolution result = holder.resolveLoadout("pvp");

        assertInstanceOf(LoadoutResolution.Ambiguous.class, result);
        assertEquals(2, ((LoadoutResolution.Ambiguous) result).matches().size());
    }
}
