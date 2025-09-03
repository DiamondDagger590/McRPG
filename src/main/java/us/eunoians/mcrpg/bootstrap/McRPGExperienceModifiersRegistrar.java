package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.registrar.Registrar;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.experience.modifier.BoostedExperienceModifier;
import us.eunoians.mcrpg.skill.experience.modifier.HeldItemBonusModifier;
import us.eunoians.mcrpg.skill.experience.modifier.RestedExperienceModifier;
import us.eunoians.mcrpg.skill.experience.modifier.SpawnReasonModifier;

/**
 * This registrar is in charge of registering {@link us.eunoians.mcrpg.skill.experience.modifier.ExperienceModifier}s
 * for McRPG.
 */
final class McRPGExperienceModifiersRegistrar implements Registrar<McRPG> {

    @Override
    public void register(@NotNull BootstrapContext<McRPG> context) {
        McRPG plugin = context.plugin();
        ExperienceModifierRegistry experienceModifierRegistry = plugin.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        experienceModifierRegistry.register(new HeldItemBonusModifier());
        experienceModifierRegistry.register(new SpawnReasonModifier());
        experienceModifierRegistry.register(new BoostedExperienceModifier(plugin));
        experienceModifierRegistry.register(new RestedExperienceModifier(plugin));
    }
}
