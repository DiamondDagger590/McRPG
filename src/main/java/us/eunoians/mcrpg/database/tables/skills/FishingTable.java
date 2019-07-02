package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "fishing_data")
public class FishingTable {


    @Column(autoIncrement = true) private int id;
    @Column(primaryKey = true) private String uuid;

    @Column(defaultValue = "0") private int current_exp;
    @Column(defaultValue = "0") private int current_level;

    @Column(defaultValue = "1") private boolean is_great_rod_toggled;
    @Column(defaultValue = "1") private boolean is_poseidons_favor_toggled;
    @Column(defaultValue = "1") private boolean is_magic_touch_toggled;
    @Column(defaultValue = "1") private boolean is_sea_gods_blessing_toggled;
    @Column(defaultValue = "1") private boolean is_sunken_armory_toggled;
    @Column(defaultValue = "1") private boolean is_shake_toggled;
    @Column(defaultValue = "1") private boolean is_super_rod_toggled;

    @Column(defaultValue = "0") private int poseidons_favor_tier;
    @Column(defaultValue = "0") private int magic_touch_tier;
    @Column(defaultValue = "0") private int sea_gods_blessing_tier;
    @Column(defaultValue = "0") private int sunken_armory_tier;
    @Column(defaultValue = "0") private int shake_tier;
    @Column(defaultValue = "0") private int super_rod_tier;

    @Column(defaultValue = "0") private boolean is_poseidons_favor_pending;
    @Column(defaultValue = "0") private boolean is_magic_touch_pending;
    @Column(defaultValue = "0") private boolean is_sea_gods_blessing_pending;
    @Column(defaultValue = "0") private boolean is_sunken_armory_pending;
    @Column(defaultValue = "0") private boolean is_shake_pending;
    @Column(defaultValue = "0") private boolean is_super_rod_pending;
}
