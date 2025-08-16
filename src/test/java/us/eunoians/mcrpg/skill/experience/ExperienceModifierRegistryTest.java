package us.eunoians.mcrpg.skill.experience;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGMockExtension;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.experience.context.BlockBreakContext;
import us.eunoians.mcrpg.skill.experience.context.MockExperienceContext;
import us.eunoians.mcrpg.skill.experience.modifier.MockModifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(RegistryResetExtension.class)
@ExtendWith(McRPGPlayerExtension.class)
@ExtendWith(ExperienceModifierRegistryExtension.class)
public class ExperienceModifierRegistryTest {

    private static final McRPG mcRPG = McRPGMockExtension.mcRPG;

    private static final RegistryAccess registryAccess = RegistryAccess.registryAccess();
    private static ExperienceModifierRegistry modifierRegistry;

    private static MockModifier mockModifier;

    @BeforeAll
    public static void setUpClass() {
        // Pulls from an extension
        modifierRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        mockModifier = new MockModifier(mcRPG);
    }

    @Test
    public void givenAlreadyRegisteredRegistry_whenRegister_throwIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {RegistryAccess.registryAccess().register(modifierRegistry);});
    }

    @Test
    public void givenExperienceRegistry_whenIsRegistered_thenReturnTrue () {
        assertTrue(RegistryAccess.registryAccess().registered(modifierRegistry));
    }

    @Test
    public void givenExperienceRegistryKey_whenGetRegistry_thenReturnRegistry() {
        ExperienceModifierRegistry returnedRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        assertEquals(modifierRegistry, returnedRegistry);
    }

    @Test
    public void register_withValidModifier_addsModifierToRegistry() {
        modifierRegistry.register(mockModifier);
        assertTrue(modifierRegistry.registered(mockModifier));
    }

    @Test
    public void givenMockSkillContext_whenCalculateModifierForContext_thenReturn11() {
        MockExperienceContext mockExperienceContext = mock(MockExperienceContext.class);
        assertEquals(11, modifierRegistry.calculateModifierForContext(mockExperienceContext));
    }

    @Test
    public void givenInvalidMockSkillContext_whenCalculateModifierForContext_thenReturn1() {
        BlockBreakContext blockBreakContext = mock(BlockBreakContext.class);
        assertEquals(1, modifierRegistry.calculateModifierForContext(blockBreakContext));
    }
}
