package us.eunoians.mcrpg.util.configuration;

import de.articdive.enum_to_yaml.interfaces.ConfigurationEnum;

public enum SoundEnum implements ConfigurationEnum{
  
  MISC_PICKUP_SOUND("Sounds.Misc.Pickup.Sound", "ENTITY_ITEM_PICKUP", "#From the player settings for ignoring certain slots"),
  MISC_PICKUP_VOLUME("Sounds.Misc.Pickup.Volume", 1),
  MISC_PICKUP_PITCH("Sounds.Misc.Pickup.Pitch", 1),
  MISC_ABILITY_POINT_GAIN_SOUND("Sounds.Misc.AbilityPointGain.Sound", "ENTITY_VILLAGER_YES"),
  MISC_ABILITY_POINT_GAIN_VOLUME("Sounds.Misc.AbilityPointGain.Volume", 1),
  MISC_ABILITY_POINT_GAIN_PITCH("Sounds.Misc.AbilityPointGain.Pitch", 1),
  MISC_LEVEL_UP_SOUND("Sounds.Misc.LevelUp.Sound", "ENTITY_PLAYER_LEVELUP"),
  MISC_LEVEL_UP_VOLUME("Sounds.Misc.LevelUp.Volume", 1),
  MISC_LEVEL_UP_PITCH("Sounds.Misc.LevelUp.Pitch", 1),
  MISC_ABILITY_UNLOCKED_SOUND("Sounds.Misc.AbilityUnlocked.Sound", "ENTITY_ENDER_DRAGON_GROWL"),
  MISC_ABILITY_UNLOCKED_VOLUME("Sounds.Misc.AbilityUnlocked.Volume", 1),
  MISC_ABILITY_UNLOCKED_PITCH("Sounds.Misc.AbilityUnlocked.Pitch", 1),
  MISC_REPLACE_COOLDOWN_PENDING_SOUND("Sounds.Misc.ReplaceCooldownPending.Sound", "ENTITY_VILLAGER_NO", "#When the players ability replace cooldown has yet to expire"),
  MISC_REPLACE_COOLDOWN_PENDING_VOLUME("Sounds.Misc.ReplaceCooldownPending.Volume", 1),
  MISC_REPLACE_COOLDOWN_PENDING_PITCH("Sounds.Misc.ReplaceCooldownPending.Pitch", 1),
  MISC_UPGRADE_ABILITY_SOUND("Sounds.Misc.UpgradeAbility.Sound", "ENTITY_VILLAGER_YES"),
  MISC_UPGRADE_ABILITY_VOLUME("Sounds.Misc.UpgradeAbility.Volume", 1),
  MISC_UPGRADE_ABILITY_PITCH("Sounds.Misc.UpgradeAbility.Pitch", 1),
  MISC_CANT_UPGRADE_ABILITY_SOUND("Sounds.Misc.CantUpgradeAbility.Sound", "ENTITY_VILLAGER_NO"),
  MISC_CANT_UPGRADE_ABILITY_VOLUME("Sounds.Misc.CantUpgradeAbility.Volume", 1),
  MISC_CANT_UPGRADE_ABILITY_PITCH("Sounds.Misc.CantUpgradeAbility.Pitch", 1),
  MISC_DENY_REPLACE_SOUND("Sounds.Misc.DenyReplace.Sound", "ENTITY_VILLAGER_NO"),
  MISC_DENY_REPLACE_VOLUME("Sounds.Misc.DenyReplace.Volume", 1),
  MISC_DENY_REPLACE_PITCH("Sounds.Misc.DenyReplace.Pitch", 1),
  MINING_BLAST_MINING_SOUND("Sounds.Mining.BlastMining.Sound", "ENTITY_GENERIC_EXPLODE"),
  MINING_BLAST_MINING_VOLUME("Sounds.Mining.BlastMining.Volume", 1),
  MINING_BLAST_MINING_PITCH("Sounds.Mining.BlastMining.Pitch", 1),
  MINING_SUPER_BREAKER_SOUND("Sounds.Mining.SuperBreaker.Sound", "ENTITY_VEX_CHARGE"),
  MINING_SUPER_BREAKER_VOLUME("Sounds.Mining.SuperBreaker.Volume", 1),
  MINING_SUPER_BREAKER_PITCH("Sounds.Mining.SuperBreaker.Pitch", 1),
  UNARMED_STICKY_FINGERS_SOUND("Sounds.Unarmed.StickyFingers.Sound", "ENTITY_SLIME_ATTACK"),
  UNARMED_STICKY_FINGERS_VOLUME("Sounds.Unarmed.StickyFingers.Volume", 1),
  UNARMED_STICKY_FINGERS_PITCH("Sounds.Unarmed.StickyFingers.Pitch", 1),
  UNARMED_SMITING_FIST_SOUND("Sounds.Unarmed.SmitingFist.Sound", "ENTITY_LIGHTNING_BOLT_THUNDER", "#When an enemy is smited by smiting fist"),
  UNARMED_SMITING_FIST_VOLUME("Sounds.Unarmed.SmitingFist.Volume", 1),
  UNARMED_SMITING_FIST_PITCH("Sounds.Unarmed.SmitingFist.Pitch", 1),
  UNARMED_SMITING_FIST_ENDED_SOUND("Sounds.Unarmed.SmitingFistEnded.Sound", "ENTITY_ILLUSIONER_DEATH"),
  UNARMED_SMITING_FIST_ENDED_VOLUME("Sounds.Unarmed.SmitingFistEnded.Volume", 1),
  UNARMED_SMITING_FIST_ENDED_PITCH("Sounds.Unarmed.SmitingFistEnded.Pitch", 1),
  UNARMED_BERSERK_SOUND("Sounds.Unarmed.Berserk.Sound", "ENTITY_IRON_GOLEM_ATTACK"),
  UNARMED_BERSERK_VOLUME("Sounds.Unarmed.Berserk.Volume", 1),
  UNARMED_BERSERK_PITCH("Sounds.Unarmed.Berserk.Pitch", 1),
  UNARMED_BERSERK_ENDED_SOUND("Sounds.Unarmed.BerserkEnded.Sound", "ENTITY_IRON_GOLEM_DEATH"),
  UNARMED_BERSERK_ENDED_VOLUME("Sounds.Unarmed.BerserkEnded.Volume", 1),
  UNARMED_BERSERK_ENDED_PITCH("Sounds.Unarmed.BerserkEnded.Pitch", 1),
  UNARMED_DENSE_IMPACT_ACTIVATED_SOUND("Sounds.Unarmed.DenseImpactActivated.Sound", "ENTITY_ZOMBIE_ATTACK_IRON_DOOR"),
  UNARMED_DENSE_IMPACT_ACTIVATED_VOLUME("Sounds.Unarmed.DenseImpactActivated.Volume", 1),
  UNARMED_DENSE_IMPACT_ACTIVATED_PITCH("Sounds.Unarmed.DenseImpactActivated.Pitch", 1),
  UNARMED_DENSE_IMPACT_ENDED_SOUND("Sounds.Unarmed.DenseImpactEnded.Sound", "ENTITY_IRON_GOLEM_DEATH"),
  UNARMED_DENSE_IMPACT_ENDED_VOLUME("Sounds.Unarmed.DenseImpactEnded.Volume", 1),
  UNARMED_DENSE_IMPACT_ENDED_PITCH("Sounds.Unarmed.DenseImpactEnded.Pitch", 1),
  TAMING_HELL_HOUND_SUMMON_SOUND("Sounds.Taming.HellHoundSummon.Sound", "ENTITY_BLAZE_AMBIENT"),
  TAMING_HELL_HOUND_SUMMON_VOLUME("Sounds.Taming.HellHoundSummon.Volume", 1),
  TAMING_HELL_HOUND_SUMMON_PITCH("Sounds.Taming.HellHoundSummon.Pitch", 2),
  TAMING_CALL_OF_WILD_SOUND("Sounds.Taming.CallOfWild.Sound", "ENTITY_WOLF_HOWL"),
  TAMING_CALL_OF_WILD_VOLUME("Sounds.Taming.CallOfWild.Volume", 0.4f),
  TAMING_CALL_OF_WILD_PITCH("Sounds.Taming.CallOfWild.Pitch", 1),
  SWORDS_RAGE_SPIKE_SOUND("Sounds.Swords.RageSpike.Sound", "ENTITY_DRAGON_FIREBALL_EXPLODE"),
  SWORDS_RAGE_SPIKE_VOLUME("Sounds.Swords.RageSpike.Volume", 1),
  SWORDS_RAGE_SPIKE_PITCH("Sounds.Swords.RageSpike.Pitch", 1),
  SWORDS_TAINTED_BLADE_ENDED_SOUND("Sounds.Swords.TaintedBladeEnd.Sound", "ENTITY_WITHER_SKELETON_HURT"),
  SWORDS_TAINTED_BLADE_ENDED_VOLUME("Sounds.Swords.TaintedBladeEnd.Volume", 1),
  SWORDS_TAINTED_BLADE_ENDED_PITCH("Sounds.Swords.TaintedBladeEnd.Pitch", 1),
  SWORDS_SERRATED_STRIKES_ENDED_SOUND("Sounds.Swords.SerratedStrikesEnded.Sound", "ENTITY_SHULKER_HURT"),
  SWORDS_SERRATED_STRIKES_ENDED_VOLUME("Sounds.Swords.SerratedStrikesEnded.Volume", 1),
  SWORDS_SERRATED_STRIKES_ENDED_PITCH("Sounds.Swords.SerratedStrikesEnded.Pitch", 1),
  WOODCUTTING_DEMETERS_SHRINE_SOUND("Sounds.Woodcutting.DemetersShrine.Sound", "ENTITY_FIREWORK_ROCKET_LAUNCH"),
  WOODCUTTING_DEMETERS_SHRINE_VOLUME("Sounds.Woodcutting.DemetersShrine.Volume", 1),
  WOODCUTTING_DEMETERS_SHRINE_PITCH("Sounds.Woodcutting.DemetersShrine.Pitch", 5),
  HERBALISM_NATURES_WRATH_SOUND("Sounds.Herbalism.NaturesWrath.Sound", "ENTITY_PLAYER_BURP"),
  HERBALISM_NATURES_WRATH_VOLUME("Sounds.Herbalism.NaturesWrath.Volume", 1),
  HERBALISM_NATURES_WRATH_PITCH("Sounds.Herbalism.NaturesWrath.Pitch", 1),
  EXCAVATION_PANS_SHRINE_SOUND("Sounds.Excavation.PansShrine.Sound", "ENTITY_FIREWORK_ROCKET_LAUNCH"),
  EXCAVATION_PANS_SHRINE_VOLUME("Sounds.Excavation.PansShrine.Volume", 1),
  EXCAVATION_RED_SHRINE_PITCH("Sounds.Excavation.PansShrine.Pitch", 5),
  EXCAVATION_FRENZY_DIG_SOUND("Sounds.Excavation.FrenzyDig.Sound", "ENTITY_VEX_CHARGE"),
  EXCAVATION_FRENZY_DIG_VOLUME("Sounds.Excavation.FrenzyDig.Volume", 1),
  EXCAVATION_FRENZY_DIG_PITCH("Sounds.Excavation.FrenzyDig.Pitch", 1),
  BREWING_FINISH_BREW_SOUND_SOUND("Sounds.Brewing.FinishBrewSound.Sound", "BLOCK_BREWING_STAND_BREW"),
  BREWING_FINISH_BREW_SOUND_VOLUME("Sounds.Brewing.FinishBrewSound.Volume", 1),
  BREWING_FINISH_BREW_SOUND_PITCH("Sounds.Brewing.FinishBrewSound.Pitch", 1),
  BREWING_CIRCES_SHRINE_SOUND("Sounds.Brewing.CircesShrine.Sound", "ENTITY_FIREWORK_ROCKET_LAUNCH"),
  BREWING_CIRCES_SHRINE_VOLUME("Sounds.Brewing.CircesShrine.Volume", 1),
  BREWING_CIRCES_SHRINE_PITCH("Sounds.Brewing.CircesShrine.Pitch", 5),
  ARCHERY_BLESSING_OF_ARTEMIS_SOUND("Sounds.Archery.BlessingOfArtemis.Sound", "ENTITY_WOLF_GROWL"),
  ARCHERY_BLESSING_OF_ARTEMIS_VOLUME("Sounds.Archery.BlessingOfArtemis.Volume", 1),
  ARCHERY_BLESSING_OF_ARTEMIS_PITCH("Sounds.Archery.BlessingOfArtemis.Pitch", 2),
  ARCHERY_BLESSING_OF_APOLLO_SOUND("Sounds.Archery.BlessingOfApollo.Sound", "ENTITY_BLAZE_SHOOT"),
  ARCHERY_BLESSING_OF_APOLLO_VOLUME("Sounds.Archery.BlessingOfApollo.Volume", 1),
  ARCHERY_BLESSING_OF_APOLLO_PITCH("Sounds.Archery.BlessingOfApollo.Pitch", 2),
  ARCHERY_CURSE_OF_HADES_SOUND("Sounds.Archery.CurseOfHades.Sound", "ENTITY_WITHER_SHOOT"),
  ARCHERY_CURSE_OF_HADES_VOLUME("Sounds.Archery.CurseOfHades.Volume", 1),
  ARCHERY_CURSE_OF_HADES_PITCH("Sounds.Archery.CurseOfHades.Pitch", 2),
  ;
  
  
  
  
  
  private String path;
  private Object defaultValue;
  private String[] comments;
  
  
  SoundEnum(String path, Object defaultValue, String... comments) {
    this.path = path;
    this.defaultValue = defaultValue;
    this.comments = comments;
  }
  
  
  @Override
  public String getPath() {
    return path;
  }
  
  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }
  
  @Override
  public String[] getComments() {
    return comments;
  }
}
