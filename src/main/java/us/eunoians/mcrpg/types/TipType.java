package us.eunoians.mcrpg.types;

import lombok.Getter;

//TODO for new skills
public enum TipType{
  
  ARCHERY_LEVEL_UP_TIP(Skills.ARCHERY),
  AXES_LEVEL_UP_TIP(Skills.AXES),
  EXCAVATION_LEVEL_UP_TIP(Skills.EXCAVATION),
  FISHING_LEVEL_UP_TIP(Skills.FISHING),
  FITNESS_LEVEL_UP_TIP(Skills.FITNESS),
  HERBALISM_LEVEL_UP_TIP(Skills.HERBALISM),
  LOGIN_TIP(null),
  MINING_LEVEL_UP_TIP(Skills.MINING),
  SORCERY_LEVEL_UP_TIP(Skills.SORCERY),
  SWORDS_LEVEL_UP_TIP(Skills.SWORDS),
  UNARMED_LEVEL_UP_TIP(Skills.UNARMED),
  WOODCUTTING_LEVEL_UP_TIP(Skills.WOODCUTTING);
  
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
