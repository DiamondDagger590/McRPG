package us.eunoians.mcrpg.command.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.quest.parser.QuestDefinitionParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static us.eunoians.mcrpg.command.CommandPlaceholders.*;

/**
 * Command: {@code /mcrpg quest start <quest_key> [player]}
 * <p>
 * Starts a new quest instance from a registered definition. If no player is specified,
 * the sender is used. Resolves the scope provider and assigns the scope to the new instance.
 */
public class QuestStartCommand extends QuestCommandBase {

    private static final Permission QUEST_START_PERMISSION = Permission.of("mcrpg.quest.start");
    private static final Permission QUEST_START_OTHERS_PERMISSION = Permission.of("mcrpg.quest.start.others");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage mm = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("quest")
                .literal("start")
                .required("quest_key", QuestDefinitionParser.questDefinitionParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>The quest definition to start")))
                .optional("player", PlayerParser.playerParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>The target player (defaults to self)")))
                .permission(Permission.anyOf(ROOT_PERMISSION, QUEST_BASE_PERMISSION, QUEST_START_PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    QuestDefinition definition = commandContext.get(CloudKey.of("quest_key", QuestDefinition.class));
                    Player target = commandContext.<Player>optional(CloudKey.of("player", Player.class))
                            .orElse(sender instanceof Player p ? p : null);

                    if (target == null) {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_CMD_SPECIFY_PLAYER));
                        return;
                    }

                    if (target != sender && sender instanceof Player) {
                        if (!commandContext.sender().getSender().hasPermission(QUEST_START_OTHERS_PERMISSION.permissionString())) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.QUEST_START_NO_PERMISSION_OTHERS));
                            return;
                        }
                    }

                    QuestManager questManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);

                    UUID targetUUID = target.getUniqueId();
                    Player finalTarget = target;
                    Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.DATABASE).getDatabase();
                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            if (!questManager.canPlayerStartQuest(connection, targetUUID, definition)) {
                                McRPG.getInstance().getServer().getScheduler().runTask(McRPG.getInstance(), () ->
                                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                                LocalizationKey.QUEST_START_CANNOT_START,
                                                Map.of(TARGET.getPlaceholder(), finalTarget.getName(),
                                                        QUEST_KEY.getPlaceholder(), definition.getQuestKey().toString()))));
                                return;
                            }

                            McRPG.getInstance().getServer().getScheduler().runTask(McRPG.getInstance(), () -> {
                                questManager.startQuest(definition, targetUUID, new ManualQuestSource()).ifPresentOrElse(
                                        instance -> sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                                LocalizationKey.QUEST_START_SUCCESS,
                                                Map.of(QUEST_KEY.getPlaceholder(), definition.getQuestKey().toString(),
                                                        TARGET.getPlaceholder(), finalTarget.getName(),
                                                        QUEST_UUID.getPlaceholder(), instance.getQuestUUID().toString()))),
                                        () -> sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                                LocalizationKey.QUEST_START_NO_SCOPE_PROVIDER,
                                                Map.of(SCOPE_TYPE.getPlaceholder(), definition.getScopeType().toString()))));
                            });
                        } catch (SQLException e) {
                            McRPG.getInstance().getLogger().log(Level.SEVERE, "Failed to validate quest start", e);
                            McRPG.getInstance().getServer().getScheduler().runTask(McRPG.getInstance(), () ->
                                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                            LocalizationKey.QUEST_START_ERROR)));
                        }
                    });
                }));
    }
}
