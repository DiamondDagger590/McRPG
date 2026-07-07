package us.eunoians.mcrpg.entity.holder;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.MockAbility;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.event.ability.AbilityCooldownExpireEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(McRPGPlayerExtension.class)
class AbilityHolderTest extends McRPGBaseTest {

    private AbilityHolder holder;
    private AbilityRegistry abilityRegistry;
    private MockAbility mockAbility;
    private NamespacedKey abilityKey;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry attributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(attributeRegistry);

        abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        mockAbility = new MockAbility(mcRPG);
        abilityKey = mockAbility.getAbilityKey();
        abilityRegistry.register(mockAbility);

        holder = new AbilityHolder(mcRPG, UUID.randomUUID());
    }

    @Nested
    @DisplayName("Available abilities")
    class AvailableAbilities {

        @Test
        @DisplayName("getAvailableAbilities returns empty set initially")
        void getAvailableAbilities_returnsEmptySet_initially() {
            assertTrue(holder.getAvailableAbilities().isEmpty());
        }

        @Test
        @DisplayName("addAvailableAbility by key makes ability available")
        void addAvailableAbility_byKey_makesAbilityAvailable() {
            holder.addAvailableAbility(abilityKey);
            assertTrue(holder.isAbilityAvailable(abilityKey));
        }

        @Test
        @DisplayName("addAvailableAbility by ability makes ability available")
        void addAvailableAbility_byAbility_makesAbilityAvailable() {
            holder.addAvailableAbility(mockAbility);
            assertTrue(holder.isAbilityAvailable(mockAbility));
        }

        @Test
        @DisplayName("isAbilityAvailable returns false for unregistered ability")
        void isAbilityAvailable_returnsFalse_whenNotAdded() {
            assertFalse(holder.isAbilityAvailable(abilityKey));
        }

        @Test
        @DisplayName("removeAvailableAbility by key removes ability")
        void removeAvailableAbility_byKey_removesAbility() {
            holder.addAvailableAbility(abilityKey);
            holder.removeAvailableAbility(abilityKey);
            assertFalse(holder.isAbilityAvailable(abilityKey));
        }

        @Test
        @DisplayName("removeAvailableAbility by ability removes ability")
        void removeAvailableAbility_byAbility_removesAbility() {
            holder.addAvailableAbility(mockAbility);
            holder.removeAvailableAbility(mockAbility);
            assertFalse(holder.isAbilityAvailable(mockAbility));
        }

        @Test
        @DisplayName("getAvailableAbilities returns immutable copy")
        void getAvailableAbilities_returnsImmutableCopy() {
            holder.addAvailableAbility(abilityKey);
            Set<NamespacedKey> snapshot = holder.getAvailableAbilities();
            assertEquals(1, snapshot.size());
            assertTrue(snapshot.contains(abilityKey));
        }

        @Test
        @DisplayName("addAvailableAbility ignores unregistered ability key")
        void addAvailableAbility_ignoresUnregisteredKey() {
            NamespacedKey unknownKey = new NamespacedKey(mcRPG, "unknown-ability");
            holder.addAvailableAbility(unknownKey);
            assertFalse(holder.isAbilityAvailable(unknownKey));
            assertTrue(holder.getAvailableAbilities().isEmpty());
        }
    }

    @Nested
    @DisplayName("Ability data")
    class AbilityDataTests {

        @Test
        @DisplayName("hasAbilityData returns false initially")
        void hasAbilityData_returnsFalse_initially() {
            assertFalse(holder.hasAbilityData(abilityKey));
        }

        @Test
        @DisplayName("addAbilityData makes data retrievable")
        void addAbilityData_makesDataRetrievable() {
            AbilityData data = new AbilityData(abilityKey);
            holder.addAbilityData(data);

            assertTrue(holder.hasAbilityData(abilityKey));
            Optional<AbilityData> result = holder.getAbilityData(abilityKey);
            assertTrue(result.isPresent());
            assertSame(data, result.get());
        }

        @Test
        @DisplayName("hasAbilityData by ability delegates correctly")
        void hasAbilityData_byAbility_delegatesCorrectly() {
            assertFalse(holder.hasAbilityData(mockAbility));

            AbilityData data = new AbilityData(abilityKey);
            holder.addAbilityData(data);
            assertTrue(holder.hasAbilityData(mockAbility));
        }

        @Test
        @DisplayName("getAbilityData by ability delegates correctly")
        void getAbilityData_byAbility_delegatesCorrectly() {
            AbilityData data = new AbilityData(abilityKey);
            holder.addAbilityData(data);

            Optional<AbilityData> result = holder.getAbilityData(mockAbility);
            assertTrue(result.isPresent());
            assertSame(data, result.get());
        }

        @Test
        @DisplayName("removeAbilityData by key removes data")
        void removeAbilityData_byKey_removesData() {
            AbilityData data = new AbilityData(abilityKey);
            holder.addAbilityData(data);
            holder.removeAbilityData(abilityKey);

            assertFalse(holder.hasAbilityData(abilityKey));
        }

        @Test
        @DisplayName("removeAbilityData by ability removes data")
        void removeAbilityData_byAbility_removesData() {
            AbilityData data = new AbilityData(abilityKey);
            holder.addAbilityData(data);
            holder.removeAbilityData(mockAbility);

            assertFalse(holder.hasAbilityData(mockAbility));
        }

        @Test
        @DisplayName("addAbilityData ignores unregistered ability key")
        void addAbilityData_ignoresUnregisteredKey() {
            NamespacedKey unknownKey = new NamespacedKey(mcRPG, "unknown-ability");
            AbilityData data = new AbilityData(unknownKey);
            holder.addAbilityData(data);

            assertFalse(holder.hasAbilityData(unknownKey));
        }

        @Test
        @DisplayName("getAbilityData returns empty for unregistered key")
        void getAbilityData_returnsEmpty_forUnregisteredKey() {
            NamespacedKey unknownKey = new NamespacedKey(mcRPG, "unknown-ability");
            assertTrue(holder.getAbilityData(unknownKey).isEmpty());
        }

        @Test
        @DisplayName("getAbilityData creates default data when none stored")
        void getAbilityData_createsDefaultData_whenNoneStored() {
            Optional<AbilityData> result = holder.getAbilityData(abilityKey);
            assertTrue(result.isPresent());
            assertEquals(abilityKey, result.get().getAbilityKey());
            assertTrue(holder.hasAbilityData(abilityKey));
        }
    }

    @Nested
    @DisplayName("Active abilities")
    class ActiveAbilities {

        @Test
        @DisplayName("isAbilityActive returns false initially")
        void isAbilityActive_returnsFalse_initially() {
            assertFalse(holder.isAbilityActive(abilityKey));
        }

        @Test
        @DisplayName("addActiveAbility by key makes ability active")
        void addActiveAbility_byKey_makesAbilityActive() {
            holder.addActiveAbility(abilityKey);
            assertTrue(holder.isAbilityActive(abilityKey));
        }

        @Test
        @DisplayName("addActiveAbility by ability makes ability active")
        void addActiveAbility_byAbility_makesAbilityActive() {
            holder.addActiveAbility(mockAbility);
            assertTrue(holder.isAbilityActive(mockAbility));
        }

        @Test
        @DisplayName("addActiveAbility with duration makes ability active immediately")
        void addActiveAbility_withDuration_makesAbilityActiveImmediately() {
            holder.addActiveAbility(abilityKey, 5);
            assertTrue(holder.isAbilityActive(abilityKey));
        }

        @Test
        @DisplayName("addActiveAbility with duration auto-removes after elapsed time")
        void addActiveAbility_withDuration_autoRemovesAfterElapsedTime() {
            holder.addActiveAbility(abilityKey, 5);
            assertTrue(holder.isAbilityActive(abilityKey));

            server.getScheduler().performTicks(5 * 20L);

            assertFalse(holder.isAbilityActive(abilityKey));
        }

        @Test
        @DisplayName("addActiveAbility by ability with duration auto-removes after elapsed time")
        void addActiveAbility_byAbility_withDuration_autoRemovesAfterElapsedTime() {
            holder.addActiveAbility(mockAbility, 3);
            assertTrue(holder.isAbilityActive(mockAbility));

            server.getScheduler().performTicks(3 * 20L);

            assertFalse(holder.isAbilityActive(mockAbility));
        }

        @Test
        @DisplayName("addActiveAbility with duration stays active before time elapses")
        void addActiveAbility_withDuration_staysActiveBeforeTimeElapses() {
            holder.addActiveAbility(abilityKey, 5);

            server.getScheduler().performTicks(4 * 20L);

            assertTrue(holder.isAbilityActive(abilityKey));
        }

        @Test
        @DisplayName("removeActiveAbility by key makes ability inactive")
        void removeActiveAbility_byKey_makesAbilityInactive() {
            holder.addActiveAbility(abilityKey);
            holder.removeActiveAbility(abilityKey);
            assertFalse(holder.isAbilityActive(abilityKey));
        }

        @Test
        @DisplayName("removeActiveAbility by ability makes ability inactive")
        void removeActiveAbility_byAbility_makesAbilityInactive() {
            holder.addActiveAbility(mockAbility);
            holder.removeActiveAbility(mockAbility);
            assertFalse(holder.isAbilityActive(mockAbility));
        }

        @Test
        @DisplayName("getCurrentlyActiveAbilities returns immutable copy")
        void getCurrentlyActiveAbilities_returnsImmutableCopy() {
            holder.addActiveAbility(abilityKey);
            Set<NamespacedKey> active = holder.getCurrentlyActiveAbilities();
            assertEquals(1, active.size());
            assertTrue(active.contains(abilityKey));
        }

        @Test
        @DisplayName("getCurrentlyActiveAbilities returns empty set initially")
        void getCurrentlyActiveAbilities_returnsEmptySet_initially() {
            assertTrue(holder.getCurrentlyActiveAbilities().isEmpty());
        }
    }

    @Nested
    @DisplayName("Equality")
    class EqualityTests {

        @Test
        @DisplayName("equals returns true for holders with same UUID")
        void equals_returnsTrue_forSameUuid() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder holder1 = new AbilityHolder(mcRPG, uuid);
            AbilityHolder holder2 = new AbilityHolder(mcRPG, uuid);
            assertEquals(holder1, holder2);
        }

        @Test
        @DisplayName("equals returns false for holders with different UUID")
        void equals_returnsFalse_forDifferentUuid() {
            AbilityHolder holder1 = new AbilityHolder(mcRPG, UUID.randomUUID());
            AbilityHolder holder2 = new AbilityHolder(mcRPG, UUID.randomUUID());
            assertFalse(holder1.equals(holder2));
        }

        @Test
        @DisplayName("equals returns false for non-AbilityHolder")
        void equals_returnsFalse_forNonAbilityHolder() {
            assertFalse(holder.equals("not a holder"));
        }
    }

    @Nested
    @DisplayName("UUID")
    class UuidTests {

        @Test
        @DisplayName("getUUID returns the UUID passed in constructor")
        void getUUID_returnsConstructorUuid() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder uuidHolder = new AbilityHolder(mcRPG, uuid);
            assertEquals(uuid, uuidHolder.getUUID());
        }
    }

    @Nested
    @DisplayName("getPlugin")
    class GetPlugin {

        @Test
        @DisplayName("returns the plugin passed in constructor")
        void returnsPlugin() {
            assertSame(mcRPG, holder.getPlugin());
        }
    }

    @Nested
    @DisplayName("Cooldown expire notification timer")
    class CooldownExpireNotificationTimer {

        @BeforeEach
        void clearSchedulerAndEventHistory() {
            server.getScheduler().cancelTasks(mcRPG);
            server.getPluginManager().clearEvents();
        }

        @Test
        @DisplayName("fires AbilityCooldownExpireEvent after cooldown elapses for online player")
        void firesEvent_afterCooldownElapses(@NotNull McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);
            AbilityHolder playerHolder = mcRPGPlayer.asSkillHolder();

            playerHolder.startCooldownExpireNotificationTimer(mockAbility, 3);
            server.getScheduler().performTicks(3 * 20L);

            server.getPluginManager().assertEventFired(AbilityCooldownExpireEvent.class);
        }

        @Test
        @DisplayName("does not fire event before cooldown elapses")
        void doesNotFireEvent_beforeCooldownElapses(@NotNull McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);
            AbilityHolder playerHolder = mcRPGPlayer.asSkillHolder();

            playerHolder.startCooldownExpireNotificationTimer(mockAbility, 5);
            server.getScheduler().performTicks(4 * 20L);

            server.getPluginManager().assertEventNotFired(AbilityCooldownExpireEvent.class);
        }

        @Test
        @DisplayName("does not schedule timer for non-player entity")
        void doesNotScheduleTimer_forNonPlayerEntity() {
            holder.startCooldownExpireNotificationTimer(mockAbility, 3);
            server.getScheduler().performTicks(3 * 20L);

            server.getPluginManager().assertEventNotFired(AbilityCooldownExpireEvent.class);
        }

        @Test
        @DisplayName("removeCooldownExpireNotificationTimer prevents event from firing")
        void removeCooldownExpireNotificationTimer_preventsEvent(@NotNull McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);
            AbilityHolder playerHolder = mcRPGPlayer.asSkillHolder();

            playerHolder.startCooldownExpireNotificationTimer(mockAbility, 5);
            playerHolder.removeCooldownExpireNotificationTimer(mockAbility);
            server.getScheduler().performTicks(5 * 20L);

            server.getPluginManager().assertEventNotFired(AbilityCooldownExpireEvent.class);
        }

        @Test
        @DisplayName("removeCooldownExpireNotificationTimer is safe when no timer exists")
        void removeCooldownExpireNotificationTimer_safeWhenNoTimer() {
            holder.removeCooldownExpireNotificationTimer(mockAbility);
            holder.removeCooldownExpireNotificationTimer(abilityKey);
        }

        @Test
        @DisplayName("starting new timer replaces existing timer")
        void startingNewTimer_replacesExistingTimer(@NotNull McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);
            AbilityHolder playerHolder = mcRPGPlayer.asSkillHolder();

            playerHolder.startCooldownExpireNotificationTimer(abilityKey, 3);
            playerHolder.startCooldownExpireNotificationTimer(abilityKey, 10);

            server.getScheduler().performTicks(3 * 20L);

            server.getPluginManager().assertEventNotFired(AbilityCooldownExpireEvent.class);

            server.getScheduler().performTicks(7 * 20L);

            server.getPluginManager().assertEventFired(AbilityCooldownExpireEvent.class);
        }
    }

    @Nested
    @DisplayName("getAllAbilityDataForSkill")
    class GetAllAbilityDataForSkill {

        private StubSkillAbility skillAbility;
        private NamespacedKey skillKey;

        @BeforeEach
        void setUpSkillAbility() {
            skillKey = new NamespacedKey(mcRPG, "test-skill");
            skillAbility = new StubSkillAbility(mcRPG, skillKey);
            abilityRegistry.register(skillAbility);
        }

        @Test
        @DisplayName("returns empty set when no abilities belong to skill")
        void returnsEmptySet_whenNoAbilitiesBelongToSkill() {
            NamespacedKey otherSkillKey = new NamespacedKey(mcRPG, "other-skill");
            Set<AbilityData> result = holder.getAllAbilityDataForSkill(otherSkillKey);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns ability data for abilities belonging to skill")
        void returnsAbilityData_forAbilitiesBelongingToSkill() {
            AbilityData data = new AbilityData(skillAbility.getAbilityKey());
            holder.addAbilityData(data);

            Set<AbilityData> result = holder.getAllAbilityDataForSkill(skillKey);
            assertEquals(1, result.size());
            assertTrue(result.contains(data));
        }

        @Test
        @DisplayName("auto-creates ability data for skill abilities without stored data")
        void autoCreatesAbilityData_forSkillAbilitiesWithoutStoredData() {
            Set<AbilityData> result = holder.getAllAbilityDataForSkill(skillKey);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("does not include non-skill abilities")
        void doesNotIncludeNonSkillAbilities() {
            AbilityData mockData = new AbilityData(abilityKey);
            holder.addAbilityData(mockData);

            Set<AbilityData> result = holder.getAllAbilityDataForSkill(skillKey);
            assertFalse(result.stream().anyMatch(d -> d.getAbilityKey().equals(abilityKey)));
        }
    }

    @Nested
    @DisplayName("Cleanup")
    class Cleanup {

        @BeforeEach
        void clearSchedulerAndEventHistory() {
            server.getScheduler().cancelTasks(mcRPG);
            server.getPluginManager().clearEvents();
        }

        @Test
        @DisplayName("cleanupHolder clears active abilities")
        void cleanupHolder_clearsActiveAbilities() {
            holder.addActiveAbility(abilityKey);
            assertTrue(holder.isAbilityActive(abilityKey));

            holder.cleanupHolder();
            assertFalse(holder.isAbilityActive(abilityKey));
            assertTrue(holder.getCurrentlyActiveAbilities().isEmpty());
        }

        @Test
        @DisplayName("cleanupHolder cancels cooldown expire timers")
        void cleanupHolder_cancelsCooldownExpireTimers(@NotNull McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);
            AbilityHolder playerHolder = mcRPGPlayer.asSkillHolder();

            playerHolder.startCooldownExpireNotificationTimer(mockAbility, 5);
            playerHolder.cleanupHolder();

            server.getScheduler().performTicks(5 * 20L);

            server.getPluginManager().assertEventNotFired(AbilityCooldownExpireEvent.class);
        }
    }

    private static final class StubSkillAbility extends BaseAbility implements SkillAbility {

        private final NamespacedKey skillKey;

        StubSkillAbility(@NotNull McRPG plugin, @NotNull NamespacedKey skillKey) {
            super(plugin, new NamespacedKey(plugin, "stub-skill-ability"));
            this.skillKey = skillKey;
        }

        @Override
        public @NotNull NamespacedKey getSkillKey() {
            return skillKey;
        }

        @Override
        public boolean activateAbility(@NotNull AbilityHolder holder, @NotNull Event event) {
            return true;
        }

        @Override
        public boolean isAbilityEnabled() {
            return true;
        }

        @Override
        public boolean isPassive() {
            return false;
        }

        @Override
        public @NotNull String getDatabaseName() {
            return "stub_skill_ability";
        }

        @Override
        public @NotNull String getName(@NotNull McRPGPlayer player) {
            return "Stub Skill Ability";
        }

        @Override
        public @NotNull String getName() {
            return "Stub Skill Ability";
        }

        @Override
        public @NotNull Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text("Stub Skill Ability");
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.text("Stub Skill Ability");
        }

        @Override
        public @NotNull AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            return mock(AbilityItemBuilder.class);
        }

        @Override
        public @NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}
