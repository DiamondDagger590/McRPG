package us.eunoians.mcrpg.configuration.file.skill;

import dev.dejvokep.boostedyaml.route.Route;

public class MiningConfigFile extends AbilityConfigFile {

    private static final int CURRENT_VERSION = 1;

    // Experience
    public static final Route BLOCK_EXPERIENCE_HEADER = Route.addTo(EXPERIENCE_HEADER, "sources");

    // Extra Ore
    private static final Route EXTRA_ORE_HEADER = Route.addTo(ABILITY_CONFIGURATION_HEADER, "extra-ore");
    public static final Route EXTRA_ORE_ENABLED = Route.addTo(EXTRA_ORE_HEADER, "enabled");
    public static final Route EXTRA_ORE_ACTIVATION_EQUATION = Route.addTo(EXTRA_ORE_HEADER, "activation-equation");
    public static final Route EXTRA_ORE_VALID_DROPS = Route.addTo(EXTRA_ORE_HEADER, "valid-drops");
}
