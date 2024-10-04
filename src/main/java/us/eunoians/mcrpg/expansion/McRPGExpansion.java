package us.eunoians.mcrpg.expansion;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.mining.ExtraOre;
import us.eunoians.mcrpg.ability.impl.mining.ItsATriple;
import us.eunoians.mcrpg.ability.impl.mining.OreScanner;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.ability.impl.swords.DeeperWound;
import us.eunoians.mcrpg.ability.impl.swords.EnhancedBleed;
import us.eunoians.mcrpg.ability.impl.swords.RageSpike;
import us.eunoians.mcrpg.ability.impl.swords.SerratedStrikes;
import us.eunoians.mcrpg.ability.impl.swords.Vampire;
import us.eunoians.mcrpg.ability.impl.woodcutting.DryadsGift;
import us.eunoians.mcrpg.ability.impl.woodcutting.ExtraLumber;
import us.eunoians.mcrpg.ability.impl.woodcutting.HeavySwing;
import us.eunoians.mcrpg.expansion.content.AbilityContentPack;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;
import us.eunoians.mcrpg.expansion.content.SkillContentPack;
import us.eunoians.mcrpg.skill.impl.mining.Mining;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.skill.impl.woodcutting.Woodcutting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Set;

/**
 * The native content expansion for McRPG that contains all out of the box
 * content such as abilities and skills that come with the default installation of the plugin.
 */
public final class McRPGExpansion extends ContentExpansion {

    public static final NamespacedKey EXPANSION_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "mcrpg");
    private final McRPG mcRPG;

    public McRPGExpansion(@NotNull McRPG mcRPG) {
        super(EXPANSION_KEY);
        this.mcRPG = mcRPG;
    }

    @NotNull
    @Override
    public Set<McRPGContentPack<? extends McRPGContent>> getExpansionContent() {
        return Set.of(getSkillContent(), getAbilityContent());
    }

    /**
     * Gets the native {@link SkillContentPack} for McRPG.
     * @return The native {@link SkillContentPack} for McRPG.
     */
    @NotNull
    private SkillContentPack getSkillContent() {
        SkillContentPack skillContent = new SkillContentPack(this);
        skillContent.addContent(new Swords());
        skillContent.addContent(new Mining());
        skillContent.addContent(new Woodcutting());
        return skillContent;
    }

    /**
     * Gets the native {@link AbilityContentPack} for McRPG.
     * @return The native {@link AbilityContentPack} for McRPG.
     */
    @NotNull
    public AbilityContentPack getAbilityContent() {
        AbilityContentPack abilityContent = new AbilityContentPack(this);
        // Swords Abilities
        abilityContent.addContent(new Bleed(mcRPG));
        abilityContent.addContent(new DeeperWound(mcRPG));
        abilityContent.addContent(new Vampire(mcRPG));
        abilityContent.addContent(new EnhancedBleed(mcRPG));
        abilityContent.addContent(new RageSpike(mcRPG));
        abilityContent.addContent(new SerratedStrikes(mcRPG));

        // Mining Abilities
        abilityContent.addContent(new ExtraOre(mcRPG));
        abilityContent.addContent(new ItsATriple(mcRPG));
        abilityContent.addContent(new RemoteTransfer(mcRPG));
        abilityContent.addContent(new OreScanner(mcRPG));

        // Woodcutting Abilities
        abilityContent.addContent(new ExtraLumber(mcRPG));
        abilityContent.addContent(new HeavySwing(mcRPG));
        abilityContent.addContent(new DryadsGift(mcRPG));
        return abilityContent;
    }
}
