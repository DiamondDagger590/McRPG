package us.eunoians.mcrpg.event.ability.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.ability.impl.swords.DeeperWound;
import us.eunoians.mcrpg.ability.impl.swords.EnhancedBleed;
import us.eunoians.mcrpg.ability.impl.swords.RageSpike;
import us.eunoians.mcrpg.ability.impl.swords.Vampire;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for all swords ability event classes except {@link SerratedStrikesActivateEvent},
 * which has its own dedicated test class.
 * <p>
 * Covers: {@link BleedActivateEvent}, {@link BleedDamageEvent},
 * {@link DeeperWoundActivateEvent}, {@link EnhancedBleedActivateEvent},
 * {@link VampireActivateEvent}, {@link RageSpikeActivateEvent},
 * {@link RageSpikeDamageEvent}.
 */
class SwordsAbilityEventTest extends McRPGBaseTest {

    private AbilityHolder holder;
    private LivingEntity livingEntity;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        YamlDocument swordsConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);
        when(swordsConfig.getInt(SwordsConfigFile.DEEPER_WOUND_AMOUNT_OF_TIERS)).thenReturn(5);
        when(swordsConfig.getInt(SwordsConfigFile.ENHANCED_BLEED_AMOUNT_OF_TIERS)).thenReturn(5);
        when(swordsConfig.getInt(SwordsConfigFile.VAMPIRE_AMOUNT_OF_TIERS)).thenReturn(5);
        when(swordsConfig.getInt(SwordsConfigFile.RAGE_SPIKE_AMOUNT_OF_TIERS)).thenReturn(5);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        // Bleed must be registered first — other abilities' event classes have static initializers that resolve Bleed from the registry
        abilityRegistry.register(new Bleed(mcRPG));
        abilityRegistry.register(new DeeperWound(mcRPG));
        abilityRegistry.register(new EnhancedBleed(mcRPG));
        abilityRegistry.register(new Vampire(mcRPG));
        abilityRegistry.register(new RageSpike(mcRPG));

        holder = mock(AbilityHolder.class);
        when(holder.getUUID()).thenReturn(UUID.randomUUID());
        livingEntity = mock(LivingEntity.class);
    }

    @Nested
    @DisplayName("BleedActivateEvent")
    class BleedActivateEventTests {

        @Test
        @DisplayName("Constructor clamps bleedCycles to minimum 1")
        void constructor_clampsBleedCycles_whenNegative() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, -5, 3.0);
            assertEquals(1, event.getBleedCycles());
        }

        @Test
        @DisplayName("Constructor clamps bleedDamage to minimum 1")
        void constructor_clampsBleedDamage_whenNegative() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, 3, -2.0);
            assertEquals(1.0, event.getBleedDamage());
        }

        @Test
        @DisplayName("getBleedCycles returns positive constructor value")
        void getBleedCycles_returnsPositiveValue() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, 7, 3.0);
            assertEquals(7, event.getBleedCycles());
        }

        @Test
        @DisplayName("setBleedCycles updates to positive value")
        void setBleedCycles_updatesValue() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, 3, 3.0);
            event.setBleedCycles(10);
            assertEquals(10, event.getBleedCycles());
        }

        @Test
        @DisplayName("setBleedCycles clamps negative to 1")
        void setBleedCycles_clampsToOne_whenNegative() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, 3, 3.0);
            event.setBleedCycles(-4);
            assertEquals(1, event.getBleedCycles());
        }

        @Test
        @DisplayName("getBleedDamage returns positive constructor value")
        void getBleedDamage_returnsPositiveValue() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, 3, 5.5);
            assertEquals(5.5, event.getBleedDamage());
        }

        @Test
        @DisplayName("setBleedDamage updates to positive value")
        void setBleedDamage_updatesValue() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, 3, 3.0);
            event.setBleedDamage(8.0);
            assertEquals(8.0, event.getBleedDamage());
        }

        @Test
        @DisplayName("setBleedDamage clamps negative to 1")
        void setBleedDamage_clampsToOne_whenNegative() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, 3, 3.0);
            event.setBleedDamage(-1.0);
            assertEquals(1.0, event.getBleedDamage());
        }

        @Test
        @DisplayName("getBleedingEntity returns the entity")
        void getBleedingEntity_returnsEntity() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, 3, 3.0);
            assertEquals(livingEntity, event.getBleedingEntity());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, 3, 3.0);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, 3, 3.0);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns Bleed instance")
        void getAbility_returnsBleedInstance() {
            BleedActivateEvent event = new BleedActivateEvent(holder, livingEntity, 3, 3.0);
            assertInstanceOf(Bleed.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("BleedDamageEvent")
    class BleedDamageEventTests {

        @Test
        @DisplayName("Constructor clamps damage to minimum 0")
        void constructor_clampsDamage_whenNegative() {
            Entity entity = mock(Entity.class);
            BleedDamageEvent event = new BleedDamageEvent(entity, -5.0, false);
            assertEquals(0.0, event.getDamage());
        }

        @Test
        @DisplayName("setDamage clamps negative to 0")
        void setDamage_clampsToZero_whenNegative() {
            Entity entity = mock(Entity.class);
            BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
            event.setDamage(-3.0);
            assertEquals(0.0, event.getDamage());
        }

        @Test
        @DisplayName("getDamagedEntity returns the entity (entity-only constructor)")
        void getDamagedEntity_returnsEntity_entityOnlyConstructor() {
            Entity entity = mock(Entity.class);
            BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
            assertEquals(entity, event.getDamagedEntity());
        }

        @Test
        @DisplayName("isDamageIgnoringArmor returns constructor value")
        void isDamageIgnoringArmor_returnsConstructorValue() {
            Entity entity = mock(Entity.class);
            BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, true);
            assertTrue(event.isDamageIgnoringArmor());
        }

        @Test
        @DisplayName("setDamageIgnoreArmor updates the flag")
        void setDamageIgnoreArmor_updatesFlag() {
            Entity entity = mock(Entity.class);
            BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
            event.setDamageIgnoreArmor(true);
            assertTrue(event.isDamageIgnoringArmor());
        }

        @Test
        @DisplayName("getBleedUser returns empty Optional for entity-only constructor")
        void getBleedUser_returnsEmpty_entityOnlyConstructor() {
            Entity entity = mock(Entity.class);
            BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
            assertTrue(event.getBleedUser().isEmpty());
        }

        @Test
        @DisplayName("getBleedUser returns present Optional for AbilityHolder constructor")
        void getBleedUser_returnsPresent_abilityHolderConstructor() {
            Entity entity = mock(Entity.class);
            BleedDamageEvent event = new BleedDamageEvent(holder, entity, 5.0, false);
            assertTrue(event.getBleedUser().isPresent());
            assertEquals(holder, event.getBleedUser().orElseThrow());
        }

        @Test
        @DisplayName("getBleedUser returns provided Optional for Optional constructor")
        void getBleedUser_returnsProvidedOptional() {
            Entity entity = mock(Entity.class);
            Optional<AbilityHolder> optionalHolder = Optional.of(holder);
            BleedDamageEvent event = new BleedDamageEvent(optionalHolder, entity, 5.0, false);
            assertTrue(event.getBleedUser().isPresent());
            assertEquals(holder, event.getBleedUser().orElseThrow());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            Entity entity = mock(Entity.class);
            BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled() {
            Entity entity = mock(Entity.class);
            BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns Bleed instance")
        void getAbility_returnsBleedInstance() {
            Entity entity = mock(Entity.class);
            BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
            assertInstanceOf(Bleed.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("DeeperWoundActivateEvent")
    class DeeperWoundActivateEventTests {

        @Test
        @DisplayName("Constructor clamps additionalBleedCycles to minimum 0")
        void constructor_clampsAdditionalBleedCycles_whenNegative() {
            DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, livingEntity, -3);
            assertEquals(0, event.getAdditionalBleedCycles());
        }

        @Test
        @DisplayName("getAdditionalBleedCycles returns positive constructor value")
        void getAdditionalBleedCycles_returnsPositiveValue() {
            DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, livingEntity, 5);
            assertEquals(5, event.getAdditionalBleedCycles());
        }

        @Test
        @DisplayName("setAdditionalBleedCycles updates to positive value")
        void setAdditionalBleedCycles_updatesValue() {
            DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, livingEntity, 3);
            event.setAdditionalBleedCycles(8);
            assertEquals(8, event.getAdditionalBleedCycles());
        }

        @Test
        @DisplayName("setAdditionalBleedCycles clamps negative to 0")
        void setAdditionalBleedCycles_clampsToZero_whenNegative() {
            DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, livingEntity, 3);
            event.setAdditionalBleedCycles(-2);
            assertEquals(0, event.getAdditionalBleedCycles());
        }

        @Test
        @DisplayName("getBleedingEntity returns the entity")
        void getBleedingEntity_returnsEntity() {
            DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, livingEntity, 3);
            assertEquals(livingEntity, event.getBleedingEntity());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, livingEntity, 3);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled() {
            DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, livingEntity, 3);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns DeeperWound instance")
        void getAbility_returnsDeeperWoundInstance() {
            DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, livingEntity, 3);
            assertInstanceOf(DeeperWound.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("EnhancedBleedActivateEvent")
    class EnhancedBleedActivateEventTests {

        @Test
        @DisplayName("Constructor clamps additionalBleedDamage to minimum 0")
        void constructor_clampsAdditionalBleedDamage_whenNegative() {
            EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, livingEntity, -5.0);
            assertEquals(0.0, event.getAdditionalBleedDamage());
        }

        @Test
        @DisplayName("getAdditionalBleedDamage returns positive constructor value")
        void getAdditionalBleedDamage_returnsPositiveValue() {
            EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, livingEntity, 4.5);
            assertEquals(4.5, event.getAdditionalBleedDamage());
        }

        @Test
        @DisplayName("setAdditionalBleedDamage updates to positive value")
        void setAdditionalBleedDamage_updatesValue() {
            EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, livingEntity, 3.0);
            event.setAdditionalBleedDamage(7.5);
            assertEquals(7.5, event.getAdditionalBleedDamage());
        }

        @Test
        @DisplayName("setAdditionalBleedDamage clamps negative to 0")
        void setAdditionalBleedDamage_clampsToZero_whenNegative() {
            EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, livingEntity, 3.0);
            event.setAdditionalBleedDamage(-1.0);
            assertEquals(0.0, event.getAdditionalBleedDamage());
        }

        @Test
        @DisplayName("getBleedingEntity returns the entity")
        void getBleedingEntity_returnsEntity() {
            EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, livingEntity, 3.0);
            assertEquals(livingEntity, event.getBleedingEntity());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, livingEntity, 3.0);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled() {
            EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, livingEntity, 3.0);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns EnhancedBleed instance")
        void getAbility_returnsEnhancedBleedInstance() {
            EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, livingEntity, 3.0);
            assertInstanceOf(EnhancedBleed.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("VampireActivateEvent")
    class VampireActivateEventTests {

        @Test
        @DisplayName("Constructor clamps amountToHeal to minimum 0")
        void constructor_clampsAmountToHeal_whenNegative() {
            VampireActivateEvent event = new VampireActivateEvent(holder, livingEntity, -3.0);
            assertEquals(0.0, event.getAmountToHeal());
        }

        @Test
        @DisplayName("getAmountToHeal returns positive constructor value")
        void getAmountToHeal_returnsPositiveValue() {
            VampireActivateEvent event = new VampireActivateEvent(holder, livingEntity, 6.0);
            assertEquals(6.0, event.getAmountToHeal());
        }

        @Test
        @DisplayName("setAmountToHeal updates to positive value")
        void setAmountToHeal_updatesValue() {
            VampireActivateEvent event = new VampireActivateEvent(holder, livingEntity, 3.0);
            event.setAmountToHeal(10.0);
            assertEquals(10.0, event.getAmountToHeal());
        }

        @Test
        @DisplayName("setAmountToHeal clamps negative to 0")
        void setAmountToHeal_clampsToZero_whenNegative() {
            VampireActivateEvent event = new VampireActivateEvent(holder, livingEntity, 3.0);
            event.setAmountToHeal(-5.0);
            assertEquals(0.0, event.getAmountToHeal());
        }

        @Test
        @DisplayName("getBleedingEntity returns the entity")
        void getBleedingEntity_returnsEntity() {
            VampireActivateEvent event = new VampireActivateEvent(holder, livingEntity, 3.0);
            assertEquals(livingEntity, event.getBleedingEntity());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            VampireActivateEvent event = new VampireActivateEvent(holder, livingEntity, 3.0);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled() {
            VampireActivateEvent event = new VampireActivateEvent(holder, livingEntity, 3.0);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns Vampire instance")
        void getAbility_returnsVampireInstance() {
            VampireActivateEvent event = new VampireActivateEvent(holder, livingEntity, 3.0);
            assertInstanceOf(Vampire.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("RageSpikeActivateEvent")
    class RageSpikeActivateEventTests {

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            RageSpikeActivateEvent event = new RageSpikeActivateEvent(holder);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled() {
            RageSpikeActivateEvent event = new RageSpikeActivateEvent(holder);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns RageSpike instance")
        void getAbility_returnsRageSpikeInstance() {
            RageSpikeActivateEvent event = new RageSpikeActivateEvent(holder);
            assertInstanceOf(RageSpike.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("RageSpikeDamageEvent")
    class RageSpikeDamageEventTests {

        @Test
        @DisplayName("Constructor clamps damage to minimum 0")
        void constructor_clampsDamage_whenNegative() {
            RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, livingEntity, -5.0);
            assertEquals(0.0, event.getDamage());
        }

        @Test
        @DisplayName("getDamage returns positive constructor value")
        void getDamage_returnsPositiveValue() {
            RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, livingEntity, 7.5);
            assertEquals(7.5, event.getDamage());
        }

        @Test
        @DisplayName("setDamage updates to positive value")
        void setDamage_updatesValue() {
            RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, livingEntity, 3.0);
            event.setDamage(12.0);
            assertEquals(12.0, event.getDamage());
        }

        @Test
        @DisplayName("setDamage clamps negative to 0")
        void setDamage_clampsToZero_whenNegative() {
            RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, livingEntity, 3.0);
            event.setDamage(-1.0);
            assertEquals(0.0, event.getDamage());
        }

        @Test
        @DisplayName("getDamagedEntity returns the entity")
        void getDamagedEntity_returnsEntity() {
            RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, livingEntity, 3.0);
            assertEquals(livingEntity, event.getDamagedEntity());
        }

        @Test
        @DisplayName("getAbilityHolder returns the holder")
        void getAbilityHolder_returnsHolder() {
            RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, livingEntity, 3.0);
            assertEquals(holder, event.getAbilityHolder());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, livingEntity, 3.0);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled() {
            RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, livingEntity, 3.0);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns RageSpike instance")
        void getAbility_returnsRageSpikeInstance() {
            RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, livingEntity, 3.0);
            assertInstanceOf(RageSpike.class, event.getAbility());
        }
    }
}
