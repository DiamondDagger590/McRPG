package us.eunoians.mcrpg.ability.impl.type;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.diamonddagger590.mccore.registry.RegistryKey;
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
import us.eunoians.mcrpg.ability.attribute.AbilityCooldownAttribute;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
class CooldownableAbilityTest extends McRPGBaseTest {

    private StubCooldownableAbility ability;
    private AbilityHolder holder;
    private AbilityAttributeRegistry attributeRegistry;

    @BeforeEach
    void setUp() {
        attributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(attributeRegistry);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        ability = new StubCooldownableAbility(mcRPG, 10);
        abilityRegistry.register(ability);

        holder = new AbilityHolder(mcRPG, UUID.randomUUID());
    }

    @Nested
    @DisplayName("isAbilityOnCooldown")
    class IsAbilityOnCooldown {

        @Test
        @DisplayName("returns false when no cooldown attribute exists")
        void returnsFalse_whenNoCooldownAttribute() {
            assertFalse(ability.isAbilityOnCooldown(holder));
        }

        @Test
        @DisplayName("returns false when cooldown is 0")
        void returnsFalse_whenCooldownIsZero() {
            AbilityData data = new AbilityData(ability.getAbilityKey());
            data.addAttribute(new AbilityCooldownAttribute(0L));
            holder.addAbilityData(data);

            assertFalse(ability.isAbilityOnCooldown(holder));
        }

        @Test
        @DisplayName("returns false when cooldown has expired")
        void returnsFalse_whenCooldownExpired() {
            long pastTime = mcRPG.getTimeProvider().now().toEpochMilli() - 5000;
            AbilityData data = new AbilityData(ability.getAbilityKey());
            data.addAttribute(new AbilityCooldownAttribute(pastTime));
            holder.addAbilityData(data);

            assertFalse(ability.isAbilityOnCooldown(holder));
        }

        @Test
        @DisplayName("returns true when cooldown is still active")
        void returnsTrue_whenCooldownActive() {
            long futureTime = mcRPG.getTimeProvider().now().toEpochMilli() + 10000;
            AbilityData data = new AbilityData(ability.getAbilityKey());
            data.addAttribute(new AbilityCooldownAttribute(futureTime));
            holder.addAbilityData(data);

            assertTrue(ability.isAbilityOnCooldown(holder));
        }
    }

    @Nested
    @DisplayName("getCooldownForHolder")
    class GetCooldownForHolder {

        @Test
        @DisplayName("returns 0 when no ability data exists")
        void returnsZero_whenNoAbilityData() {
            assertEquals(0, ability.getCooldownForHolder(holder));
        }

        @Test
        @DisplayName("returns stored cooldown value")
        void returnsStoredCooldownValue() {
            long cooldownEnd = mcRPG.getTimeProvider().now().toEpochMilli() + 15000;
            AbilityData data = new AbilityData(ability.getAbilityKey());
            data.addAttribute(new AbilityCooldownAttribute(cooldownEnd));
            holder.addAbilityData(data);

            assertEquals(cooldownEnd, ability.getCooldownForHolder(holder));
        }

        @Test
        @DisplayName("returns 0 when cooldown attribute is default")
        void returnsZero_whenCooldownAttributeDefault() {
            AbilityData data = new AbilityData(ability.getAbilityKey());
            data.addAttribute(new AbilityCooldownAttribute(0L));
            holder.addAbilityData(data);

            assertEquals(0L, ability.getCooldownForHolder(holder));
        }
    }

    @Nested
    @DisplayName("putHolderOnCooldown")
    class PutHolderOnCooldown {

        @Test
        @DisplayName("applies cooldown even when ability data is auto-created")
        void appliesCooldown_whenAbilityDataAutoCreated() {
            long applied = ability.putHolderOnCooldown(holder, 10);
            assertEquals(10, applied);
            assertTrue(ability.isAbilityOnCooldown(holder));
        }

