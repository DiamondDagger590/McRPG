package us.eunoians.mcrpg.skill.experience.modifier;

import com.diamonddagger590.mccore.testing.InternalResetTestTools;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGMockExtension;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistryExtension;

@ExtendWith(RegistryResetExtension.class)
@ExtendWith(ExperienceModifierRegistryExtension.class)
@ExtendWith(McRPGPlayerExtension.class)
public class ExperienceModifiersContractTest {

    private static final String EXPERIENCE_MODIFIER_REGISTRY_CLASS_PATH = "us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry";
    private static final McRPG mcRPG = McRPGMockExtension.mcRPG;

    @AfterEach
    public void cleanUpRegistry() {
        InternalResetTestTools.resetRegistryAccess(EXPERIENCE_MODIFIER_REGISTRY_CLASS_PATH);
    }

}
