package us.eunoians.mcrpg;

import com.diamonddagger590.mccore.bootstrap.CoreBootstrap;
import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;

import java.time.Clock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;


/**
 * This bootstrap is used to initialize and instance of McRPG for unit tests.
 * There are some things that aren't as easy to bootstrap such as databases,
 * nor do we want to bootstrap that for every unit test since this bootstrapping
 * happens PER test.
 * <p>
 * If, for example, we want to do database unit testing, we need to structure
 * a specific test setup around that requirement.
 */
public class TestBootstrap extends CoreBootstrap<McRPG> {

    public TestBootstrap() {
        /*
         We can mock here, the actual instance isn't actually used, and we don't have
         access to the initialized instance at this current time.
         */
        super(mock(McRPG.class));
    }

    @Override
    public void start(@NotNull StartupProfile startupProfile) {
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mock(FileManager.class));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mock(McRPGLocalizationManager.class));
    }

    @Override
    public void stop(@NotNull StartupProfile startupProfile) {

    }

    @NotNull
    @Override
    public TimeProvider getTimeProvider() {
        return spy(new TimeProvider(Clock.systemUTC()));
    }
}
