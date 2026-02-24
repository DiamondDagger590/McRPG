package us.eunoians.mcrpg.command.quest;

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
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageState;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.*;

/**
 * Command: {@code /mcrpg quest list [player]}
 * <p>
 * Lists all active quests for the sender or a specified player.
 */
public class QuestListCommand extends QuestCommandBase {

    private static final Permission QUEST_LIST_PERMISSION = Permission.of("mcrpg.quest.list");
    private static final Permission QUEST_LIST_OTHERS_PERMISSION = Permission.of("mcrpg.quest.list.others");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage mm = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("quest")
                .literal("list")
                .optional("player", PlayerParser.playerParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>The player to list quests for (defaults to self)")))
                .permission(Permission.anyOf(ROOT_PERMISSION, QUEST_BASE_PERMISSION, QUEST_LIST_PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    Player target = commandContext.<Player>optional(CloudKey.of("player", Player.class))
                            .orElse(sender instanceof Player p ? p : null);

                    if (target == null) {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_CMD_SPECIFY_PLAYER));
                        return;
                    }

                    if (target != sender && sender instanceof Player) {
                        if (!commandContext.sender().getSender().hasPermission(QUEST_LIST_OTHERS_PERMISSION.permissionString())) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.QUEST_LIST_NO_PERMISSION_OTHERS));
                            return;
                        }
                    }

                    QuestManager questManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
                    List<QuestInstance> quests = questManager.getActiveQuestsForPlayer(target.getUniqueId());

                    if (quests.isEmpty()) {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_LIST_NO_ACTIVE_QUESTS,
                                Map.of(TARGET.getPlaceholder(), target.getName())));
                        return;
                    }

                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                            LocalizationKey.QUEST_LIST_HEADER,
                            Map.of(TARGET.getPlaceholder(), target.getName(),
                                    COUNT.getPlaceholder(), String.valueOf(quests.size()))));

                    for (QuestInstance quest : quests) {
                        long completedStages = quest.getQuestStageInstances().stream()
                                .filter(s -> s.getQuestStageState() == QuestStageState.COMPLETED)
                                .count();
                        long totalStages = quest.getQuestStageInstances().size();

                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_LIST_ENTRY,
                                Map.of(QUEST_KEY.getPlaceholder(), quest.getQuestKey().toString(),
                                        QUEST_STATE.getPlaceholder(), quest.getQuestState().toString(),
                                        STAGES_COMPLETED.getPlaceholder(), String.valueOf(completedStages),
                                        STAGES_TOTAL.getPlaceholder(), String.valueOf(totalStages),
                                        QUEST_UUID.getPlaceholder(), quest.getQuestUUID().toString().substring(0, 8))));
                    }
                }));
    }
}
