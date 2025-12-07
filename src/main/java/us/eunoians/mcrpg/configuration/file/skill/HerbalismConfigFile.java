package us.eunoians.mcrpg.configuration.file.skill;

import dev.dejvokep.boostedyaml.route.Route;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * This class contains all the {@link Route}s for the {@link us.eunoians.mcrpg.skill.impl.herbalism.Herbalism} configuration file.
 */
public class HerbalismConfigFile extends SkillConfigFile {

    public static final String MATERIAL_MODIFIERS_HEADER = toRoutePath(EXPERIENCE_HEADER, "material-modifiers");
    public static final String BLOCK_EXPERIENCE_HEADER = toRoutePath(EXPERIENCE_HEADER, "sources");
    public static final Route ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN = Route.fromString(toRoutePath(EXPERIENCE_HEADER, "allowed-items-for-experience-gain"));

}
