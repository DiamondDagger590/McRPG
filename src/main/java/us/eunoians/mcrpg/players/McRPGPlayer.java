package us.eunoians.mcrpg.players;

import com.cyr1en.flatdb.Database;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.events.mcrpg.axes.CripplingBlowEvent;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.SmitingFistEvent;
import us.eunoians.mcrpg.api.leaderboards.PlayerRank;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.RedeemBit;
import us.eunoians.mcrpg.database.tables.skills.ArcheryDAO;
import us.eunoians.mcrpg.database.tables.skills.AxesDAO;
import us.eunoians.mcrpg.database.tables.skills.ExcavationDAO;
import us.eunoians.mcrpg.database.tables.skills.FishingDAO;
import us.eunoians.mcrpg.database.tables.skills.FitnessDAO;
import us.eunoians.mcrpg.database.tables.skills.HerbalismDAO;
import us.eunoians.mcrpg.database.tables.skills.MiningDAO;
import us.eunoians.mcrpg.database.tables.skills.SkillDataSnapshot;
import us.eunoians.mcrpg.party.AcceptedTeleportRequest;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.party.PartyInvite;
import us.eunoians.mcrpg.party.TeleportRequest;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.AbilityType;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.DisplayType;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.GenericAbility;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.TipType;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class McRPGPlayer {

    @Getter
    private UUID uuid;

    @Getter
    private int powerLevel;
    @Getter
    @Setter
    private int abilityPoints;

    private final List<Skill> skills = new ArrayList<>();

    @Getter
    private final List<UnlockedAbilities> pendingUnlockAbilities = new ArrayList<>();
    private final Map<UnlockedAbilities, Long> abilitiesOnCooldown = new HashMap<>();
    @Getter
    private final List<UnlockedAbilities> abilityLoadout = new ArrayList<>();
    @Getter
    @Setter
    private long endTimeForReplaceCooldown;
    @Getter
    private final List<UnlockedAbilities> activeAbilities = new ArrayList<>();

    @Getter
    @Setter
    private PlayerRank powerRank;
    @Getter
    private final Map<Skills, PlayerRank> skillRanks = new HashMap<>();
    @Getter
    private boolean isLoadingRankData = false;

    //Ability data
    @Getter
    @Setter
    private boolean hasBleedImmunity = false;
    @Getter
    @Setter
    private boolean hasDazeImmunity = false;
    @Setter
    @Getter
    private boolean canSmite;
    @Getter
    @Setter
    private SmitingFistEvent smitingFistData;
    @Getter
    @Setter
    private CripplingBlowEvent cripplingBlowData;
    @Getter
    @Setter
    private boolean isLinkedToRemoteTransfer = false;
    @Getter
    @Setter
    private boolean canDenseImpact;
    @Getter
    @Setter
    private int armourDmg;
    @Getter
    @Setter
    private double divineEscapeExpDebuff;
    @Getter
    @Setter
    private double divineEscapeDamageDebuff;
    @Getter
    @Setter
    private long divineEscapeExpEnd;
    @Getter
    @Setter
    private long divineEscapeDamageEnd;
    @Getter
    @Setter
    private boolean isHandDigging = false;
    @Getter
    @Setter
    private Set<Material> handDiggingBlocks;

    //Ready variables
    @Setter
    private boolean isReadying = false;
    @Getter
    @Setter
    private PlayerReadyBit readyingAbilityBit = null;

    //Settings
    @Getter
    @Setter
    private MobHealthbarUtils.MobHealthbarType healthbarType = MobHealthbarUtils.MobHealthbarType.BAR;
    @Getter
    @Setter
    private boolean keepHandEmpty = false;
    @Getter
    @Setter
    private DisplayType displayType = DisplayType.SCOREBOARD;
    @Getter
    @Setter
    private boolean autoDeny = false;
    @Getter
    @Setter
    private boolean requireEmptyOffHand = false;
    @Getter
    @Setter
    private boolean ignoreTips;
    @Getter
    @Setter
    private int unarmedIgnoreSlot;
    @Getter
    @Setter
    private boolean autoAcceptPartyInvites;

    @Getter
    private Set<TipType> usedTips = new HashSet<>();

    //Redeemable data
    @Getter
    @Setter
    private int redeemableExp;
    @Getter
    @Setter
    private int redeemableLevels;
    @Getter
    @Setter
    private boolean listenForCustomExpInput = false;
    @Getter
    @Setter
    private RedeemBit redeemBit;

    //Guardian Data
    @Getter
    @Setter
    private double guardianSummonChance;
    @Getter
    @Setter
    private Location lastFishCaughtLoc = null;

    //Fitness Data
    @Getter
    private final List<Location> lastFallLocation = new ArrayList<>();

    //Artifact variables
    @Getter
    @Setter
    private long magnetArtifactCooldownTime = 0;
    @Getter
    @Setter
    private long cooldownResetArtifactCooldownTime = 0;

    //mcMMO conversion
    @Getter
    @Setter
    private int boostedExp;

    //party invites
    @Getter
    private LinkedBlockingQueue<PartyInvite> partyInvites = new LinkedBlockingQueue<>();
    @Getter
    @Setter
    private UUID partyID;

    @Getter
    @Setter
    private boolean usePartyChat = false;

    @Getter
    private List<TeleportRequest> teleportRequests = new ArrayList<>();
    @Getter
    private Map<UUID, TeleportRequest> teleportRequestMap = new HashMap<>();

    @Getter
    @Setter
    private boolean spyPartyChat = false;

    /**
     * This represents the teleport request for a player. If null then they don't have a request accepted
     */
    @Getter
    @Setter
    private AcceptedTeleportRequest acceptedTeleportRequest = null;

    public McRPGPlayer(UUID uuid) {
        this.uuid = uuid;
        this.guardianSummonChance = McRPG.getInstance().getConfig().getDouble("PlayerConfiguration.PoseidonsGuardian.DefaultSummonChance");
        Database database = McRPG.getInstance().getDatabaseManager().getDatabase();
        //TODO reimplement using DAO's
        Optional<ResultSet> playerDataSet = database.executeQuery("SELECT * FROM mcrpg_player_data WHERE uuid = '" + uuid.toString() + "'");

        boolean isNew = false;
        try {
            if (playerDataSet.isPresent()) {
                isNew = !playerDataSet.get().next();
            }
            else {
                isNew = true;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        if (isNew) {
            for (Skills type : Skills.values()) {
                String query = "INSERT INTO mcrpg_" + type.getName() + "_data (uuid) VALUES ('" + uuid.toString() + "')";
                database.executeUpdate(query);
            }
            String query = "INSERT INTO MCRPG_PLAYER_SETTINGS (UUID) VALUES ('" + uuid.toString() + "')";
            database.executeUpdate(query);
            query = "INSERT INTO MCRPG_PLAYER_DATA (UUID) VALUES ('" + uuid.toString() + "')";
            database.executeUpdate(query);
            query = "INSERT INTO MCRPG_LOADOUT (UUID) VALUES ('" + uuid.toString() + "')";
            database.executeUpdate(query);
            playerDataSet = database.executeQuery("SELECT * FROM mcrpg_player_data WHERE uuid = '" + uuid.toString() + "'");
            try {
                playerDataSet.get().next();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        playerDataSet.ifPresent(resultSet -> {
            try {
                //if(resultSet.next()) {
                this.abilityPoints = resultSet.getInt("ability_points");
                this.redeemableExp = resultSet.getInt("redeemable_exp");
                this.redeemableLevels = resultSet.getInt("redeemable_levels");
                long replaceCooldown = resultSet.getLong("replace_ability_cooldown_time");
                this.boostedExp = resultSet.getInt("boosted_exp");
                this.divineEscapeExpDebuff = resultSet.getDouble("divine_escape_exp_debuff");
                this.divineEscapeDamageDebuff = resultSet.getDouble("divine_escape_damage_debuff");
                this.divineEscapeExpEnd = resultSet.getLong("divine_escape_exp_end_time");
                this.divineEscapeDamageEnd = resultSet.getLong("divine_escape_damage_end_time");
                String partyIDString = resultSet.getString("party_uuid");
                if (partyIDString.equalsIgnoreCase("nu")) {
                    partyID = null;
                }
                else {
                    partyID = UUID.fromString(partyIDString);
                    Party party = McRPG.getInstance().getPartyManager().getParty(partyID);
                    StringBuilder nullPartyMessage = new StringBuilder();
                    if (party == null) {
                        partyID = null;
                        nullPartyMessage.append("&cYour party no longer exists.");
                    }
                    else {
                        if (!party.isPlayerInParty(uuid)) {
                            partyID = null;
                            nullPartyMessage.append("&cYou were removed from your party whilst offline.");
                        }
                    }
                    if (nullPartyMessage.length() != 0) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                OfflinePlayer offlinePlayer = getOfflineMcRPGPlayer();
                                if (offlinePlayer.isOnline()) {
                                    ((Player) offlinePlayer).sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + nullPartyMessage.toString()));
                                }
                            }
                        }.runTaskLater(McRPG.getInstance(), 2 * 20);
                    }
                }
                Calendar cal1 = Calendar.getInstance();
                Calendar cal = Calendar.getInstance();
                cal1.setTimeInMillis(replaceCooldown);
                if (cal.getTimeInMillis() < cal1.getTimeInMillis()) {
                    this.endTimeForReplaceCooldown = cal1.getTimeInMillis();
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });

        final Optional<ResultSet> settingsSet = database.executeQuery("SELECT * FROM mcrpg_player_settings WHERE uuid = '" + uuid.toString() + "'");
        settingsSet.ifPresent(rs -> {
            try {
                if (rs.next()) {
                    this.healthbarType = MobHealthbarUtils.MobHealthbarType.fromString(rs.getString("health_type"));
                    this.keepHandEmpty = rs.getBoolean("keep_hand");
                    this.displayType = DisplayType.fromString(rs.getString("display_type"));
                    this.autoDeny = rs.getBoolean("auto_deny");
                    this.ignoreTips = rs.getBoolean("ignore_tips");
                    this.requireEmptyOffHand = rs.getBoolean("require_empty_offhand");
                    this.unarmedIgnoreSlot = rs.getInt("unarmed_ignore_slot");
                    this.autoAcceptPartyInvites = rs.getBoolean("auto_accept_party_teleports");
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });

        //Initialize skills
        Connection connection = McRPG.getInstance().getDatabaseManager().getDatabase().getConnection();
        ArcheryDAO.getPlayerArcheryData(connection, uuid).thenAccept(this::initializeSkill);//TODO probably should respect ArcheryDAO#isAcceptingQueries but I don't have some sort of actual mutex handling so we just pretend it's always gonna be true
        AxesDAO.getPlayerAxesData(connection, uuid).thenAccept(this::initializeSkill);
        ExcavationDAO.getPlayerExcavationData(connection, uuid).thenAccept(this::initializeSkill);
        FishingDAO.getPlayerFishingData(connection, uuid).thenAccept(this::initializeSkill);
        FitnessDAO.getPlayerFitnessData(connection, uuid).thenAccept(this::initializeSkill);
        HerbalismDAO.getPlayerHerbalismData(connection, uuid).thenAccept(this::initializeSkill);
        MiningDAO.getPlayerMiningData(connection, uuid).thenAccept(this::initializeSkill);

        final Optional<ResultSet> loadoutSet = database.executeQuery("SELECT * FROM mcrpg_loadout WHERE uuid = '" + uuid.toString() + "'");
        loadoutSet.ifPresent(rs -> {
            try {
                if (rs.next()) {
                    for (int i = 1; i <= McRPG.getInstance().getConfig().getInt("PlayerConfiguration.AmountOfTotalAbilities"); i++) {
                        //It has to be an unlocked ability since default ones cant be in the loadout
                        String s = rs.getString("Slot" + i);
                        if (s == null || s.equalsIgnoreCase("null")) {
                            continue;
                        }
                        UnlockedAbilities ability = UnlockedAbilities.fromString(s);
                        abilityLoadout.add(ability);
                    }
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });

        updatePowerLevel();

        for (Skill skill : skills) {
            skill.updateExpToLevel();
        }

        //Remove any abilities that shouldn't be in the player's loadout
        List<UnlockedAbilities> toRemove = new ArrayList<>();
        for (UnlockedAbilities abilityType : abilityLoadout) {
            BaseAbility baseAbility = getBaseAbility(abilityType);
            if (baseAbility != null && baseAbility.getCurrentTier() < 1) {
                baseAbility.setUnlocked(false);
                toRemove.add(abilityType);
            }
        }

        //Needed to prevent a CME lol
        for (UnlockedAbilities abilityType : toRemove) {
            abilityLoadout.remove(abilityType);
        }
    }

    void initializeSkill(SkillDataSnapshot skillDataSnapshot){

        Skills skillType = skillDataSnapshot.getSkillType();
        Class<? extends Skill> skillClazz = skillType.getClazz();
        Map<GenericAbility, BaseAbility> abilityMap = new HashMap<>();
        int currentExp = skillDataSnapshot.getCurrentExp();
        int currentLevel = skillDataSnapshot.getCurrentLevel();

        try {
            for (GenericAbility genericAbility : skillType.getAllAbilities()) {

                BaseAbility abilityInstance;
                Class<? extends BaseAbility> abilityClazz = genericAbility.getClazz();

                boolean toggled = skillDataSnapshot.getAbilityToggledMap().getOrDefault(genericAbility, true);

                //Unlocked abilities have special data so we need to deal with those
                if (genericAbility instanceof UnlockedAbilities unlockedAbility) {

                    int tier = skillDataSnapshot.getAbilityTiers().getOrDefault(genericAbility, 0);

                    //Remote transfer needs Jesus
                    if (genericAbility.equals(UnlockedAbilities.REMOTE_TRANSFER)) { // yes, i know this is quirky. deal with it. this ability should be re-worked in the future anyways -Jared (Thx Jared -Coleman)
                        abilityInstance = abilityClazz.getConstructor(UUID.class, boolean.class, int.class)
                                .newInstance(uuid, toggled, tier);

                        this.isLinkedToRemoteTransfer = ((RemoteTransfer) abilityInstance).isAbilityLinked();
                    }
                    //Any other non-Jesus needing ability
                    else {
                        abilityInstance = abilityClazz.getConstructor(boolean.class, int.class)
                                .newInstance(toggled, tier);
                    }

                    //If it can be unlocked, we need to see if it needs to be put as currently pending
                    if (skillDataSnapshot.getPendingAbilities().getOrDefault(genericAbility, false)) {
                        pendingUnlockAbilities.add(unlockedAbility);
                    }

                    //Unlocked abilities can be put on cooldown, so check here
                    if (genericAbility.isCooldown()) { //Deal with cooldowns (I hate this and this will be redone)

                        int cooldown = skillDataSnapshot.getAbilityCooldowns().getOrDefault(genericAbility, 0);

                        if (cooldown > 0) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.SECOND, cooldown);

                            abilitiesOnCooldown.put(unlockedAbility, calendar.getTimeInMillis());
                        }
                    }
                }
                //We know it's a default ability
                else {
                    abilityInstance = abilityClazz.getConstructor(boolean.class)
                            .newInstance(toggled);
                }

                //Put the created ability into the map
                abilityMap.put(genericAbility, abilityInstance);
            }

            //Initialize the skill (We need a better way of doing this lmfao, maybe look at spawning an entity in bukkit?)
            Skill skillInstance = skillClazz.getConstructor(int.class, int.class, Map.class, McRPGPlayer.class)
                    .newInstance(currentLevel, currentExp, abilityMap, this);

            skills.add(skillInstance);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
            exception.printStackTrace();
        }
    }

    public OfflinePlayer getOfflineMcRPGPlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    @Deprecated
    public OfflinePlayer getOfflineMcMMOPlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public boolean isReadying() {
        if (this.readyingAbilityBit == null || this.readyingAbilityBit.getAbilityReady() == null) {
            this.isReadying = false;
        }
        return isReadying;
    }

    /**
     * Updates the power level of the player by adding together all of the levels of each skill
     *
     * @return The power level of the player
     */
    public int updatePowerLevel() {
        if (skills.isEmpty()) {
            powerLevel = 0;
        }
        else {
            final AtomicInteger powerLevelUpdater = new AtomicInteger(0);
            skills.forEach(skill -> powerLevelUpdater.addAndGet(skill.getCurrentLevel()));
            this.powerLevel = powerLevelUpdater.get();
        }
        return powerLevel;
    }

    /**
     * Get the instance of the players skill
     *
     * @param skill The skill you want to get an instance of. Will return null if the skill doesnt exist
     * @return The instance of the players skill of the type provided
     */
    public Skill getSkill(String skill) {
        return skills.stream().filter(n -> n.getName().equalsIgnoreCase(skill)).findAny().orElse(null);
    }

    /**
     * Get the instance of the players skill
     *
     * @param skill The skill you want to get an instance of. Will return null if the skill doesnt exist. Good lucky getting null out of this xD
     * @return The instance of the players skill of the type provided
     */
    public Skill getSkill(Skills skill) {
        return skills.stream().filter(n -> n.getType().equals(skill)).findFirst().orElse(null);
    }

    /**
     * @param ability The GenericAbility enum value you are searching for
     * @return The BaseAbility of the provided enum value
     */
    public BaseAbility getBaseAbility(GenericAbility ability) {
        return ability != null && getSkill(ability.getSkill()) != null ? getSkill(ability.getSkill()).getAbility(ability) : null;
    }

    public void giveExp(Skills skill, int exp, GainReason reason) {
        getSkill(skill).giveExp(this, exp, reason);
    }

    /**
     * Get the cooldown of an ability.
     *
     * @param ability The ability type you want to check the cooldown for
     * @return The endtime of the cooldown in milis. If the cooldown doesnt exist return -1
     */
    public long getCooldown(GenericAbility ability) {

        if (abilitiesOnCooldown.containsKey(ability)) {
            return TimeUnit.MILLISECONDS.toSeconds(abilitiesOnCooldown.get(ability) - Calendar.getInstance().getTimeInMillis());
        }
        else {
            return -1;
        }
    }

    /**
     * Get the cooldown of an ability (this works since a skill can only have one active ability unlocked
     *
     * @param skill The skill to check
     * @return The time to end in millis or -1 if it doesnt exist
     */
    public long getCooldown(Skills skill) {
        for (UnlockedAbilities ab : abilitiesOnCooldown.keySet()) {
            if (ab.getSkill().equals(skill)) {
                return TimeUnit.MILLISECONDS.toSeconds(abilitiesOnCooldown.get(ab) - Calendar.getInstance().getTimeInMillis());
            }
        }
        return -1;
    }

    /**
     * @param ability   Ability to add on cooldown
     * @param timeToEnd The end time in milis
     */
    public void addAbilityOnCooldown(UnlockedAbilities ability, long timeToEnd) {
        abilitiesOnCooldown.put(ability, timeToEnd);
    }

    /**
     * @param ability Ability to remove from cooldows
     */
    public void removeAbilityOnCooldown(UnlockedAbilities ability) {
        abilitiesOnCooldown.replace(ability, 0L);
    }

    public void removeAbilityOnCooldown(Skills skill) {
        UnlockedAbilities remove = null;
        for (UnlockedAbilities ab : abilitiesOnCooldown.keySet()) {
            if (ab.getSkill().equals(skill)) {
                remove = ab;
                break;
            }
        }
        if (remove != null) {
            abilitiesOnCooldown.remove(remove);
        }
    }

    /**
     * Update all the cooldowns and verify if they are valid
     */
    public void updateCooldowns() {
        ArrayList<UnlockedAbilities> toRemove = new ArrayList<>();
        if (abilitiesOnCooldown.isEmpty() && endTimeForReplaceCooldown == 0) {
            return;
        }
        for (UnlockedAbilities ability : abilitiesOnCooldown.keySet()) {
            if (!abilityLoadout.contains(ability)) {
                toRemove.add(ability);
                continue;
            }
            long timeToEnd = abilitiesOnCooldown.get(ability);
            if (Calendar.getInstance().getTimeInMillis() >= timeToEnd) {
                if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                    this.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                               McRPG.getInstance().getLangFile().getString("Messages.Players.CooldownExpire").replace("%Ability%", ability.getName())));
                }
                toRemove.add(ability);
            }
            else if (timeToEnd <= 0L) {
                toRemove.add(ability);
            }
        }
        Database database = McRPG.getInstance().getDatabaseManager().getDatabase();
        if (!toRemove.isEmpty()) {
            for (UnlockedAbilities ab : toRemove) {
                database.executeUpdate("UPDATE mcrpg_" + ab.getSkill().getName().toLowerCase() + "_data SET "
                                       + Methods.convertNameToSQL(ab.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_cooldown = 0 WHERE uuid = '" + uuid.toString() + "'");
                abilitiesOnCooldown.remove(ab);
            }
        }
        if (endTimeForReplaceCooldown != 0 && Calendar.getInstance().getTimeInMillis() >= endTimeForReplaceCooldown) {
            this.endTimeForReplaceCooldown = 0;
            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                this.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                           McRPG.getInstance().getLangFile().getString("Messages.Players.ReplaceCooldownExpire")));
            }
            database.executeUpdate("UPDATE mcrpg_player_data SET replace_ability_cooldown_time = 0 WHERE uuid = '" + uuid.toString() + "'");
        }
        if (divineEscapeExpEnd != 0 && divineEscapeExpEnd <= Calendar.getInstance().getTimeInMillis()) {
            divineEscapeExpEnd = 0;
            divineEscapeExpDebuff = 0;
            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                getPlayer().sendMessage(Methods.color(getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.DivineEscape.ExpDebuffExpire")));
            }
        }
        if (divineEscapeDamageEnd != 0 && divineEscapeDamageEnd <= Calendar.getInstance().getTimeInMillis()) {
            divineEscapeDamageEnd = 0;
            divineEscapeDamageDebuff = 0;
            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                getPlayer().sendMessage(Methods.color(getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.DivineEscape.DamageDebuffExpire")));
            }
        }
    }

    /**
     * Reset all cooldowns to be 0
     */
    public void resetCooldowns() {
        Database database = McRPG.getInstance().getDatabaseManager().getDatabase();
        for (UnlockedAbilities ability : abilitiesOnCooldown.keySet()) {
            long timeToEnd = abilitiesOnCooldown.get(ability);
            if (Calendar.getInstance().getTimeInMillis() >= timeToEnd) {
                if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                    this.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                               McRPG.getInstance().getLangFile().getString("Messages.Players.CooldownExpire").replace("%Ability%", ability.getName())));
                }
                database.executeUpdate("UPDATE mcrpg_" + ability.getSkill().getName().toLowerCase() + "_data SET " + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus"))
                                       + "_cooldown = 0 WHERE uuid = '" + uuid.toString() + "'");

            }
        }
        abilitiesOnCooldown.clear();
        endTimeForReplaceCooldown = 0;
        database.executeUpdate("UPDATE mcrpg_player_data SET replace_ability_cooldown_time = 0 WHERE uuid = `" + uuid.toString() + "`");
        if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
            this.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                       McRPG.getInstance().getLangFile().getString("Messages.Players.ReplaceCooldownExpire")));
        }
    }

    /**
     * Save players data
     */
    public void saveData() {
        Database database = McRPG.getInstance().getDatabaseManager().getDatabase();
        for (Skills type : Skills.values()) {
            Skill skill = getSkill(type);
            String query = "UPDATE mcrpg_" + skill.getName().toLowerCase() + "_data SET current_level = " + skill.getCurrentLevel() + ", current_exp = " + skill.getCurrentExp();
            for (GenericAbility ability : skill.getAbilityKeys()) {
                if (ability instanceof DefaultAbilities) {
                    query += ", is_" + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_toggled = " + Methods.convertBool(skill.getAbility(ability).isToggled());
                }
                if (ability instanceof UnlockedAbilities) {
                    query += ", is_" + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_toggled = " + Methods.convertBool(skill.getAbility(ability).isToggled());
                    query += ", is_" + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_pending = " + Methods.convertBool(pendingUnlockAbilities.contains(ability));
                    query += ", " + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_tier = " + skill.getAbility(ability).getCurrentTier();
                }
                Calendar cal = Calendar.getInstance();
                if (abilitiesOnCooldown.containsKey(ability)) {
                    Calendar temp = Calendar.getInstance();
                    temp.setTimeInMillis(abilitiesOnCooldown.get(ability));
                    int seconds = (int) (temp.getTimeInMillis() - cal.getTimeInMillis()) / 1000;
                    query += ", " + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_cooldown = " + seconds;
                }
            }
            query += " WHERE uuid = '" + this.uuid.toString() + "'";
            database.executeUpdate(query);
        }
        if (endTimeForReplaceCooldown != 0) {
            database.executeUpdate("UPDATE mcrpg_player_data SET replace_ability_cooldown_time = " + endTimeForReplaceCooldown + " WHERE uuid = '" + uuid.toString() + "'");
        }
        database.executeUpdate("UPDATE mcrpg_player_data SET ability_points = " + abilityPoints + ", power_level = " + powerLevel + ", redeemable_exp = " + redeemableExp + ", redeemable_levels = " + redeemableLevels + ", boosted_exp = " + boostedExp + ", divine_escape_exp_debuff = " + divineEscapeExpDebuff
                               + ", divine_escape_damage_debuff = " + divineEscapeDamageDebuff + ", divine_escape_exp_end_time = " + divineEscapeExpEnd +
                               ", divine_escape_damage_end_time = " + divineEscapeDamageEnd + ", party_uuid = '" + (partyID == null ? "nu" : partyID.toString()) + "' WHERE uuid = '" + uuid.toString() + "'");
        String query = "UPDATE mcrpg_player_settings SET require_empty_offhand = " + Methods.convertBool(requireEmptyOffHand) + ", keep_hand = " + Methods.convertBool(keepHandEmpty)
                       + ", ignore_tips = " + Methods.convertBool(ignoreTips) + ", auto_deny = " + Methods.convertBool(autoDeny) + ", display_type = '" + displayType.getName() +
                       "', health_type = '" + healthbarType.getName() + "', unarmed_ignore_slot = " + unarmedIgnoreSlot + ", auto_accept_party_teleports = "
                       + Methods.convertBool(autoAcceptPartyInvites) + " WHERE uuid = '" + uuid.toString() + "'";
        database.executeUpdate(query);
        for (UnlockedAbilities ability : pendingUnlockAbilities) {
            query = "UPDATE mcrpg_" + ability.getSkill().getName().toLowerCase() + "_data SET is_" + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_pending = 1" +
                    " WHERE uuid = '" + uuid.toString() + "'";
            database.executeUpdate(query);
        }
        String loadoutQuery = "UPDATE mcrpg_loadout SET";
        for (int i = 1; i <= McRPG.getInstance().getConfig().getInt("PlayerConfiguration.AmountOfTotalAbilities"); i++) {
            if (i != 1) {
                loadoutQuery += ",";
            }
            String toAdd = "null";
            loadoutQuery += " Slot" + i + " = '" + (abilityLoadout.size() >= i ? abilityLoadout.get(i - 1).getName() : toAdd) + "'";

        }
        loadoutQuery += " WHERE uuid = '" + uuid.toString() + "'";
        database.executeUpdate(loadoutQuery);

        RemoteTransfer transfer = (RemoteTransfer) getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER);
        if (transfer.isUnlocked()) {
            File remoteTransferFile = new File(McRPG.getInstance().getDataFolder(), File.separator + "remote_transfer_data" + File.separator + uuid.toString() + ".yml");
            FileConfiguration data = YamlConfiguration.loadConfiguration(remoteTransferFile);
            for (Material mat : transfer.getItemsToSync().keySet()) {
                data.set("RemoteTransferBlocks." + mat.toString(), transfer.getItemsToSync().get(mat));
            }
            try {
                data.save(remoteTransferFile);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param abilities The ability to add to the pending list
     */
    public void addPendingAbilityUnlock(UnlockedAbilities abilities) {
        this.pendingUnlockAbilities.add(abilities);
    }

    /**
     * @param abilities The ability to remove from the pending list
     */
    public void removePendingAbilityUnlock(UnlockedAbilities abilities) {
        this.pendingUnlockAbilities.remove(abilities);
    }

    /**
     * @return true if the player has a pending ability and false if not
     */
    public boolean hasPendingAbility() {
        return !this.pendingUnlockAbilities.isEmpty();
    }

    /**
     * @return true if player is online false if not
     */
    public boolean isOnline() {
        return Bukkit.getOfflinePlayer(uuid).isOnline();
    }

    /**
     * @return Player instance of the mcrpg player. We dont safe check if they are online here
     */
    public Player getPlayer() {
        return (Player) Bukkit.getOfflinePlayer(uuid);
    }

    public boolean isPlayerOnline() {
        return Bukkit.getOfflinePlayer(uuid).isOnline();
    }

    /**
     * @param ability Ability to add to the loadout
     */
    public void addAbilityToLoadout(UnlockedAbilities ability) {
        abilityLoadout.add(ability);
        saveData();
    }

    /**
     * @param ability Ability to check for
     * @return true if the player has the ability in their loadout, false if not
     */
    public boolean doesPlayerHaveAbilityInLoadout(UnlockedAbilities ability) {
        return abilityLoadout.stream().filter(ability1 -> ability1.getName().equalsIgnoreCase(ability.getName())).findFirst().orElse(null) != null;
    }

    /**
     * @param skill The skill to check if they have an active ability for
     * @return true if the player has an active ability, false if not
     */
    public boolean doesPlayerHaveActiveAbilityFromSkill(Skills skill) {
        return abilityLoadout.stream().filter(ability -> ability.getSkill().equals(skill))
                       .filter(ability -> ability.getAbilityType() == AbilityType.ACTIVE).findFirst().orElse(null) != null;
    }

    /**
     * @param skill Skill to get the ability for
     * @return The UnlockedAbilities instance of the active ability belonging to the provided skill a player has, or null if they dont have any
     */
    public UnlockedAbilities getActiveAbilityForSkill(Skills skill) {
        return abilityLoadout.stream().filter(ability -> ability.getSkill().equals(skill))
                .filter(ability -> ability.getAbilityType() == AbilityType.ACTIVE).findFirst().orElse(null);
    }

    /**
     * @param oldAbility Old ability to be replaced
     * @param newAbility Ability to replace with
     */
    public void replaceAbility(UnlockedAbilities oldAbility, UnlockedAbilities newAbility) {
        for (int i = 0; i < abilityLoadout.size(); i++) {
            if (abilityLoadout.get(i).equals(oldAbility)) {
                abilityLoadout.set(i, newAbility);
                return;
            }
        }
    }

    public void giveRedeemableExp(int exp) {
        this.redeemableExp += exp;
    }

    public void giveRedeemableLevels(int levels) {
        this.redeemableLevels += levels;
    }

    public void addTeleportRequest(TeleportRequest teleportRequest) {
        teleportRequests.add(teleportRequest);
        teleportRequestMap.put(teleportRequest.getSender(), teleportRequest);
    }

    public void emptyTeleportRequests() {
        teleportRequestMap = new HashMap<>();
        teleportRequests = new ArrayList<>();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof McRPGPlayer) {
            return uuid.equals(((McRPGPlayer) object).getUuid());
        }
        else if (object instanceof Player) {
            return uuid.equals(((Player) object).getUniqueId());
        }
        else if (object instanceof UUID) {
            return uuid.equals((object));
        }
        return false;
    }
}