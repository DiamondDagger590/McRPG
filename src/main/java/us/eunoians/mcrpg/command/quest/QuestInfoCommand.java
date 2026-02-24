package us.eunoians.mcrpg.command.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.util.Methods;
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
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageState;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import static us.eunoians.mcrpg.command.CommandPlaceholders.*;

/**
 * Command: {@code /mcrpg quest info <quest_uuid>}
 * <p>
 * Displays detailed information about a quest instance including all stages,
 * objectives, and their progress.
 */
public class QuestInfoCommand extends QuestCommandBase {

    private static final Permission QUEST_INFO_PERMISSION = Permission.of("mcrpg.quest.info");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage mm = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("quest")
                .literal("info")
                .required("quest_uuid", StringParser.stringParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>The UUID of the quest to inspect")))
                .permission(Permission.anyOf(ROOT_PERMISSION, QUEST_BASE_PERMISSION, QUEST_INFO_PERMISSION))
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

                    var request = questManager.getQuestInstance(questUUID);
                    request.getItemCompletableFuture().thenAccept(quest -> {
                        if (quest == null) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.QUEST_CMD_QUEST_NOT_FOUND,
                                    Map.of(QUEST_UUID.getPlaceholder(), questUUID.toString())));
                            return;
                        }
                        displayQuestInfo(sender, quest, localizationManager);
                    });
                }));
    }

    private static void displayQuestInfo(Audience sender, QuestInstance quest, McRPGLocalizationManager lm) {
        sender.sendMessage(lm.getLocalizedMessageAsComponent(sender, LocalizationKey.QUEST_INFO_TITLE));
        sender.sendMessage(lm.getLocalizedMessageAsComponent(sender, LocalizationKey.QUEST_INFO_DEFINITION,
                Map.of(QUEST_KEY.getPlaceholder(), quest.getQuestKey().toString())));
        sender.sendMessage(lm.getLocalizedMessageAsComponent(sender, LocalizationKey.QUEST_INFO_UUID,
                Map.of(QUEST_UUID.getPlaceholder(), quest.getQuestUUID().toString())));
        sender.sendMessage(lm.getLocalizedMessageAsComponent(sender, LocalizationKey.QUEST_INFO_STATE,
                Map.of(QUEST_STATE.getPlaceholder(), quest.getQuestState().toString())));
        sender.sendMessage(lm.getLocalizedMessageAsComponent(sender, LocalizationKey.QUEST_INFO_SCOPE_TYPE,
                Map.of(SCOPE_TYPE.getPlaceholder(), quest.getScopeType().toString())));

        quest.getStartTime().ifPresent(t ->
                sender.sendMessage(lm.getLocalizedMessageAsComponent(sender, LocalizationKey.QUEST_INFO_STARTED,
                        Map.of(TIMESTAMP.getPlaceholder(), formatTimestamp(t)))));
        quest.getEndTime().ifPresent(t ->
                sender.sendMessage(lm.getLocalizedMessageAsComponent(sender, LocalizationKey.QUEST_INFO_ENDED,
                        Map.of(TIMESTAMP.getPlaceholder(), formatTimestamp(t)))));
        quest.getExpirationTime().ifPresent(t ->
                sender.sendMessage(lm.getLocalizedMessageAsComponent(sender, LocalizationKey.QUEST_INFO_EXPIRES,
                        Map.of(TIMESTAMP.getPlaceholder(), formatTimestamp(t)))));

        sender.sendMessage(lm.getLocalizedMessageAsComponent(sender, LocalizationKey.QUEST_INFO_STAGES_HEADER,
                Map.of(COUNT.getPlaceholder(), String.valueOf(quest.getQuestStageInstances().size()))));

        for (QuestStageInstance stage : quest.getQuestStageInstances()) {
            sender.sendMessage(lm.getLocalizedMessageAsComponent(sender, LocalizationKey.QUEST_INFO_STAGE_ENTRY,
                    Map.of(STATE_COLOR.getPlaceholder(), stateColor(stage.getQuestStageState()),
                            STAGE_STATE.getPlaceholder(), stage.getQuestStageState().toString(),
                            STAGE_KEY.getPlaceholder(), stage.getStageKey().toString(),
                            PHASE_INDEX.getPlaceholder(), String.valueOf(stage.getPhaseIndex()))));

            for (QuestObjectiveInstance obj : stage.getQuestObjectives()) {
                double progress = obj.getRequiredProgression() > 0
                        ? (double) obj.getCurrentProgression() / obj.getRequiredProgression()
                        : 0;
                sender.sendMessage(lm.getLocalizedMessageAsComponent(sender, LocalizationKey.QUEST_INFO_OBJECTIVE_ENTRY,
                        Map.of(STATE_COLOR.getPlaceholder(), stateColor(obj.getQuestObjectiveState()),
                                OBJECTIVE_KEY.getPlaceholder(), obj.getQuestObjectiveKey().toString(),
                                CURRENT_PROGRESS.getPlaceholder(), String.valueOf(obj.getCurrentProgression()),
                                REQUIRED_PROGRESS.getPlaceholder(), String.valueOf(obj.getRequiredProgression())))
                        .append(Methods.getProgressBar(progress, 20)));
            }
        }
    }

    private static String stateColor(Enum<?> state) {
        return switch (state.name()) {
            case "COMPLETED" -> "<green>";
            case "IN_PROGRESS" -> "<yellow>";
            case "NOT_STARTED" -> "<gray>";
            case "CANCELLED" -> "<red>";
            default -> "<white>";
        };
    }

    private static String formatTimestamp(long epochMillis) {
        return TIMESTAMP_FORMAT.format(Instant.ofEpochMilli(epochMillis));
    }
}
