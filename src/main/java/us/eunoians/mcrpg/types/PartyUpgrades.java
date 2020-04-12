package us.eunoians.mcrpg.types;

import lombok.Getter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;

import java.util.Arrays;

public enum PartyUpgrades{
  
  MEMBER_COUNT("Member Count"),
  EXP_SHARE_RANGE("Exp Share Range"),
  EXP_SHARE_AMOUNT("Exp Share Amount"),
  PRIVATE_BANK_SIZE("Private Bank Size");
  
  @Getter
  private String name;
  
  PartyUpgrades(String name){
    this.name = name;
  }
  
  /**
   * This method allows you to get an instance of the enum from a string
   *
   * @param name The string value of the upgrade you want
   * @return The enum representation of the string provided or null if there is not one
   */
  public static PartyUpgrades getPartyUpgrades(String name){
    return Arrays.stream(values()).filter(perm -> perm.getName().replace(" ", "").equalsIgnoreCase(name)).findFirst().orElse(null);
  }
  
  /**
   * This method is a generic lookup for the max tier for an upgrade
   *
   * @param partyUpgrade The upgrade to lookup the max tier for
   * @return The max upgrade tier if it is a valid upgrade or 0 if not
   */
  public static int getMaxTier(PartyUpgrades partyUpgrade){
    switch(partyUpgrade){
      case MEMBER_COUNT:
        return getMaxMemberUpgradeTier();
      case EXP_SHARE_RANGE:
        return getMaxExpRangeTier();
      case EXP_SHARE_AMOUNT:
        return getMaxExpShareTier();
      case PRIVATE_BANK_SIZE:
        return getMaxPrivateBankUpgradeTier();
    }
    return 0;
  }
  
  /**
   * @param tier The tier that you want to check the member count at
   * @return The member count at the tier provided. If the tier is not defined, 5 will be returned
   */
  public static int getMemberCountAtTier(int tier){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getInt("MemberCountUpgrade.MembersPerTier." + tier, 5);
  }
  
  /**
   * Use this method to get the max tier that a party can upgrade the member count upgrade to
   *
   * @return The integer representing the max upgrade tier. If the setting is missing, 5 is returned
   */
  public static int getMaxMemberUpgradeTier(){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getInt("MemberCountUpgrade.MaxTier", 5);
  }
  
  /**
   * @param tier The tier that you want to check the private bank size at
   * @return The private bank size at the tier provided. If the tier is not defined, 5 will be returned
   */
  public static int getPrivateBankSizeAtTier(int tier){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getInt("PrivateBankSize.SlotsPerTier." + tier, 5);
  }
  
  /**
   * Use this method to get the max tier that a party can upgrade the private bank size upgrade to
   *
   * @return The integer representing the max upgrade tier. If the setting is missing, 5 is returned
   */
  public static int getMaxPrivateBankUpgradeTier(){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getInt("PrivateBankSize.MaxTier", 5);
  }
  
  /**
   * @param tier The tier that you want to check the exp share range at
   * @return The range that exp can be should be shared to other players, or if the tier is not found, 10.0 will be returned
   */
  public static double getExpShareRangeAtTier(int tier){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getDouble("ExpShareRange.RangePerTier." + tier, 10.0);
  }
  
  /**
   * Use this method to get the max tier that a party can upgrade the share range upgrade to
   *
   * @return The integer representing the max upgrade tier. If the setting is missing, 5 is returned
   */
  public static int getMaxExpRangeTier(){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getInt("ExpShareRange.MaxTier", 5);
  }
  
  /**
   * @param tier The tier that you want to check the exp share percentage at
   * @return The percentage that should be shared to other players, or if the tier is not found, 5.0 will be returned
   */
  public static double getExpShareAmountAtTier(int tier){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getDouble("ExpSharePercent.ShareAmountPerTier." + tier, 5.0);
  }
  
  /**
   * Use this method to get the max tier that a party can upgrade the share percent upgrade to
   *
   * @return The integer representing the max upgrade tier. If the setting is missing, 5 is returned
   */
  public static int getMaxExpShareTier(){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getInt("ExpSharePercent.MaxTier", 5);
  }
  
  /**
   * Use this method to get the amount of exp that a player should keep when they are sharing exp
   *
   * @return The percentage that a player will keep when sharing exp. This number needs to be divided by 100 for proper multiplication. If the setting is missiong, 70 is returned
   */
  public static double getExpHolderPercent(){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getDouble("ExpSharePercent.PercentGivenToEarner", 70.0);
  }
}
