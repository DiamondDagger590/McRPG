package us.eunoians.mcrpg.ability.impl.type.configurable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the YAML tier-configuration keys that an ability resolves through the
 * {@link com.diamonddagger590.mccore.parser.Parser Parser} at runtime.
 * <p>
 * Every concrete {@link ConfigurableTierableAbility} implementation <b>must</b> carry this
 * annotation. The {@code ParserConfigKeysPresenceTest} enforces this at CI time. Values
 * listed here are the tier-config sub-keys (e.g., {@code "activation-chance"}, {@code "damage"})
 * that the ability's methods read via the standard tier-then-all-tiers lookup pattern.
 * <p>
 * Generic keys shared by all tierable abilities ({@code unlock-level}, {@code upgrade-point-cost})
 * are validated automatically and do <b>not</b> need to be listed here.
 * <p>
 * If an ability has no additional Parser-backed keys beyond the generic ones, use an empty array:
 * {@code @ParserConfigKeys({})}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ParserConfigKeys {

    /**
     * The tier-configuration sub-keys that this ability resolves through the Parser.
     *
     * @return An array of YAML key names (e.g., {@code "activation-chance"}, {@code "damage"}).
     */
    String[] value();
}
