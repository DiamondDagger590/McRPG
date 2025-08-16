package us.eunoians.mcrpg.skill.experience.modifier;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.world.WorldMock;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGMockExtension;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistryExtension;
import us.eunoians.mcrpg.skill.experience.McRPGBaseTest;
import us.eunoians.mcrpg.skill.experience.context.EntityDamageContext;
import us.eunoians.mcrpg.skill.experience.context.MockExperienceContext;
import us.eunoians.mcrpg.util.EntityKeys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockBukkitExtension.class)
@ExtendWith(RegistryResetExtension.class)
@ExtendWith(ExperienceModifierRegistryExtension.class)
@ExtendWith(McRPGPlayerExtension.class)
public class SpawnReasonModifierTest extends McRPGBaseTest {

    private static final McRPG mcRPG = McRPGMockExtension.mcRPG;
    private static ExperienceModifierRegistry experienceModifierRegistry;
    private static SpawnReasonModifier spawnReasonModifier;

    @BeforeAll
    public static void setup() {
        experienceModifierRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        spawnReasonModifier = new SpawnReasonModifier();
        experienceModifierRegistry.register(spawnReasonModifier);
    }

    @Test
    public void givenInvalidSkillExperienceContext_whenCanProcessContext_returnsFalse() {
        MockExperienceContext mockExperienceContext = mock(MockExperienceContext.class);
        assertFalse(spawnReasonModifier.canProcessContext(mockExperienceContext));
    }

    @Test
    public void givenValidSkillExperienceContext_whenCanProcessContext_returnsTrue(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, 2d, false);
        assertTrue(spawnReasonModifier.canProcessContext(entityDamageContext));
    }

    @Test
    public void givenInvalidSkillExperienceContext_whenCalculateModifierForContext_returnsOne() {
        MockExperienceContext mockExperienceContext = mock(MockExperienceContext.class);
        assertEquals(1d, experienceModifierRegistry.calculateModifierForContext(mockExperienceContext));
    }

    @Test
    public void givenValidSkillExperienceContext_whenCalculateModifierForContext_returnsFive(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, 5d, true);
        assertEquals(5d, experienceModifierRegistry.calculateModifierForContext(entityDamageContext));
    }

    @NotNull
    private EntityDamageContext constructEntityDamageContext(@NotNull McRPGPlayer mcRPGPlayer, double heldItemBonus, boolean match) {
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        WorldMock world = new WorldMock(Material.DIRT, 5);
        Entity entity = world.spawnEntity(new Location(world, 0, 0, 0), EntityType.SKELETON);
        doReturn(entity).when(entityDamageByEntityEvent).getEntity();
        if (match) {
            entity.getPersistentDataContainer().set(EntityKeys.SPAWN_REASON_EXPERIENCE_MODIFIER_KEY, PersistentDataType.DOUBLE, heldItemBonus);
        }
        return new EntityDamageContext(mcRPGPlayer.asSkillHolder(), mock(Skill.class), 100, entityDamageByEntityEvent);
    }
}
