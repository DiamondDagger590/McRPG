package us.eunoians.mcrpg.skill.experience;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import us.eunoians.mcrpg.McRPGMockExtension;

public class ExperienceModifierRegistryExtension extends McRPGMockExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context)  {
        super.beforeAll(context);
        ExperienceModifierRegistry experienceModifierRegistry = new ExperienceModifierRegistry(mcRPG);
        try {
            RegistryAccess.registryAccess().register(experienceModifierRegistry);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException("Attempted to register an already registered registry. " +
                    "Ensure your class also uses the RegistryResetExtension before this one.", e);
        }
    }
}
