package us.eunoians.mcrpg.util.worldguard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles various actions that can be handled by {@link WGSupportManager}
 *
 * @author DiamondDagger590
 */
public enum ActionParserType {

    ATTACK("Attack"),
    BREAK("Break"),
    ABILITY_ACTIVATE("Ability Activate"),
    EXP_GAIN("Exp Gain");

    @NotNull
    private final String name;

    ActionParserType(String name){
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public static ActionParserType fromString(String type){
        for(ActionParserType t : ActionParserType.values()){
            if(t.getName().equals(type)){
                return t;
            }
        }
        return null;
    }
}