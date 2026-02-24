package us.eunoians.mcrpg.quest.board.template;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link QuestTemplate} instances.
 * <p>
 * Supports dual-source registration: config-loaded templates (reloadable via
 * {@link #replaceConfigTemplates}) and expansion-registered templates (persistent
 * across reloads via programmatic {@link #register} calls).
 * <p>
 * Additionally supports expansion template directories: third-party expansions can
 * register additional directories that are scanned alongside the primary
 * {@code quest-board/templates/} directory during reload.
 *
 * <h3>Reload behaviour</h3>
 * <ul>
 *   <li><b>Config-loaded templates</b> are replaced atomically by
 *       {@link #replaceConfigTemplates(Map)}. This method is called by
 *       {@code ReloadableTemplateConfig} whenever the template YAML files or the
 *       owning configuration document are reloaded (e.g. via {@code /mcrpg reload}).
 *       All previously config-loaded templates are removed and the fresh set is
 *       inserted.</li>
 *   <li><b>Expansion-registered templates</b> (registered via {@link #register})
 *       are <em>not</em> touched by {@code replaceConfigTemplates}. They persist
 *       across reloads for the lifetime of the server process. This means templates
 *       registered programmatically by a {@code ContentExpansion} survive
 *       {@code /mcrpg reload} without the expansion needing to re-register.</li>
 *   <li><b>Expansion directories</b> registered via
 *       {@link #registerTemplateDirectory(File)} are additive and permanent for the
 *       server session. On reload, the {@code ReloadableTemplateConfig} scans the
 *       primary directory <em>plus</em> all expansion directories, so any YAML
 *       templates shipped by expansions are picked up automatically.</li>
 *   <li><b>Full restart</b> starts with a fresh registry instance (created in
 *       {@code McRPGBootstrap}), so all state is clean.</li>
 * </ul>
 */
public class QuestTemplateRegistry implements Registry<QuestTemplate> {

    private final Map<NamespacedKey, QuestTemplate> templates = new LinkedHashMap<>();
    private final Set<NamespacedKey> configLoadedKeys = new HashSet<>();
    private final List<File> expansionDirectories = new ArrayList<>();

    /**
     * Registers a template. If a template with the same key already exists, it is replaced.
     * Templates registered through this method are considered expansion-registered and
     * survive {@link #replaceConfigTemplates} reloads.
     *
     * @param template the template to register
     */
    public void register(@NotNull QuestTemplate template) {
        templates.put(template.getKey(), template);
    }

    /**
     * Gets a registered template by its key.
     *
     * @param key the namespaced key
     * @return the template, or empty if not registered
     */
    @NotNull
    public Optional<QuestTemplate> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(templates.get(key));
    }

    /**
     * Gets all registered templates.
     *
     * @return an unmodifiable collection of all templates
     */
    @NotNull
    public Collection<QuestTemplate> getAll() {
        return List.copyOf(templates.values());
    }

    /**
     * Returns templates that are board-eligible and support the given rarity.
     *
     * @param rarityKey the rarity to filter by
     * @return an unmodifiable list of eligible templates
     */
    @NotNull
    public List<QuestTemplate> getEligibleTemplates(@NotNull NamespacedKey rarityKey) {
        return templates.values().stream()
                .filter(QuestTemplate::isBoardEligible)
                .filter(t -> t.getSupportedRarities().contains(rarityKey))
                .toList();
    }

    /**
     * Registers an additional directory to scan for template YAML files.
     * Intended for use by expansion packs that ship their own templates.
     * Directories are scanned on initial load and on every reload.
     * <p>
     * Duplicate directories are silently ignored.
     *
     * @param directory the directory containing template YAML files
     */
    public void registerTemplateDirectory(@NotNull File directory) {
        if (!expansionDirectories.contains(directory)) {
            expansionDirectories.add(directory);
        }
    }

    /**
     * Returns all registered expansion template directories.
     *
     * @return a defensive copy of the expansion directories list
     */
    @NotNull
    public List<File> getExpansionDirectories() {
        return List.copyOf(expansionDirectories);
    }

    /**
     * Replaces config-loaded templates (from primary + expansion directories).
     * Programmatically registered templates (via {@link #register}) are untouched.
     *
     * @param freshConfig the new config-loaded templates
     */
    public void replaceConfigTemplates(@NotNull Map<NamespacedKey, QuestTemplate> freshConfig) {
        configLoadedKeys.forEach(templates::remove);
        configLoadedKeys.clear();
        freshConfig.forEach((key, template) -> {
            templates.put(key, template);
            configLoadedKeys.add(key);
        });
    }

    /**
     * Clears all registered templates (config-loaded and expansion-registered).
     */
    public void clear() {
        templates.clear();
        configLoadedKeys.clear();
    }

    @Override
    public boolean registered(@NotNull QuestTemplate template) {
        return templates.containsKey(template.getKey());
    }
}
