package us.eunoians.mcrpg;

import com.diamonddagger590.mccore.localization.LocalizationManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import us.eunoians.mcrpg.configuration.FileManager;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * This fixture sets up a {@link McRPG} instance with
 * some common things that are used across many tests in order
 * to streamline test development.
 */
public interface McRPGMockFixture extends BeforeAllCallback {

    McRPG mcRPG = mock(McRPG.class);

    @Override
    default void beforeAll(ExtensionContext context) {
        doReturn("McRPG").when(mcRPG).getName();
        doReturn(RegistryAccess.registryAccess()).when(mcRPG).registryAccess();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mock(FileManager.class));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mock(LocalizationManager.class));
    }
}
