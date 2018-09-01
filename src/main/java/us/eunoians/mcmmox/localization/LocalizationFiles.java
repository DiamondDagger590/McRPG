package us.eunoians.mcmmox.localization;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.util.IOUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LocalizationFiles {

  private final String localePath = "/localization/%s";
  @Getter
  private Map<String, File> languages;
  private Mcmmox mcb;

  public LocalizationFiles(Mcmmox p, boolean copy) {
    mcb = p;
    languages = new HashMap<>();
    languages.put("en", new File(p.getDataFolder().toString() + getPath("en.yml")));
    languages.put("custom", new File(p.getDataFolder().toString() + getPath("custom.yml")));
    for (String key : languages.keySet()) {
      if (copy) {
        if (key.equals("custom"))
          if (!languages.get(key).exists())
            IOUtil.saveResource(p, "localization/" + key + ".yml", true);
          else
            IOUtil.saveResource(p, "localization/" + key + ".yml", true);

      }
    }
  }

  private void saveResource(Mcmmox m, String resourcePath, boolean replace) {
    IOUtil.saveResource(m, resourcePath, replace);
  }

  private String getPath(String file) {
    return String.format(localePath, file);
  }

  public YamlConfiguration getLocalization(String lang) {
    File lang1 = new File(mcb.getDataFolder() + "/localizations/" + lang + ".yml");
    return YamlConfiguration.loadConfiguration(lang1);
  }

}

