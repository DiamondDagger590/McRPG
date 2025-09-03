package us.eunoians.mcrpg.skill.experience;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.experience.context.BlockBreakContext;
import us.eunoians.mcrpg.skill.experience.context.MockExperienceContext;
import us.eunoians.mcrpg.skill.experience.modifier.MockModifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests the functionality of the {@link ExperienceModifierRegistry}.
 */
@ExtendWith(McRPGPlayerExtension.class)
@ExtendWith(ExperienceModifierRegistryExtension.class)
public class ExperienceModifierRegistryTest extends McRPGBaseTest {

    private static final RegistryAccess registryAccess = RegistryAccess.registryAccess();
    private ExperienceModifierRegistry modifierRegistry;

    private MockModifier mockModifier;

    @BeforeEach
    public void setUp() {
        // Pulls from an extension
        modifierRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        mockModifier = new MockModifier(mcRPG);
    }

    @Test
    @DisplayName("Given a registry already registered in RegistryAccess, when registering it again, then an IllegalArgumentException is thrown")
    public void register_throwsIllegalArgument_whenRegistryAlreadyRegistered() {
        assertThrows(IllegalArgumentException.class, () -> {
            RegistryAccess.registryAccess().register(modifierRegistry);
        });
    }

    @Test
    @DisplayName("Given the experience modifier registry, when checking if it is registered, then it returns true")
    public void isRegistered_returnsTrue_forExperienceRegistry() {
        assertTrue(RegistryAccess.registryAccess().registered(modifierRegistry));
    }

    @Test
    @DisplayName("Given the EXPERIENCE_MODIFIER key, when retrieving the registry, then the same registry instance is returned")
    public void registry_returnsSameInstance_forKey() {
        ExperienceModifierRegistry returnedRegistry =
                RegistryAccess.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        assertEquals(modifierRegistry, returnedRegistry);
    }

    @Test
    @DisplayName("Given a valid modifier, when registering it, then the modifier is added to the registry")
    public void register_addsModifier_whenValid() {
        modifierRegistry.register(mockModifier);
        assertTrue(modifierRegistry.registered(mockModifier));
    }

    @Test
    @DisplayName("Given a mock experience context handled by MockModifier, when calculating the modifier, then it returns 10")
    public void calculateModifier_returnsTen_forMockContext() {
        MockExperienceContext mockExperienceContext = mock(MockExperienceContext.class);
        modifierRegistry.register(mockModifier);
        assertEquals(10, modifierRegistry.calculateModifierForContext(mockExperienceContext));
    }

    @Test
    @DisplayName("Given an unsupported context (BlockBreakContext), when calculating the modifier, then it returns 1")
    public void calculateModifier_returnsOne_forUnsupportedContext() {
        BlockBreakContext blockBreakContext = mock(BlockBreakContext.class);
        assertEquals(1, modifierRegistry.calculateModifierForContext(blockBreakContext));
    }
}
