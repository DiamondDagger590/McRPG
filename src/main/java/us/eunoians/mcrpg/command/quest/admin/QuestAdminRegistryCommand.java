package us.eunoians.mcrpg.command.quest.admin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.bukkit.NamespacedKey;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.command.quest.QuestCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Set;

import static us.eunoians.mcrpg.command.CommandPlaceholders.COUNT;
import static us.eunoians.mcrpg.command.CommandPlaceholders.REGISTRY_KEY;
import static us.eunoians.mcrpg.command.CommandPlaceholders.REGISTRY_TYPE;

/**
 * Command: {@code /mcrpg quest admin registry <objectives|rewards|scopes|definitions>}
 * <p>
 * Lists all registered keys from the specified quest registry. Useful for server
 * owners to verify which objective types, reward types, scope providers, and quest
 * definitions are currently loaded.
 */
public class QuestAdminRegistryCommand extends QuestCommandBase {

    private static final Permission QUEST_ADMIN_REGISTRY_PERMISSION = Permission.of("mcrpg.quest.admin.registry");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();

        var base = commandManager.commandBuilder("mcrpg")
                .literal("quest")
                .literal("admin")
                .literal("registry")
                .permission(Permission.anyOf(ROOT_PERMISSION, QUEST_ADMIN_PERMISSION, QUEST_ADMIN_REGISTRY_PERMISSION));

        commandManager.command(base.literal("objectives").handler(ctx -> {
            Set<NamespacedKey> keys = RegistryAccess.registryAccess()
                    .<us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry>registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE)
                    .getRegisteredKeys();
            sendKeyList(ctx.sender().getSender(), "Objective Types", keys);
        }));

        commandManager.command(base.literal("rewards").handler(ctx -> {
            Set<NamespacedKey> keys = RegistryAccess.registryAccess()
                    .<us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry>registry(McRPGRegistryKey.QUEST_REWARD_TYPE)
                    .getRegisteredKeys();
            sendKeyList(ctx.sender().getSender(), "Reward Types", keys);
        }));

        commandManager.command(base.literal("scopes").handler(ctx -> {
            Set<NamespacedKey> keys = RegistryAccess.registryAccess()
                    .<us.eunoians.mcrpg.quest.impl.scope.QuestScopeProviderRegistry>registry(McRPGRegistryKey.QUEST_SCOPE_PROVIDER)
                    .getRegisteredKeys();
            sendKeyList(ctx.sender().getSender(), "Scope Providers", keys);
        }));

        commandManager.command(base.literal("definitions").handler(ctx -> {
            QuestManager questManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
            Set<NamespacedKey> keys = questManager.getQuestDefinitionRegistry().getRegisteredKeys();
            sendKeyList(ctx.sender().getSender(), "Quest Definitions", keys);
        }));
    }

    private static void sendKeyList(Audience sender, String label, Set<NamespacedKey> keys) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        if (keys.isEmpty()) {
            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                    LocalizationKey.QUEST_REGISTRY_EMPTY_MESSAGE,
                    Map.of(REGISTRY_TYPE.getPlaceholder(), label)));
            return;
        }

        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                LocalizationKey.QUEST_REGISTRY_HEADER_MESSAGE,
                Map.of(REGISTRY_TYPE.getPlaceholder(), label,
                        COUNT.getPlaceholder(), String.valueOf(keys.size()))));

        keys.stream()
                .sorted()
                .forEach(key -> sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                        LocalizationKey.QUEST_REGISTRY_ENTRY_MESSAGE,
                        Map.of(REGISTRY_KEY.getPlaceholder(), key.toString()))));
    }
}
