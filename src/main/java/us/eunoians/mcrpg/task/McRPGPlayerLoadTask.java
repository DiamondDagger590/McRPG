package us.eunoians.mcrpg.task;

import com.diamonddagger590.mccore.task.PlayerLoadTask;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.database.table.PlayerLoadoutDAO;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.database.table.SkillDataSnapshot;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * A {@link PlayerLoadTask} that loads McRPG player data
 */
//TODO javadoc
public class McRPGPlayerLoadTask extends PlayerLoadTask {

    public McRPGPlayerLoadTask(@NotNull McRPG plugin, @NotNull McRPGPlayer mcRPGPlayer) {
        super(plugin, mcRPGPlayer);
    }

    @Override
    public McRPGPlayer getCorePlayer() {
        return (McRPGPlayer) super.getCorePlayer();
    }

    @NotNull
    @Override
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    @VisibleForTesting
    @Override
    protected boolean loadPlayer() { //TODO completable future?
        SkillRegistry skillRegistry = getPlugin().getSkillRegistry();
        AbilityRegistry abilityRegistry = getPlugin().getAbilityRegistry();
        AbilityAttributeManager abilityAttributeManager = getPlugin().getAbilityAttributeManager();
        SkillHolder skillHolder = getCorePlayer().asSkillHolder();
        UUID uuid = getCorePlayer().getUUID();
        CompletableFuture futures[] = new CompletableFuture[skillRegistry.getRegisteredSkillKeys().size()];

        Connection connection = getPlugin().getDatabaseManager().getDatabase().getConnection();
        int i = 0;
        for (NamespacedKey skillKey : skillRegistry.getRegisteredSkillKeys()) {
            Skill skill = skillRegistry.getRegisteredSkill(skillKey);

            getPlugin().getLogger().log(Level.INFO, "Loading data for skill: " + skillKey.getKey());

            CompletableFuture<SkillDataSnapshot> future = SkillDAO.getAllPlayerSkillInformation(connection, getCorePlayer().getUUID(), skillKey);
            futures[i] = future;
            i++;
            future.thenAccept(skillDataSnapshot -> {

                getPlugin().getLogger().log(Level.INFO, "Data loaded for skill: " + skillKey.getKey() + " Skill level: " + skillDataSnapshot.getCurrentLevel() + " Skill exp: " + skillDataSnapshot.getCurrentExp());
                skillHolder.addSkillHolderData(skill, skillDataSnapshot.getCurrentLevel(), skillDataSnapshot.getCurrentExp());

                for (NamespacedKey abilityKey : abilityRegistry.getAbilitiesBelongingToSkill(skillKey)) {
                    Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
                    skillHolder.addAvailableAbility(abilityKey);
                    AbilityData abilityData = new AbilityData(abilityKey, skillDataSnapshot.getAbilityAttributes(abilityKey).values());
                    for (NamespacedKey attributeKey : ability.getApplicableAttributes()) {
                        if (!abilityData.hasAttribute(attributeKey)) {
                            abilityAttributeManager.getAttribute(attributeKey).ifPresent(abilityData::addAttribute);
                        }
                    }
                    skillHolder.addAbilityData(abilityData);
                }

                getPlugin().getLogger().log(Level.INFO, "Player abilities are now: "
                        + getCorePlayer().asSkillHolder().getAvailableAbilities().stream()
                        .map(NamespacedKey::getKey).reduce((s, s2) -> s + " " + s2).get());
                getPlugin().getLogger().log(Level.INFO, "Player skills are now: "
                        + getCorePlayer().asSkillHolder().getSkills().stream()
                        .map(NamespacedKey::getKey).reduce((s, s2) -> s + " " + s2).get());
            }).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            });
        }

        int loadoutAmount = McRPG.getInstance().getFileManager().getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_AMOUNT);
        CompletableFuture[] loadoutFutures = new CompletableFuture[loadoutAmount];
        for (int x = 1; x <= loadoutAmount; x++) {
            CompletableFuture<Loadout> future = PlayerLoadoutDAO.getPlayerLoadout(connection, uuid, x);
            loadoutFutures[x - 1] = future;
            future.thenAccept(skillHolder::setLoadout)
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
        }

        CompletableFuture.allOf(ArrayUtils.addAll(futures, loadoutFutures)).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
        return true;
    }

    @VisibleForTesting
    @Override
    protected void onPlayerLoadSuccessfully() {

        // Fire event
        super.onPlayerLoadSuccessfully();

        getPlugin().getLogger().log(Level.INFO, "Player data has been loaded for player: " + getCorePlayer().getUUID());

        //Begin tracking player
        getPlugin().getPlayerManager().addPlayer(getCorePlayer());
        getPlugin().getEntityManager().trackAbilityHolder(getCorePlayer().asSkillHolder());
        getPlugin().getEntityManager().trackQuestHolder(getCorePlayer().asQuestHolder());
    }

    @VisibleForTesting
    @Override
    protected void onPlayerLoadFail() {
        getPlugin().getLogger().log(Level.SEVERE, ChatColor.RED + "There was an issue loading in the McRPG player data for player with UUID: " + getCorePlayer().getUUID());

        Optional<Player> player = getCorePlayer().getAsBukkitPlayer();

        if (player.isPresent() && player.get().isOnline()) {
            player.get().sendMessage(ChatColor.RED + "There was an issue loading your McRPG data, logging back into the server may fix this issue. If that does not fix the issue, please contact a server admin!");
        }
    }

    @Override
    protected void onDelayComplete() {
    }

    @Override
    protected void onIntervalStart() {
    }

    @Override
    protected void onIntervalPause() {
    }

    @Override
    protected void onIntervalResume() {
    }

    @Override
    public void onTaskExpire() {
    }

    @Override
    protected void onCancel() {
    }
}
