package us.eunoians.mcrpg.localization;

import com.diamonddagger590.mccore.localization.Localization;
import dev.dejvokep.boostedyaml.YamlDocument;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Locale;

/**
 * A localization that provides a configuration file in the form of a {@link YamlDocument}
 * for a specific {@link Locale}.
 */
public interface McRPGLocalization extends McRPGContent, Localization {
}
