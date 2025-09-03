package us.eunoians.mcrpg.entity.player;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.spy;

/**
 * This fixture adds the {@link McRPGPlayerManager} to the {@link com.diamonddagger590.mccore.registry.manager.ManagerRegistry}
 * and before each unit test will create a new {@link McRPGPlayer} and after each test will remove the player from the player manager.
 * <p>
 * This player will be passed in as a fixture and can be used by having a {@link McRPGPlayer} parameter on a method.
 */
public class McRPGPlayerExtension implements BeforeEachCallback,
        AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(McRPGPlayerExtension.class);

    public record Fixture(@NotNull McRPGPlayer mcRPGPlayer) {
    }

    @Override
    public void beforeEach(@NotNull ExtensionContext context) {
        McRPGPlayerManager mcRPGPlayerManager = new McRPGPlayerManager(McRPG.getInstance());
        try {
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mcRPGPlayerManager);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException("Attempted to register an already registered manager. " +
                    "Ensure your class also uses the RegistryResetExtension before this one.",e);
        }
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        McRPGPlayer mcRPGPlayer = spy(new McRPGPlayer(UUID.randomUUID(), McRPG.getInstance()));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).addPlayer(mcRPGPlayer);
        store.put(Fixture.class, new Fixture(mcRPGPlayer));
    }

    @Override
    public void afterEach(@NotNull ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        Fixture fixture = store.remove(Fixture.class, Fixture.class);
        if (fixture != null) {
            try {
                RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.PLAYER)
                        .removePlayer(fixture.mcRPGPlayer().getUUID());
            }
            catch (Exception e) {
                /*
                Swallow in case the registry access has already been reset
                 */
            }
        }
    }

    @Override
    public boolean supportsParameter(@NotNull ParameterContext parameterContext, @NotNull ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> clazz = parameterContext.getParameter().getType();
        return clazz == Fixture.class || clazz == McRPGPlayer.class;
    }

    @Override
    public Object resolveParameter(@NotNull ParameterContext parameterContext, @NotNull ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Fixture fixture = extensionContext.getStore(NAMESPACE).get(Fixture.class, Fixture.class);
        Objects.requireNonNull(fixture, "PlayerFixtureExtension not initialized");
        if (parameterContext.getParameter().getType() == McRPGPlayer.class) {
            return fixture.mcRPGPlayer();
        }
        return fixture;
    }
}
