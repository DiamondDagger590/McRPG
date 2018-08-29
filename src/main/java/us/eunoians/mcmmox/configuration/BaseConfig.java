package us.eunoians.mcmmox.configuration;

import com.cyr1en.mcutils.config.Config;
import com.cyr1en.mcutils.config.ConfigManager;
import com.cyr1en.mcutils.logger.Logger;
import lombok.Getter;
import lombok.var;
import us.eunoians.mcmmox.api.configuration.IConfig;
import us.eunoians.mcmmox.api.configuration.Node;

import java.io.File;
import java.util.List;

public abstract class BaseConfig implements IConfig {

    @Getter private final String[] header;
    @Getter private final String path;
    private ConfigManager configManager;
    @Getter protected Config config;

    public BaseConfig(ConfigManager configManager, String[] header) {
        this(configManager, header, "");
    }

    public BaseConfig(ConfigManager configManager, String[] header, String path) {
        this.configManager = configManager;
        this.header = header;
        this.path = path.isEmpty() ? this.getClass().getSimpleName() : path.replaceAll(".yml", "");
    }

    protected void initNode(Node node) {
        var comment = node.getComment();
        if (config.get(node.key()) == null) {
            if(node.getDefaultValue() instanceof Node[]) {
                Node[] children = (Node[]) node.getDefaultValue();
                for (Node child : children) {
                    config.set(node.key() + "." + child.key(), child.getDefaultValue());
                }
            } else {
                config.set(node.key(), node.getDefaultValue(), comment);
            }
            config.saveConfig();
        }
    }

    boolean init() {
        File f = configManager.getConfigFile(path + ".yml");
        if (!f.exists()) {
            Logger.warn(this.getClass().getSimpleName() + ".yml" + " have been generated or new fields have been added. " +
                    "Please make sure to fill in all config fields correctly. Server will be stopped for safety.");
            config = configManager.getNewConfig(path + ".yml", header);
            initialize();
            return false;
        }
        config = configManager.getNewConfig(this.getClass().getSimpleName() + ".yml", header);
        initialize();
        return true;
    }

    @Override
    public String getString(Node node) {
        return config.getString(node.key());
    }

    @Override
    public boolean getBoolean(Node node) {
        return config.getBoolean(node.key());
    }

    @Override
    public int getInt(Node node) {
        return config.getInt(node.key());
    }

    @Override
    public List getList(Node node) {
        return config.getList(node.key());
    }

    @Override
    public double getDouble(Node node){ return config.getDouble(node.key());}

    public abstract void initialize();

}
