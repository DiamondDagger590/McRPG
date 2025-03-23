package us.eunoians.mcrpg.configuration.file.localization;

import dev.dejvokep.boostedyaml.route.Route;
import us.eunoians.mcrpg.configuration.file.ConfigFile;

/**
 * A java representation of a language configuration file.
 */
public final class LocalizationKeys extends ConfigFile {

    private static final Route LOGIN_HEADER = Route.fromString("login");
    public static final Route LOGIN_UNABLE_TO_LOAD_DATA = Route.addTo(LOGIN_HEADER, "unable-to-load-data");

    private static final Route SKILL_HEADER = Route.fromString("skill");
    private static final Route SWORDS_HEADER = Route.addTo(SKILL_HEADER, "swords");
    // Bleed
    private static final Route BLEED_HEADER = Route.addTo(SWORDS_HEADER, "bleed");
    public static final Route BLEED_DISPLAY_ITEM_HEADER = Route.addTo(BLEED_HEADER, "display-item");

    // Deeper Wound
    private static final Route DEEPER_WOUND_HEADER = Route.addTo(SWORDS_HEADER, "deeper-wound");
    public static final Route DEEPER_WOUND_DISPLAY_NAME = Route.addTo(DEEPER_WOUND_HEADER, "display-name");
}
