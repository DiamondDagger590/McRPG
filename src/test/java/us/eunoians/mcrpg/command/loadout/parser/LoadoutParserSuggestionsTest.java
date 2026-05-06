package us.eunoians.mcrpg.command.loadout.parser;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link LoadoutParser#computeSuggestions(LoadoutHolder)}, the pure-logic core
 * of tab-completion that is decoupled from Cloud's {@code CommandContext}.
 */
@ExtendWith(McRPGPlayerExtension.class)
public class LoadoutParserSuggestionsTest extends McRPGBaseTest {

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

    @DisplayName("computeSuggestions returns one slot-number string per valid slot")
    @Test
    public void computeSuggestions_returnsSlotNumbers_forEachValidSlot(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();

        List<String> suggestions = LoadoutParser.computeSuggestions(holder);

        assertEquals(MAX_SLOTS, suggestions.size());
        for (int i = 1; i <= MAX_SLOTS; i++) {
            assertTrue(suggestions.contains(Integer.toString(i)));
        }
    }

    @DisplayName("computeSuggestions does not include the default loadout name when the display has not been customised")
    @Test
    public void computeSuggestions_excludesDefaultDisplay_whenLoadoutNotCustomised(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1); // auto-creates slot with default (non-customised) display

        List<String> suggestions = LoadoutParser.computeSuggestions(holder);

        // Only slot numbers should be present; no display name for an uncustomised loadout
        assertEquals(MAX_SLOTS, suggestions.size());
    }

    @DisplayName("computeSuggestions appends the plain-text custom display name when a loadout is named")
    @Test
    public void computeSuggestions_includesCustomName_whenLoadoutDisplayIsSet(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1).getDisplay().setDisplayName("pvp");

        List<String> suggestions = LoadoutParser.computeSuggestions(holder);

        assertTrue(suggestions.contains("pvp"));
        assertEquals(MAX_SLOTS + 1, suggestions.size()); // 3 slot numbers + "pvp"
    }

    @DisplayName("computeSuggestions strips MiniMessage formatting tags from custom display names")
    @Test
    public void computeSuggestions_stripsFormattingTags_whenDisplayNameContainsMiniMessage(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(2).getDisplay().setDisplayName("<red>combat</red>");

        List<String> suggestions = LoadoutParser.computeSuggestions(holder);

        assertTrue(suggestions.contains("combat"));
        assertFalse(suggestions.contains("<red>combat</red>"));
    }

    @DisplayName("computeSuggestions includes names for all customised loadouts and slot numbers for all valid slots")
    @Test
    public void computeSuggestions_includesAllNamesAndSlots_whenMultipleLoadoutsNamed(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        holder.getLoadout(1).getDisplay().setDisplayName("mining");
        holder.getLoadout(2).getDisplay().setDisplayName("pvp");
        // Slot 3 is not customised

        List<String> suggestions = LoadoutParser.computeSuggestions(holder);

        assertEquals(MAX_SLOTS + 2, suggestions.size()); // 3 slot numbers + "mining" + "pvp"
        assertTrue(suggestions.contains("mining"));
        assertTrue(suggestions.contains("pvp"));
        assertTrue(suggestions.contains("1"));
        assertTrue(suggestions.contains("2"));
        assertTrue(suggestions.contains("3"));
    }

    @DisplayName("computeSuggestions returns only slot numbers when max loadout count is reduced")
    @Test
    public void computeSuggestions_respectsReducedMax_whenConfigChanges(@NotNull McRPGPlayer mcRPGPlayer) {
        LoadoutHolder holder = mcRPGPlayer.asSkillHolder();
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(1);

        List<String> suggestions = LoadoutParser.computeSuggestions(holder);

        assertEquals(1, suggestions.size());
        assertTrue(suggestions.contains("1"));
        assertFalse(suggestions.contains("2"));
    }
}
