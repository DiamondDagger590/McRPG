package us.eunoians.mcrpg.ability.impl;

import com.diamonddagger590.mccore.configuration.ReloadableContent;

import java.util.Set;

public interface ReloadableContentAbility extends ConfigurableAbility {

    Set<ReloadableContent<?>> getReloadableContent();
}
