package us.eunoians.mcrpg.types;

import lombok.Getter;

public enum TipType {
  LOGIN_TIP(null),
  SWORDS_LEVEL_UP_TIP(Skills.SWORDS),
  UNARMED_LEVEL_UP_TIP(Skills.UNARMED),
  MINING_LEVEL_UP_TIP(Skills.MINING),
  HERBALISM_LEVEL_UP_TIP(Skills.HERBALISM),
  ARCHERY_LEVEL_UP_TIP(Skills.ARCHERY),
  WOODCUTTING_LEVEL_UP_TIP(Skills.WOODCUTTING),
  FITNESS(Skills.FITNESS);

  @Getter
  Skills skillType;

  TipType(Skills skillType){
    this.skillType = skillType;
  }

  public static TipType getSkillTipType(Skills skills){
    for(TipType t : TipType.values()){
      if(t.getSkillType() == null){
        continue;
      }
      else{
        if(t.getSkillType() == skills){
          return t;
        }
      }
    }
    return null;
  }
}
