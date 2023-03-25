package us.eunoians.mcrpg.entity.holder;

import org.bukkit.NamespacedKey;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;

import java.util.Map;

/**
 *
 */
//TODO javadoc
public record AbilityHolderAttributeRecord(Map<NamespacedKey, AbilityAttribute<?>> abilityAttributesMap) {

}
