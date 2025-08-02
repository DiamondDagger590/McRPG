package us.eunoians.mcrpg.exception.localization;

import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown whenever a string is attempted to be parsed
 * into a {@link java.util.Locale} but fails.
 */
public class LocaleParseException extends RuntimeException {

    private final String locale;

    public LocaleParseException(@NotNull String parsedLocale) {
        super(String.format("Could not parse locale '%s'", parsedLocale));
        this.locale = parsedLocale;
    }

    public LocaleParseException(@NotNull String parsedLocale, @NotNull String message) {
        super(message);
        this.locale = parsedLocale;
    }

    /**
     * Gets the string that failed to be parsed into a
     * {@link java.util.Locale}.
     *
     * @return The string that failed.
     */
    @NotNull
    public String getParsedLocale() {
        return locale;
    }
}
