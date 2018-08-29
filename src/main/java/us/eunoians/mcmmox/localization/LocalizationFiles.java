package us.eunoians.mcmmox.localization;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcmmox.Mcmmox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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
            saveResource(p, "localization/" + key + ".yml", true);
          else
            saveResource(p, "localization/" + key + ".yml", true);
      }
    }
  }

  public void saveResource(Mcmmox m, String resourcePath, boolean replace) {
    if (resourcePath != null && !resourcePath.equals("")) {
      resourcePath = resourcePath.replace('\\', '/');
      InputStream in = m.getResource(resourcePath);
      if (in == null) {
        throw new IllegalArgumentException("The embedded resource \'" + resourcePath + "\' cannot be found");
      } else {
        File outFile = new File(m.getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf(47);
        File outDir = new File(m.getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));
        if (!outDir.exists()) {
          outDir.mkdirs();
        }
        try {
          if (outFile.exists() && !replace) {
            m.getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
          } else {
            FileOutputStream ex = new FileOutputStream(outFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
              ex.write(buf, 0, len);
            }
            ex.close();
            in.close();
          }
        } catch (IOException var10) {
          m.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
        }

      }
    } else {
      throw new IllegalArgumentException("ResourcePath cannot be null or empty");
    }
  }

  private String getPath(String file) {
    return String.format(localePath, file);
  }

  public YamlConfiguration getLocalization(String lang) {
    File lang1 = new File(mcb.getDataFolder() + "/localizations/" + lang + ".yml");
    return YamlConfiguration.loadConfiguration(lang1);
  }

}

