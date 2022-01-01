package us.eunoians.mcrpg.database.tables;

import us.eunoians.mcrpg.types.AbilityType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillDAOWrapper{

    private final UUID uuid;
    private final int currentExp;
    private final int currentLevel;
    private final Map<AbilityType, Boolean> abilityToggledMap;
    private final Map<AbilityType, Integer> abilityTiers;
    private final Map<AbilityType, Integer> abilityCooldowns;
    private final Map<AbilityType, Boolean> pendingAbilities;

    SkillDAOWrapper(UUID uuid, int currentExp, int currentLevel){
        this.uuid = uuid;
        this.currentExp = currentExp;
        this.currentLevel = currentLevel;
        this.abilityToggledMap = new HashMap<>();
        this.abilityTiers = new HashMap<>();
        this.abilityCooldowns = new HashMap<>();
        this.pendingAbilities = new HashMap<>();
    }
}
