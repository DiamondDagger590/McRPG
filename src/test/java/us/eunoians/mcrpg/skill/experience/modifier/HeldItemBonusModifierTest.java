package us.eunoians.mcrpg.skill.experience.modifier;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGMockExtension;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistryExtension;
import us.eunoians.mcrpg.skill.experience.McRPGBaseTest;
import us.eunoians.mcrpg.skill.experience.context.EntityDamageContext;
import us.eunoians.mcrpg.skill.experience.context.MockExperienceContext;
import us.eunoians.mcrpg.skill.impl.type.HeldItemBonusSkill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * This unit test covers implementation of {@link HeldItemBonusModifier}
 * being registered and being used to calculate experience modifier.
 */
@ExtendWith(RegistryResetExtension.class)
@ExtendWith(ExperienceModifierRegistryExtension.class)
@ExtendWith(McRPGPlayerExtension.class)
public class HeldItemBonusModifierTest extends McRPGBaseTest {

    private static final McRPG mcRPG = McRPGMockExtension.mcRPG;
    private static ExperienceModifierRegistry experienceModifierRegistry;
    private static HeldItemBonusModifier heldItemBonusModifier;

    @BeforeAll
    public static void setup() {
        experienceModifierRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        heldItemBonusModifier = new HeldItemBonusModifier();
        experienceModifierRegistry.register(heldItemBonusModifier);
    }

    @Test
    public void givenInvalidSkillExperienceContext_whenCanProcessContext_returnsFalse() {
        MockExperienceContext mockExperienceContext = mock(MockExperienceContext.class);
        assertFalse(heldItemBonusModifier.canProcessContext(mockExperienceContext));
    }

    @Test
    public void givenValidSkillExperienceContext_whenCanProcessContext_returnsTrue(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, 2d, false);
        assertTrue(heldItemBonusModifier.canProcessContext(entityDamageContext));
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
        LivingEntity livingEntity = spy(LivingEntity.class);
        LivingEntity attacker = spy(LivingEntity.class);
        doReturn(mcRPGPlayer.getUUID()).when(attacker).getUniqueId();
        doReturn(livingEntity).when(entityDamageByEntityEvent).getEntity();
        doReturn(attacker).when(entityDamageByEntityEvent).getDamager();

        // Set up the equipment of the player
        EntityEquipment entityEquipment = spy(EntityEquipment.class);
        doReturn(entityEquipment).when(attacker).getEquipment();
        ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD);
        ItemStack air = new ItemStack(Material.AIR);
        when(entityEquipment.getItemInMainHand()).thenReturn(diamondSword);
        when(entityEquipment.getItemInOffHand()).thenReturn(air);

        HeldItemBonusSkill heldItemBonusSkill = mock(HeldItemBonusSkill.class);
        ItemStack[] itemStacks = new ItemStack[]{diamondSword, air};
        if (match) {
            when(heldItemBonusSkill.getHeldItemBonus(itemStacks)).thenReturn(heldItemBonus - 1);
        }
        return new EntityDamageContext(mcRPGPlayer.asSkillHolder(), heldItemBonusSkill, 100, entityDamageByEntityEvent);
    }

}
