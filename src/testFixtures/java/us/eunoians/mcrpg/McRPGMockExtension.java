package us.eunoians.mcrpg;

import com.diamonddagger590.mccore.localization.LocalizationManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.configuration.FileManager;

import java.util.logging.Logger;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * This fixture sets up a {@link McRPG} instance with
 * some common things that are used across many tests in order
 * to streamline test development.
 */
public class McRPGMockExtension implements BeforeAllCallback, AfterAllCallback {

    public static final McRPG mcRPG = mock(McRPG.class);
    private static MockedStatic<McRPG> staticMcRPG;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (staticMcRPG == null) {
            Logger logger = Logger.getLogger("McRPG Test");
            staticMcRPG = mockStatic(McRPG.class);
            staticMcRPG.when(McRPG::getInstance).thenReturn(mcRPG);
            doReturn("McRPG").when(mcRPG).getName();
            doReturn("mcrpg").when(mcRPG).namespace();
            doReturn(MiniMessage.miniMessage()).when(mcRPG).getMiniMessage();
            doReturn(logger).when(mcRPG).getLogger();
            doReturn(RegistryAccess.registryAccess()).when(mcRPG).registryAccess();
            try {
                RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mock(FileManager.class));
                RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mock(LocalizationManager.class));
            }
            catch (IllegalArgumentException e) {
            /*
            Silent fail since multiple extensions might use this, and
            we want to ensure the same contract of what is and isn't set
            up no matter what extensions are used.
             */
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (staticMcRPG != null) {
            staticMcRPG.close();
            staticMcRPG = null;
        }
    }
}
