package us.eunoians.mcrpg.skill.experience;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.experience.modifier.ExperienceModifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ExperienceModifierRegistryTest {

    private static final McRPG mcRPG = mock(McRPG.class);
    private static final RegistryAccess registryAccess = RegistryAccess.registryAccess();
    private static final ExperienceModifierRegistry modifierRegistry = new ExperienceModifierRegistry(mcRPG);

    @BeforeAll
    public static void setUpClass() {
        RegistryAccess.registryAccess().register(modifierRegistry);
        doReturn(registryAccess).when(mcRPG).registryAccess();
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
        ExperienceModifier mockModifier = mock(ExperienceModifier.class);
        modifierRegistry.register(mockModifier);
        assertTrue(modifierRegistry.registered(mockModifier));
    }
}
