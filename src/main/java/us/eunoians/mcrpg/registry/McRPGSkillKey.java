package us.eunoians.mcrpg.registry;

import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.skill.impl.mining.Mining;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;

import static us.eunoians.mcrpg.skill.SkillKeyImpl.create;

/**
 * A soft enum of different {@link SkillKey}s supported by McRPG.
 * <p>
 * To use these, you will need access to the {@link SkillRegistry}
 * via {@link com.diamonddagger590.mccore.registry.RegistryAccess#registry(com.diamonddagger590.mccore.registry.RegistryKey)}
 * and pass in {@link McRPGRegistryKey#SKILL}.
 * <p>
 * From there, you can call {@link SkillRegistry#skill(SkillKey)} with the key
 * you want to get the {@link Skill} for.
 * <p>
 * Example usage:
 * <pre>{@code
 * Swords swords = McRPG.getInstance()
 *     .registryAccess()
 *     .registry(McRPGRegistryKey.SKILL)
 *     .skill(McRPGSkillKey.SWORDS);
 * }</pre>
 */
public interface McRPGSkillKey {

    SkillKey<Herbalism> HERBALISM = create(Herbalism.class);
    SkillKey<Mining> MINING = create(Mining.class);
    SkillKey<Swords> SWORDS = create(Swords.class);
    SkillKey<WoodCutting> WOODCUTTING = create(WoodCutting.class);
}
