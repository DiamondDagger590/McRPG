package us.eunoians.mcmmox.configuration;

import com.cyr1en.mcutils.config.ConfigManager;
import lombok.var;
import us.eunoians.mcmmox.configuration.annotations.Configuration;
import us.eunoians.mcmmox.configuration.enums.Config;
import us.eunoians.mcmmox.configuration.files.GeneralConfig;
import us.eunoians.mcmmox.configuration.files.SwordsConfig;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class MConfigManager {

  private ConfigManager manager;
  private HashMap<Config, BaseConfig> configs;

  public MConfigManager(ConfigManager manager) {
    this.manager = manager;
    this.configs = new HashMap<>();
  }

  @SafeVarargs
  public final boolean setupConfigs(Class<? extends BaseConfig>... classes) {
    var isSafeToStart = true;
    for (Class<? extends BaseConfig> config : classes) {
      if (config.isAnnotationPresent(Configuration.class)) {
        var meta = config.getAnnotation(Configuration.class);
        try {
          BaseConfig baseConfig = meta.path().isEmpty() ?
                  config.getConstructor(ConfigManager.class, String[].class).newInstance(manager, meta.header()) :
                  config.getConstructor(ConfigManager.class, String[].class, String.class).newInstance(manager, meta.header(), meta.path());
          if (!baseConfig.init())
            isSafeToStart = false;
          configs.put(meta.type(), baseConfig);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }
    return isSafeToStart;
  }

  public BaseConfig getConfig(Config config) {
    return configs.get(config);
  }

  public GeneralConfig getGeneralConfig() {
    return (GeneralConfig) getConfig(Config.GENERAL_CONFIG);
  }

  public SwordsConfig getSwordsConfig() {
    return (SwordsConfig) getConfig(Config.SWORDS_CONFIG);
  }

}
