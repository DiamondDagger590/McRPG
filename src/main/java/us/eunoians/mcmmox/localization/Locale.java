package us.eunoians.mcmmox.localization;

import com.cyr1en.mcutils.logger.Logger;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcmmox.configuration.MConfigManager;

import java.io.File;

public class Locale {

  private static MConfigManager configsManager;

  public static void init(MConfigManager config) {
    Locale.configsManager = config;
  }

  private static YamlConfiguration getLocalization() {
    var loc = configsManager.getGeneralConfig().getLocale();
    var f = new File(Bukkit.getPluginManager().getPlugin("mcmmox").getDataFolder() + "/localizations/" + loc + ".yml");
    if (!f.exists())
      f = new File(Bukkit.getPluginManager().getPlugin("mcmmox").getDataFolder() + "localizations/en.yml");
    return YamlConfiguration.loadConfiguration(f);
  }

  private static String getTranslatedMessage(String messagePath) {
    var s = getLocalization().getString(messagePath);
    if (s == null) {
      Logger.warn("Can not get localization for " + messagePath + ". Returned path");
      return messagePath;
    }
    return getLocalization().getString(messagePath);
  }

  public static Formatter getCommandMessage(String path) {
    return new Formatter(getTranslatedMessage("command." + path));
  }

  public static class Formatter {
    private String message;

    Formatter(String message) {
      this.message = message;
    }

    public String format(Object... objects) {
      return String.format(message, objects);
    }

    public String finish() {
      return message;
    }
  }
}