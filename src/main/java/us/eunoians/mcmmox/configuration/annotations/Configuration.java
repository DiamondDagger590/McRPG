package us.eunoians.mcmmox.configuration.annotations;

import us.eunoians.mcmmox.configuration.enums.Config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Annotation interface to initialize the {@link Config type} and the header of
 * the configuration file.<p/>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
    /**
     * Quarry the {@link Config type} of configuration
     *
     * @return Config enumeration object.
     */
    Config type();

    /**
     * Quarry the header of the configuration file
     *
     * @return Configuration header.
     */
    String[] header();

    /**
     * Path where the file should be shaved under in.
     *
     * Leave as empty {@link String string} for default location.
     * @return File path.
     */
    String path() default "";
}
