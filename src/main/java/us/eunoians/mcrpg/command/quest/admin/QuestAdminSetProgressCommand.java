package us.eunoians.mcrpg.command.quest.admin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.quest.QuestCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.UUID;

import static us.eunoians.mcrpg.command.CommandPlaceholders.*;

/**
 * Command: {@code /mcrpg quest admin setprogress <quest_uuid> <objective_key> <progress>}
 * <p>
 * Sets the current progression of a specific objective within an active quest.
 * This bypasses the normal progress event flow and directly sets the value.
 * If the new progress meets or exceeds the requirement, the objective will be
 * checked for completion on the next progress tick.
 */
public class QuestAdminSetProgressCommand extends QuestCommandBase {

    private static final Permission QUEST_ADMIN_SET_PROGRESS_PERMISSION = Permission.of("mcrpg.quest.admin.setprogress");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage mm = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("quest")
                .literal("admin")
                .literal("setprogress")
                .required("quest_uuid", StringParser.stringParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>The UUID of the quest")))
                .required("objective_key", StringParser.stringParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>The namespaced key of the objective")))
                .required("progress", IntegerParser.integerParser(0),
                        RichDescription.richDescription(mm.deserialize("<gray>The progress value to set")))
                .permission(Permission.anyOf(ROOT_PERMISSION, QUEST_ADMIN_PERMISSION, QUEST_ADMIN_SET_PROGRESS_PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    String uuidStr = commandContext.get(CloudKey.of("quest_uuid", String.class));
                    String objectiveKeyStr = commandContext.get(CloudKey.of("objective_key", String.class));
                    int progress = commandContext.get(CloudKey.of("progress", Integer.class));

                    UUID questUUID;
                    try {
                        questUUID = UUID.fromString(uuidStr);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_CMD_INVALID_UUID,
                                Map.of(QUEST_UUID.getPlaceholder(), uuidStr)));
                        return;
                    }

                    NamespacedKey objectiveKey = NamespacedKey.fromString(objectiveKeyStr);
                    if (objectiveKey == null) {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_ADMIN_SETPROGRESS_INVALID_OBJECTIVE_KEY,
                                Map.of(OBJECTIVE_KEY.getPlaceholder(), objectiveKeyStr)));
                        return;
                    }

                    QuestManager questManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);

                    QuestInstance target = null;
                    for (QuestInstance quest : questManager.getActiveQuests()) {
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

                    QuestObjectiveInstance targetObjective = null;
                    for (QuestStageInstance stage : target.getQuestStageInstances()) {
                        for (QuestObjectiveInstance obj : stage.getQuestObjectives()) {
                            if (obj.getQuestObjectiveKey().equals(objectiveKey)) {
                                targetObjective = obj;
                                break;
                            }
                        }
                        if (targetObjective != null) break;
                    }

                    if (targetObjective == null) {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_ADMIN_SETPROGRESS_OBJECTIVE_NOT_FOUND,
                                Map.of(OBJECTIVE_KEY.getPlaceholder(), objectiveKey.toString(),
                                        QUEST_UUID.getPlaceholder(), questUUID.toString())));
                        return;
                    }

                    targetObjective.setCurrentProgression(progress);
                    target.markDirty();

                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                            LocalizationKey.QUEST_ADMIN_SETPROGRESS_SUCCESS,
                            Map.of(OBJECTIVE_KEY.getPlaceholder(), objectiveKey.toString(),
                                    CURRENT_PROGRESS.getPlaceholder(), String.valueOf(progress),
                                    REQUIRED_PROGRESS.getPlaceholder(), String.valueOf(targetObjective.getRequiredProgression()),
                                    QUEST_KEY.getPlaceholder(), target.getQuestKey().toString())));
                }));
    }
}
