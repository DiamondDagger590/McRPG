package us.eunoians.mcrpg.exception.localization;

import com.google.common.collect.ImmutableSet;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Set;

/**
 * This exception is thrown whenever a translation is searched for using a given {@link Route} and
 * nothing is returned for a given set of {@link Locale}s.
 */
public class NoLocalizationContainsMessageException extends RuntimeException {

    private final Route route;
    private final Set<Locale> checkedLocales;

    public NoLocalizationContainsMessageException(@NotNull Route route, @NotNull Set<Locale> checkedLocales) {
        this.route = route;
        this.checkedLocales = checkedLocales;
    }

    /**
     * Gets the {@link Route} that a translation was searched for.
     *
     * @return The {@link Route} that a translation was searched for.
     */
    @NotNull
    public Route getRoute() {
        return route;
    }

    /**
     * Gets a {@link Set} of {@link Locale}s that were checked for translations.
     *
     * @return A {@link Set} of {@link Locale}s that were checked for translations.
     */
    @NotNull
    public Set<Locale> getCheckedLocales() {
        return ImmutableSet.copyOf(checkedLocales);
    }

    @NotNull
    @Override
    public String getMessage() {
        return String.format("Attempted to find localization for key %s. The following locales were checked and returned no matches: %s", route, checkedLocales.stream().map(Locale::getDisplayName).reduce((s, s2) -> s + "," + s2));
    }
}
