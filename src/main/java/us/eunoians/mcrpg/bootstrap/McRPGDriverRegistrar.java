package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.registrar.Registrar;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.driver.McRPGSqliteDriver;

/**
 * This registrar is in charge of registering database drivers
 * for McRPG.
 */
final class McRPGDriverRegistrar implements Registrar<McRPG> {

    @Override
    public void register(@NotNull BootstrapContext<McRPG> context) {
        context.plugin().registryAccess().registry(RegistryKey.DRIVER)
                .register(new McRPGSqliteDriver(context.plugin()));
    }
}
