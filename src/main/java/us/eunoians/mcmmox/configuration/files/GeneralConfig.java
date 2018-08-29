package us.eunoians.mcmmox.configuration.files;

import com.cyr1en.mcutils.config.ConfigManager;
import lombok.Getter;
import us.eunoians.mcmmox.api.configuration.Node;
import us.eunoians.mcmmox.configuration.BaseConfig;
import us.eunoians.mcmmox.configuration.annotations.Configuration;
import us.eunoians.mcmmox.configuration.enums.Config;

@Configuration(type = Config.GENERAL_CONFIG, header = {"General Mcmmox configuration"})
public class GeneralConfig extends BaseConfig {

    public GeneralConfig(ConfigManager configManager, String[] header) {
        super(configManager, header);
    }

    @Override
    public void initialize() {
        for (GenConfigNode node : GenConfigNode.values())
            initNode(node);
    }

    public boolean isAutoUpdate() {
        return getBoolean(GenConfigNode.AUTO_UPDATE);
    }

    public boolean isDebugMode() {
        return getBoolean(GenConfigNode.DEBUG_MODE);
    }

    public String getLocale() {
        return getString(GenConfigNode.LOCALE);
    }

    enum GenConfigNode implements Node {
        AUTO_UPDATE("Auto_Update", new String[]{"Do you want Mcmmox to", "update automatically."}, true),
        LOCALE("Localization", new String[]{"What language do you want", "Mcmmox to be in?"}, true),
        DEBUG_MODE("Debug_Mode", new String[]{"Enable debug logging for troubleshooting"}, true),
        SAMPLE("Sample", new String[]{""}, SampleChildren.values());

        @Getter private String key;
        @Getter private String[] comment;
        @Getter private Object defaultValue;

        GenConfigNode(String key, String[] comment, Object defaultValue) {
            this.key = key;
            this.comment = comment;
            this.defaultValue = defaultValue;
        }

        public String key() {
            return key;
        }
    }

    enum SampleChildren implements Node {
        TEST_1("Test_1", new String[]{""}, true),
        TEST_2("Test_2", new String[]{""}, true);

        @Getter private String key;
        @Getter private String[] comment;
        @Getter private Object defaultValue;

        SampleChildren(String key, String[] comment, Object defaultValue) {
            this.key = key;
            this.comment = comment;
            this.defaultValue = defaultValue;
        }

        public String key() {
            return this.key;
        }
    }
}

