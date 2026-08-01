package us.eunoians.mcrpg.combat;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Shared test support for combat-tracker tests. Centralizes the combat configuration stubbing so
 * combat test classes do not duplicate it (and so a new config key only needs to be added once).
 */
public final class CombatTestSupport {

    private CombatTestSupport() {
    }

    /**
     * Stubs the combat configuration file on the given plugin's file manager with the provided values
     * and returns the mocked {@link YamlDocument} so individual tests can override specific keys.
     *
     * @param mcRPG               The plugin whose file manager to stub.
     * @param timeoutSeconds      Value for {@link CombatConfigFile#SESSION_TIMEOUT_SECONDS}.
     * @param maxMobParticipants  Value for {@link CombatConfigFile#MAX_MOB_PARTICIPANTS}.
     * @param scanIntervalSeconds Value for {@link CombatConfigFile#TIMEOUT_SCAN_INTERVAL_SECONDS}.
     * @return The mocked combat configuration {@link YamlDocument}.
     */
    @NotNull
    public static YamlDocument mockCombatConfig(@NotNull McRPG mcRPG, double timeoutSeconds,
                                                int maxMobParticipants, double scanIntervalSeconds) {
        FileManager fileManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument combatConfig = mock(YamlDocument.class);
        lenient().when(fileManager.getFile(FileType.COMBAT_CONFIG)).thenReturn(combatConfig);
        lenient().when(combatConfig.getDouble(CombatConfigFile.SESSION_TIMEOUT_SECONDS)).thenReturn(timeoutSeconds);
        lenient().when(combatConfig.getInt(CombatConfigFile.MAX_MOB_PARTICIPANTS)).thenReturn(maxMobParticipants);
        lenient().when(combatConfig.getDouble(CombatConfigFile.TIMEOUT_SCAN_INTERVAL_SECONDS)).thenReturn(scanIntervalSeconds);
        return combatConfig;
    }
}
