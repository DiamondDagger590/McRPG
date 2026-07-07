package us.eunoians.mcrpg.ability.impl.swords.bleed;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BleedManagerTest extends McRPGBaseTest {

    private BleedManager bleedManager;
    private YamlDocument swordsConfig;

    @BeforeEach
    void setUp() {
        swordsConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        when(swordsConfig.getInt(SwordsConfigFile.BLEED_BASE_CYCLES)).thenReturn(3);
        when(swordsConfig.getDouble(SwordsConfigFile.BLEED_BASE_DAMAGE)).thenReturn(1.0);
        when(swordsConfig.getDouble(SwordsConfigFile.BLEED_BASE_FREQUENCY)).thenReturn(1.0);
        when(swordsConfig.getBoolean(SwordsConfigFile.BLEED_GRANT_IMMUNITY_AFTER_EXPIRE)).thenReturn(false);
        when(swordsConfig.getInt(SwordsConfigFile.BLEED_IMMUNITY_DURATION)).thenReturn(5);
        when(swordsConfig.getInt(SwordsConfigFile.BLEED_MINIMUM_HEALTH_ALLOWED)).thenReturn(1);
        when(swordsConfig.getBoolean(SwordsConfigFile.BLEED_DAMAGE_PIERCE_ARMOR)).thenReturn(false);

        bleedManager = new BleedManager(mcRPG);
    }

    @Nested
    @DisplayName("isEntityBleeding")
    class IsEntityBleeding {

        @DisplayName("returns false for unknown UUID")
        @Test
        void isEntityBleeding_returnsFalse_forUnknownUuid() {
            assertFalse(bleedManager.isEntityBleeding(UUID.randomUUID()));
        }

        @DisplayName("returns false for unknown entity")
        @Test
        void isEntityBleeding_returnsFalse_forUnknownEntity() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            assertFalse(bleedManager.isEntityBleeding(entity));
        }

        @DisplayName("returns true after startBleeding")
        @Test
        void isEntityBleeding_returnsTrue_afterStartBleeding() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            bleedManager.startBleeding(entity, 3, 1.0);

            assertTrue(bleedManager.isEntityBleeding(entity));
        }

        @DisplayName("UUID overload returns true after startBleeding")
        @Test
        void isEntityBleeding_uuid_returnsTrue_afterStartBleeding() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            bleedManager.startBleeding(entity, 3, 1.0);

            assertTrue(bleedManager.isEntityBleeding(entity.getUniqueId()));
        }
    }

    @Nested
    @DisplayName("stopEntityBleeding")
    class StopEntityBleeding {

        @DisplayName("removes entity from bleeding state")
        @Test
        void stopEntityBleeding_removesBleeding() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            bleedManager.startBleeding(entity, 3, 1.0);
            assertTrue(bleedManager.isEntityBleeding(entity));

            bleedManager.stopEntityBleeding(entity);

            assertFalse(bleedManager.isEntityBleeding(entity));
        }

        @DisplayName("UUID overload removes bleeding state")
        @Test
        void stopEntityBleeding_uuid_removesBleeding() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            bleedManager.startBleeding(entity, 3, 1.0);

            bleedManager.stopEntityBleeding(entity.getUniqueId());

            assertFalse(bleedManager.isEntityBleeding(entity.getUniqueId()));
        }

        @DisplayName("no-op for non-bleeding entity")
        @Test
        void stopEntityBleeding_noOp_forNonBleedingEntity() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            bleedManager.stopEntityBleeding(entity);

            assertFalse(bleedManager.isEntityBleeding(entity));
        }
    }

    @Nested
    @DisplayName("canEntityStartBleeding")
    class CanEntityStartBleeding {

        @DisplayName("returns true for fresh entity")
        @Test
        void canEntityStartBleeding_returnsTrue_forFreshEntity() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            assertTrue(bleedManager.canEntityStartBleeding(entity));
        }

        @DisplayName("UUID overload returns true for fresh UUID")
        @Test
        void canEntityStartBleeding_uuid_returnsTrue_forFreshUuid() {
            assertTrue(bleedManager.canEntityStartBleeding(UUID.randomUUID()));
        }

        @DisplayName("returns false when entity is bleeding")
        @Test
        void canEntityStartBleeding_returnsFalse_whenBleeding() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            bleedManager.startBleeding(entity, 3, 1.0);

            assertFalse(bleedManager.canEntityStartBleeding(entity));
        }

        @DisplayName("returns false when entity is bleed immune")
        @Test
        void canEntityStartBleeding_returnsFalse_whenBleedImmune() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            bleedManager.startBleedImmunity(entity);

            assertFalse(bleedManager.canEntityStartBleeding(entity));
        }

        @DisplayName("returns false when entity is both bleeding and immune")
        @Test
        void canEntityStartBleeding_returnsFalse_whenBleedingAndImmune() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            bleedManager.startBleeding(entity, 3, 1.0);
            bleedManager.startBleedImmunity(entity);

            assertFalse(bleedManager.canEntityStartBleeding(entity));
        }
    }

    @Nested
    @DisplayName("isEntityBleedImmune")
    class IsEntityBleedImmune {

        @DisplayName("returns false for fresh entity")
        @Test
        void isEntityBleedImmune_returnsFalse_forFreshEntity() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            assertFalse(bleedManager.isEntityBleedImmune(entity));
        }

        @DisplayName("UUID overload returns false for unknown UUID")
        @Test
        void isEntityBleedImmune_uuid_returnsFalse_forUnknownUuid() {
            assertFalse(bleedManager.isEntityBleedImmune(UUID.randomUUID()));
        }

        @DisplayName("returns true after startBleedImmunity")
        @Test
        void isEntityBleedImmune_returnsTrue_afterStartImmunity() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            bleedManager.startBleedImmunity(entity);

            assertTrue(bleedManager.isEntityBleedImmune(entity));
        }

        @DisplayName("UUID overload returns true after startBleedImmunity")
        @Test
        void isEntityBleedImmune_uuid_returnsTrue_afterStartImmunity() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            bleedManager.startBleedImmunity(entity.getUniqueId());

            assertTrue(bleedManager.isEntityBleedImmune(entity.getUniqueId()));
        }
    }

    @Nested
    @DisplayName("endEntityBleedImmunity")
    class EndEntityBleedImmunity {

        @DisplayName("removes immunity")
        @Test
        void endEntityBleedImmunity_removesImmunity() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            bleedManager.startBleedImmunity(entity);
            assertTrue(bleedManager.isEntityBleedImmune(entity));

            bleedManager.endEntityBleedImmunity(entity);

            assertFalse(bleedManager.isEntityBleedImmune(entity));
        }

        @DisplayName("UUID overload removes immunity")
        @Test
        void endEntityBleedImmunity_uuid_removesImmunity() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            bleedManager.startBleedImmunity(entity.getUniqueId());

            bleedManager.endEntityBleedImmunity(entity.getUniqueId());

            assertFalse(bleedManager.isEntityBleedImmune(entity.getUniqueId()));
        }

        @DisplayName("no-op for non-immune entity")
        @Test
        void endEntityBleedImmunity_noOp_forNonImmuneEntity() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            bleedManager.endEntityBleedImmunity(entity);

            assertFalse(bleedManager.isEntityBleedImmune(entity));
        }
    }

    @Nested
    @DisplayName("startBleeding guards")
    class StartBleedingGuards {

        @DisplayName("does not start bleeding when entity is already bleeding")
        @Test
        void startBleeding_noOp_whenAlreadyBleeding() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            bleedManager.startBleeding(entity, 3, 1.0);
            assertTrue(bleedManager.isEntityBleeding(entity));

            bleedManager.startBleeding(entity, 5, 2.0);

            assertTrue(bleedManager.isEntityBleeding(entity));
        }

        @DisplayName("does not start bleeding when entity is bleed immune")
        @Test
        void startBleeding_noOp_whenBleedImmune() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            bleedManager.startBleedImmunity(entity);

            bleedManager.startBleeding(entity, 3, 1.0);

            assertFalse(bleedManager.isEntityBleeding(entity));
        }

        @DisplayName("config-based overload starts bleeding")
        @Test
        void startBleeding_configBased_startsBleeding() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            bleedManager.startBleeding(entity);

            assertTrue(bleedManager.isEntityBleeding(entity));
        }

        @DisplayName("holder overload with null holder starts bleeding")
        @Test
        void startBleeding_holderOverload_nullHolder_startsBleeding() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            bleedManager.startBleeding(null, entity);

            assertTrue(bleedManager.isEntityBleeding(entity));
        }

        @DisplayName("holder overload with non-null holder starts bleeding")
        @Test
        void startBleeding_holderOverload_nonNullHolder_startsBleeding() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            us.eunoians.mcrpg.entity.holder.AbilityHolder holder =
                    new us.eunoians.mcrpg.entity.holder.AbilityHolder(mcRPG, UUID.randomUUID());

            bleedManager.startBleeding(holder, entity, 3, 1.0);

            assertTrue(bleedManager.isEntityBleeding(entity));
        }
    }

    @Nested
    @DisplayName("startBleedImmunity")
    class StartBleedImmunity {

        @DisplayName("entity is immune after startBleedImmunity")
        @Test
        void startBleedImmunity_entityBecomesImmune() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            bleedManager.startBleedImmunity(entity);

            assertTrue(bleedManager.isEntityBleedImmune(entity));
            assertFalse(bleedManager.canEntityStartBleeding(entity));
        }

        @DisplayName("immunity expires after delay ticks")
        @Test
        void startBleedImmunity_expiresAfterDelay() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            when(swordsConfig.getInt(SwordsConfigFile.BLEED_IMMUNITY_DURATION)).thenReturn(2);
            bleedManager.startBleedImmunity(entity);
            assertTrue(bleedManager.isEntityBleedImmune(entity));

            server.getScheduler().performTicks(41);

            assertFalse(bleedManager.isEntityBleedImmune(entity));
        }

        @DisplayName("canEntityStartBleeding returns true after immunity expires")
        @Test
        void canEntityStartBleeding_returnsTrue_afterImmunityExpires() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);
            when(swordsConfig.getInt(SwordsConfigFile.BLEED_IMMUNITY_DURATION)).thenReturn(1);
            bleedManager.startBleedImmunity(entity);

            server.getScheduler().performTicks(21);

            assertTrue(bleedManager.canEntityStartBleeding(entity));
        }
    }

    @Nested
    @DisplayName("Lifecycle integration")
    class LifecycleIntegration {

        @DisplayName("bleed -> stop -> can start again")
        @Test
        void fullLifecycle_bleedStopCanStartAgain() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            bleedManager.startBleeding(entity, 3, 1.0);
            assertTrue(bleedManager.isEntityBleeding(entity));
            assertFalse(bleedManager.canEntityStartBleeding(entity));

            bleedManager.stopEntityBleeding(entity);
            assertFalse(bleedManager.isEntityBleeding(entity));
            assertTrue(bleedManager.canEntityStartBleeding(entity));
        }

        @DisplayName("immunity -> end immunity -> can start bleeding")
        @Test
        void fullLifecycle_immunityEndCanBleed() {
            LivingEntity entity = spawnEntity(org.bukkit.entity.Zombie.class);

            bleedManager.startBleedImmunity(entity);
            assertTrue(bleedManager.isEntityBleedImmune(entity));
            assertFalse(bleedManager.canEntityStartBleeding(entity));

            bleedManager.endEntityBleedImmunity(entity);
            assertFalse(bleedManager.isEntityBleedImmune(entity));
            assertTrue(bleedManager.canEntityStartBleeding(entity));

            bleedManager.startBleeding(entity, 3, 1.0);
            assertTrue(bleedManager.isEntityBleeding(entity));
        }
    }
}
