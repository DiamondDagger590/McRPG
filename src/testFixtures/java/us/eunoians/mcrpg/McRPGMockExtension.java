package us.eunoians.mcrpg;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;

import java.util.logging.Logger;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * This fixture sets up a {@link McRPG} instance with
 * some common things that are used across many tests in order
 * to streamline test development.
 */
public class McRPGMockExtension extends MockBukkitExtension implements BeforeAllCallback, AfterAllCallback {

    public static final McRPG mcRPG = mock(McRPG.class);
    private static MockedStatic<McRPG> staticMcRPG;

    @Override
    public void beforeAll(ExtensionContext context) {
        super.beforeAll(context);
        if (staticMcRPG == null) {
            Logger logger = Logger.getLogger("McRPG Test");
            staticMcRPG = mockStatic(McRPG.class);
            staticMcRPG.when(McRPG::getInstance).thenReturn(mcRPG);
            doReturn("McRPG").when(mcRPG).getName();
            doReturn("mcrpg").when(mcRPG).namespace();
            MiniMessage miniMessage = spy(MiniMessage.miniMessage());
            doReturn(miniMessage).when(mcRPG).getMiniMessage();
            doReturn(logger).when(mcRPG).getLogger();

            PluginDescriptionFile pluginDescriptionFile = mock(PluginDescriptionFile.class);
            when(mcRPG.getDescription()).thenReturn(pluginDescriptionFile);
            doReturn("McRPG").when(pluginDescriptionFile).getName();

            BukkitAudiences bukkitAudiences = spy(BukkitAudiences.create(mcRPG));

            doReturn(bukkitAudiences).when(mcRPG.getAdventure());
            doReturn(RegistryAccess.registryAccess()).when(mcRPG).registryAccess();
            try {
                RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mock(FileManager.class));
                RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(spy(new McRPGLocalizationManager(mcRPG)));
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
        super.afterAll(context);
        if (staticMcRPG != null) {
            staticMcRPG.close();
            staticMcRPG = null;
        }
    }
}
