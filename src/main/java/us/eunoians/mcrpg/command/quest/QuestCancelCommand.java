package us.eunoians.mcrpg.command.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static us.eunoians.mcrpg.command.CommandPlaceholders.*;

/**
 * Command: {@code /mcrpg quest cancel <quest_uuid>}
 * <p>
 * Cancels an active quest instance by UUID.
 */
public class QuestCancelCommand extends QuestCommandBase {

    private static final Permission QUEST_CANCEL_PERMISSION = Permission.of("mcrpg.quest.cancel");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage mm = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("quest")
                .literal("cancel")
                .required("quest_uuid", StringParser.stringParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>The UUID of the quest to cancel")))
                .permission(Permission.anyOf(ROOT_PERMISSION, QUEST_BASE_PERMISSION, QUEST_CANCEL_PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    String uuidStr = commandContext.get(CloudKey.of("quest_uuid", String.class));

                    UUID questUUID;
                    try {
                        questUUID = UUID.fromString(uuidStr);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_CMD_INVALID_UUID,
                                Map.of(QUEST_UUID.getPlaceholder(), uuidStr)));
                        return;
                    }

                    QuestManager questManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);

                    List<QuestInstance> activeQuests = questManager.getActiveQuests();
                    QuestInstance target = null;
                    for (QuestInstance quest : activeQuests) {
                        if (quest.getQuestUUID().equals(questUUID)) {
                            target = quest;
                            break;
                        }
                    }

                    if (target == null) {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_CMD_QUEST_NOT_FOUND,
                                Map.of(QUEST_UUID.getPlaceholder(), questUUID.toString())));
                        return;
                    }

                    if (target.getQuestState() != QuestState.IN_PROGRESS && target.getQuestState() != QuestState.NOT_STARTED) {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_CANCEL_NOT_CANCELLABLE,
                                Map.of(QUEST_STATE.getPlaceholder(), target.getQuestState().toString())));
                        return;
                    }

                    target.cancel();
                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                            LocalizationKey.QUEST_CANCEL_SUCCESS,
                            Map.of(QUEST_KEY.getPlaceholder(), target.getQuestKey().toString(),
                                    QUEST_UUID.getPlaceholder(), questUUID.toString())));
                }));
    }
}
