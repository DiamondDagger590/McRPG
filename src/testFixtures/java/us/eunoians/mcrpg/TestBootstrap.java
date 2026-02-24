package us.eunoians.mcrpg;

import com.diamonddagger590.mccore.bootstrap.CoreBootstrap;
import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProviderRegistry;
import us.eunoians.mcrpg.quest.impl.scope.impl.PermissionQuestScopeProvider;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScopeProvider;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
        RegistryAccess registryAccess = RegistryAccess.registryAccess();
        registryAccess.registry(RegistryKey.MANAGER).register(mock(FileManager.class));
        registryAccess.registry(RegistryKey.MANAGER).register(mock(McRPGLocalizationManager.class));

        registryAccess.register(new QuestDefinitionRegistry());
        QuestScopeProviderRegistry scopeProviderRegistry = new QuestScopeProviderRegistry();
        registryAccess.register(scopeProviderRegistry);
        scopeProviderRegistry.register(new SinglePlayerQuestScopeProvider());
        scopeProviderRegistry.register(new PermissionQuestScopeProvider());
        registryAccess.register(new QuestObjectiveTypeRegistry());
        registryAccess.register(new QuestRewardTypeRegistry());
        registryAccess.registry(RegistryKey.MANAGER).register(mock(QuestManager.class));
    }

    @Override
    public void stop(@NotNull StartupProfile startupProfile) {

    }

    @NotNull
    @Override
    public TimeProvider getTimeProvider() {
        Instant instant = ZonedDateTime.of(2025, 12, 7, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        return spy(new TimeProvider(Clock.fixed(instant, ZoneId.of("UTC"))));
    }
}
