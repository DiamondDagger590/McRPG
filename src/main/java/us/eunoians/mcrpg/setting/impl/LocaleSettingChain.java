package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds and manages a circular linked chain of all available locale settings.
 * <p>
 * The chain order is: CLIENT_LOCALE -> SERVER_LOCALE -> [specific locales...] -> (wraps to CLIENT_LOCALE)
 * <p>
 * This class provides methods to get properly connected {@link LinkedNode}s for any setting
 * in the chain, allowing traversal in both directions.
 */
public final class LocaleSettingChain {

    private LocaleSettingChain() {
        // Utility class
    }

    /**
     * Builds a circular linked chain of all locale settings and returns the node
     * for the specified setting.
     * <p>
     * The chain is built fresh each time to account for dynamically added/removed locales.
     * The order is: CLIENT_LOCALE -> SERVER_LOCALE -> [specific locales sorted alphabetically] -> (wraps to CLIENT_LOCALE)
     *
     * @param setting The setting to find in the chain.
     * @return The {@link LinkedNode} for the specified setting, connected to the full chain.
     */
    @NotNull
    public static LinkedNode<? extends PlayerSetting> getNodeForSetting(@NotNull LocalePlayerSetting setting) {
        // Build the chain
        LinkedNode<LocalePlayerSetting> head = buildChain();

        // Find the node for the specified setting
        LinkedNode<LocalePlayerSetting> current = head;
        do {
            if (settingsMatch(current.getNodeValue(), setting)) {
                return current;
            }
            current = current.getNextNode();
        } while (current != head && current.hasNext());

        // If not found (shouldn't happen), return head
        return head;
    }

    /**
     * Gets the next setting node for the given setting.
     *
     * @param setting The current setting.
     * @return The {@link LinkedNode} containing the next setting in the chain.
     */
    @NotNull
    public static LinkedNode<? extends PlayerSetting> getNextSettingNode(@NotNull LocalePlayerSetting setting) {
        LinkedNode<? extends PlayerSetting> node = getNodeForSetting(setting);
        return node.getNextNode();
    }

    /**
     * Builds the complete circular linked chain of locale settings.
     *
     * @return The head node (CLIENT_LOCALE) of the chain.
     */
    @NotNull
    private static LinkedNode<LocalePlayerSetting> buildChain() {
        // Get all available locale codes
        List<String> availableLocaleCodes = getAvailableLocaleCodes();

        // Create head node (CLIENT_LOCALE)
        LinkedNode<LocalePlayerSetting> head = new LinkedNode<>(LocaleSetting.CLIENT_LOCALE);
        LinkedNode<LocalePlayerSetting> current = head;

        // Add SERVER_LOCALE
        LinkedNode<LocalePlayerSetting> serverNode = new LinkedNode<>(LocaleSetting.SERVER_LOCALE);
        current.setNext(serverNode);
        current = serverNode;

        // Add all specific locales
        for (String localeCode : availableLocaleCodes) {
            LinkedNode<LocalePlayerSetting> localeNode = new LinkedNode<>(new SpecificLocaleSetting(localeCode));
            current.setNext(localeNode);
            current = localeNode;
        }

        // Close the loop - last node points back to head
        current.setNext(head);

        return head;
    }

    /**
     * Checks if two settings are logically equal.
     *
     * @param a First setting.
     * @param b Second setting.
     * @return {@code true} if the settings represent the same option.
     */
    private static boolean settingsMatch(@NotNull LocalePlayerSetting a, @NotNull LocalePlayerSetting b) {
        if (a instanceof LocaleSetting enumA && b instanceof LocaleSetting enumB) {
            return enumA == enumB;
        }
        if (a instanceof SpecificLocaleSetting specA && b instanceof SpecificLocaleSetting specB) {
            return specA.getLocaleCode().equals(specB.getLocaleCode());
        }
        return false;
    }

    /**
     * Gets a list of all unique available locale codes.
     *
     * @return A sorted list of unique locale codes.
     */
    @NotNull
    private static List<String> getAvailableLocaleCodes() {
        Set<Locale> registeredLocales = McRPG.getInstance()
                .registryAccess()
                .registry(com.diamonddagger590.mccore.registry.RegistryKey.MANAGER)
                .manager(us.eunoians.mcrpg.registry.manager.McRPGManagerKey.LOCALIZATION)
                .getRegisteredLocales();

        return registeredLocales.stream()
                .map(Locale::toString)
                .filter(code -> !code.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
