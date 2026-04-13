package us.eunoians.mcrpg.gui.loadout;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class LoadoutSelectionGuiTest extends McRPGBaseTest {

    private YamlDocument mainConfig;

    @BeforeEach
    public void setup() {
        mainConfig = mock(YamlDocument.class);
        var fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
    }

    private LoadoutSelectionGui createGui(@NotNull McRPGPlayer mcRPGPlayer, int maxLoadouts) {
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(maxLoadouts);
        addPlayerToServer(mcRPGPlayer);
        return new LoadoutSelectionGui(mcRPGPlayer);
    }

    @Test
    @DisplayName("getMaximumPage returns 1 when max loadouts is 1")
    void getMaximumPage_returnsOne_whenMaxLoadoutsIsOne(@NotNull McRPGPlayer mcRPGPlayer) {
        var gui = createGui(mcRPGPlayer, 1);
        assertEquals(1, gui.getMaximumPage());
    }

    @Test
    @DisplayName("getMaximumPage returns 1 when max loadouts fills exactly one page (9)")
    void getMaximumPage_returnsOne_whenMaxLoadoutsFillsExactlyOnePage(@NotNull McRPGPlayer mcRPGPlayer) {
        var gui = createGui(mcRPGPlayer, 9);
        assertEquals(1, gui.getMaximumPage());
    }

    @Test
    @DisplayName("getMaximumPage returns 2 when max loadouts is 10, overflowing to a second page")
    void getMaximumPage_returnsTwo_whenMaxLoadoutsOverflowsOnePage(@NotNull McRPGPlayer mcRPGPlayer) {
        var gui = createGui(mcRPGPlayer, 10);
        assertEquals(2, gui.getMaximumPage());
    }

    @Test
    @DisplayName("getMaximumPage returns 2 when max loadouts fills exactly two pages (18)")
    void getMaximumPage_returnsTwo_whenMaxLoadoutsFillsExactlyTwoPages(@NotNull McRPGPlayer mcRPGPlayer) {
        var gui = createGui(mcRPGPlayer, 18);
        assertEquals(2, gui.getMaximumPage());
    }

    @Test
    @DisplayName("getMaximumPage returns 3 when max loadouts is 19, overflowing to a third page")
    void getMaximumPage_returnsThree_whenMaxLoadoutsOverflowsTwoPages(@NotNull McRPGPlayer mcRPGPlayer) {
        var gui = createGui(mcRPGPlayer, 19);
        assertEquals(3, gui.getMaximumPage());
    }
}
