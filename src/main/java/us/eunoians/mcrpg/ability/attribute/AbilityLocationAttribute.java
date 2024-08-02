package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.util.Methods;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AbilityLocationAttribute extends OptionalSavingAbilityAttribute<Location> {

    private static final Location DEFAULT_LOCATION = new Location(null, 0, 0 ,0);

    AbilityLocationAttribute() {
        super("location", AbilityAttributeManager.ABILITY_LOCATION_ATTRIBUTE);
    }

    public AbilityLocationAttribute(@NotNull Location content) {
        super("location", AbilityAttributeManager.ABILITY_LOCATION_ATTRIBUTE, content);
    }

    @Override
    public boolean shouldContentBeSaved() {
        return getContent().getWorld() != null;
    }

    @NotNull
    @Override
    public AbilityAttribute<Location> create(@NotNull Location content) {
        return new AbilityLocationAttribute(content);
    }

    @NotNull
    @Override
    public Location convertContent(@NotNull String stringContent) {
        Optional<Location> optionalLocation = Methods.deserializeLocation(stringContent);
        if (optionalLocation.isEmpty()) {
            throw new RuntimeException("Expected a location to be able to be deserialized, but it wasn't. Serialized content was :" + stringContent);
        }
        return optionalLocation.get();
    }

    @NotNull
    @Override
    public Location getDefaultContent() {
        return DEFAULT_LOCATION;
    }

    @NotNull
    @Override
    public String serializeContent() {
        return Methods.serializeLocation(getContent());
    }
}
