package us.eunoians.mcrpg.ability.impl.type;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableAbility;

import java.util.Set;

public interface ReloadableContentAbility extends ConfigurableAbility {

    Set<ReloadableContent<?>> getReloadableContent();
}
