package us.eunoians.mcrpg.registry;

import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityKey;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.herbalism.InstantIrrigation;
import us.eunoians.mcrpg.ability.impl.herbalism.MassHarvest;
import us.eunoians.mcrpg.ability.impl.herbalism.TooManyPlants;
import us.eunoians.mcrpg.ability.impl.herbalism.VerdantSurge;
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
import us.eunoians.mcrpg.ability.impl.woodcutting.NymphsVitality;

import static us.eunoians.mcrpg.ability.AbilityKeyImpl.create;

/**
 * A soft enum of different {@link AbilityKey}s supported by McRPG.
 * <p>
 * To use these, you will need access to the {@link AbilityRegistry}
 * via {@link com.diamonddagger590.mccore.registry.RegistryAccess#registry(com.diamonddagger590.mccore.registry.RegistryKey)}
 * and pass in {@link McRPGRegistryKey#ABILITY}.
 * <p>
 * From there, you can call {@link AbilityRegistry#ability(AbilityKey)} with the key
 * you want to get the {@link Ability} for.
 * <p>
 * Example usage:
 * <pre>{@code
 * NymphsVitality nymphsVitality = McRPG.getInstance()
 *     .registryAccess()
 *     .registry(McRPGRegistryKey.ABILITY)
 *     .ability(McRPGAbilityKey.NYMPHS_VITALITY);
 * }</pre>
 */
public interface McRPGAbilityKey {

    // Herbalism abilities
    AbilityKey<InstantIrrigation> INSTANT_IRRIGATION = create(InstantIrrigation.class);
    AbilityKey<MassHarvest> MASS_HARVEST = create(MassHarvest.class);
    AbilityKey<TooManyPlants> TOO_MANY_PLANTS = create(TooManyPlants.class);
    AbilityKey<VerdantSurge> VERDANT_SURGE = create(VerdantSurge.class);

    // Mining abilities
    AbilityKey<ExtraOre> EXTRA_ORE = create(ExtraOre.class);
    AbilityKey<ItsATriple> ITS_A_TRIPLE = create(ItsATriple.class);
    AbilityKey<OreScanner> ORE_SCANNER = create(OreScanner.class);
    AbilityKey<RemoteTransfer> REMOTE_TRANSFER = create(RemoteTransfer.class);

    // Swords abilities
    AbilityKey<Bleed> BLEED = create(Bleed.class);
    AbilityKey<DeeperWound> DEEPER_WOUND = create(DeeperWound.class);
    AbilityKey<EnhancedBleed> ENHANCED_BLEED = create(EnhancedBleed.class);
    AbilityKey<RageSpike> RAGE_SPIKE = create(RageSpike.class);
    AbilityKey<SerratedStrikes> SERRATED_STRIKES = create(SerratedStrikes.class);
    AbilityKey<Vampire> VAMPIRE = create(Vampire.class);

    // Woodcutting abilities
    AbilityKey<DryadsGift> DRYADS_GIFT = create(DryadsGift.class);
    AbilityKey<ExtraLumber> EXTRA_LUMBER = create(ExtraLumber.class);
    AbilityKey<HeavySwing> HEAVY_SWING = create(HeavySwing.class);
    AbilityKey<NymphsVitality> NYMPHS_VITALITY = create(NymphsVitality.class);
}