        @Test
        @DisplayName("stores cooldown as epoch millis plus duration in seconds")
        void storesCooldownAsEpochPlusDuration() {
            AbilityData data = new AbilityData(ability.getAbilityKey());
            holder.addAbilityData(data);

            long beforeCooldown = mcRPG.getTimeProvider().now().toEpochMilli();
            long appliedCooldown = ability.putHolderOnCooldown(holder, 10);

            assertEquals(10, appliedCooldown);
            long storedCooldown = ability.getCooldownForHolder(holder);
            assertEquals(beforeCooldown + 10000, storedCooldown);
        }

        @Test
        @DisplayName("puts holder on cooldown after application")
        void putsHolderOnCooldown_afterApplication() {
            AbilityData data = new AbilityData(ability.getAbilityKey());
            holder.addAbilityData(data);

            ability.putHolderOnCooldown(holder, 5);

            assertTrue(ability.isAbilityOnCooldown(holder));
        }

        @Test
        @DisplayName("uses getCooldown when called without duration")
        void useGetCooldown_whenCalledWithoutDuration() {
            ability.setCooldownDuration(15);
            AbilityData data = new AbilityData(ability.getAbilityKey());
            holder.addAbilityData(data);

            long appliedCooldown = ability.putHolderOnCooldown(holder);
            assertEquals(15, appliedCooldown);
        }
    }

    @Nested
    @DisplayName("getApplicableAttributes")
    class GetApplicableAttributes {

        @Test
        @DisplayName("includes cooldown attribute key")
        void includesCooldownAttributeKey() {
            Set<NamespacedKey> attributes = ability.getApplicableAttributes();
            assertTrue(attributes.contains(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY));
        }
    }

    @Nested
    @DisplayName("notifyCooldownActive")
    class NotifyCooldownActive {

        @Test
        @DisplayName("sends localized message to online player")
        void sendsLocalizedMessage_toOnlinePlayer(@NotNull McRPGPlayer mcRPGPlayer) {
            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
            Component stubComponent = Component.text("Ability is on cooldown");
            when(localizationManager.getLocalizedMessageAsComponent(any(McRPGPlayer.class), any(), anyMap()))
                    .thenReturn(stubComponent);

            PlayerMock playerMock = addPlayerToServer(mcRPGPlayer);

            ability.notifyCooldownActive(mcRPGPlayer);

            verify(localizationManager).getLocalizedMessageAsComponent(any(McRPGPlayer.class), any(), anyMap());
            playerMock.assertSaid(stubComponent);
        }

        @Test
        @DisplayName("does nothing when player is offline")
        void doesNothing_whenPlayerOffline(@NotNull McRPGPlayer mcRPGPlayer) {
            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

            ability.notifyCooldownActive(mcRPGPlayer);

            verify(localizationManager, never())
                    .getLocalizedMessageAsComponent(any(McRPGPlayer.class), any(), anyMap());
        }
    }

    private static final class StubCooldownableAbility extends BaseAbility implements CooldownableAbility {

        private long cooldownDuration;

        StubCooldownableAbility(@NotNull McRPG plugin, long cooldownDuration) {
            super(plugin, new NamespacedKey(plugin, "stub-cooldownable"));
            this.cooldownDuration = cooldownDuration;
        }

        void setCooldownDuration(long duration) {
            this.cooldownDuration = duration;
        }

        @Override
        public @NotNull Set<NamespacedKey> getApplicableAttributes() {
            return Set.of(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY);
        }

        @Override
        public long getCooldown(@NotNull AbilityHolder abilityHolder) {
            return cooldownDuration;
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
            return "stub_cooldownable";
        }

        @Override
        public @NotNull String getName(@NotNull McRPGPlayer player) {
            return "Stub Cooldownable";
        }

        @Override
        public @NotNull String getName() {
            return "Stub Cooldownable";
        }

        @Override
        public @NotNull Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text("Stub Cooldownable");
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.text("Stub Cooldownable");
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
