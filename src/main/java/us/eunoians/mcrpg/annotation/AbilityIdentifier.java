package us.eunoians.mcrpg.annotation;

import us.eunoians.mcrpg.ability.creation.AbilityCreationData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AbilityIdentifier {

    public String id();

    public Class<? extends AbilityCreationData> abilityCreationData();

}
