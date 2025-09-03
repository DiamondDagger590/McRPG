package us.eunoians.mcrpg.skill.experience;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.testing.InternalResetTestTools;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import us.eunoians.mcrpg.McRPG;

import static org.mockito.Mockito.mock;

/**
 * This extension sets up the {@link ExperienceModifierRegistry} to be pulled out of {@link RegistryAccess}
 * for ease of use with unit tests. This test assumes Registry Access is already set up, likely with
 * {@link com.diamonddagger590.mccore.testing.RegistryResetExtension}.
 */
public class ExperienceModifierRegistryExtension implements BeforeEachCallback, AfterEachCallback {

    private static final String EXPERIENCE_MODIFIER_REGISTRY_CLASS_PATH = "us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry";

    @Override
    public void beforeEach(ExtensionContext context)  {
        ExperienceModifierRegistry experienceModifierRegistry = new ExperienceModifierRegistry(mock(McRPG.class));
        try {
            RegistryAccess.registryAccess().register(experienceModifierRegistry);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException("Attempted to register an already registered registry. " +
                    "Ensure your class also uses the RegistryResetExtension before this one.", e);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        try {
            InternalResetTestTools.resetRegistryAccess(EXPERIENCE_MODIFIER_REGISTRY_CLASS_PATH);
        }
        catch (Exception e) {
            /*
            Swallow since it would already be reset at this point
             */
        }
    }
}
