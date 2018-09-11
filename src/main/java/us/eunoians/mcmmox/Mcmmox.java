package us.eunoians.mcmmox;

import com.cyr1en.mcutils.PluginUpdater;
import com.cyr1en.mcutils.config.ConfigManager;
import com.cyr1en.mcutils.logger.Logger;
import com.cyr1en.mcutils.utils.reflection.Initializable;
import com.cyr1en.mcutils.utils.reflection.annotation.Initialize;
import com.cyr1en.mcutils.utils.reflection.annotation.process.Initializer;
import lombok.Getter;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.commands.McMMOStub;
import us.eunoians.mcmmox.configuration.MConfigManager;
import us.eunoians.mcmmox.configuration.files.GeneralConfig;
import us.eunoians.mcmmox.configuration.files.SwordsConfig;
import us.eunoians.mcmmox.events.mcmmo.AbilityActivate;
import us.eunoians.mcmmox.events.mcmmo.McMMOExpGain;
import us.eunoians.mcmmox.events.mcmmo.McMMOPlayerLevelChange;
import us.eunoians.mcmmox.events.vanilla.*;
import us.eunoians.mcmmox.localization.Locale;
import us.eunoians.mcmmox.localization.LocalizationFiles;
import us.eunoians.mcmmox.players.PlayerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class Mcmmox extends JavaPlugin implements Initializable {

	private static Mcmmox instance;
	@Getter
	private MConfigManager mConfigManager;
	@Getter
	private PluginUpdater pluginUpdater;
	@Getter
	private LocalizationFiles localizationFiles;
	@Getter
	private FileManager fileManager;

	@Override
	public void onEnable(){
		Bukkit.getScheduler().runTaskLater(this, () -> Initializer.initAll(this), 1L);
	}

	@Override
	public void onDisable(){
		if(!Initializer.finished())
			Initializer.interrupt();
	}

	@Initialize(priority = 0)
	private void preInit(){
		var configManager = new ConfigManager(this);
		mConfigManager = new MConfigManager(configManager);
		if(!mConfigManager.setupConfigs(
			GeneralConfig.class, SwordsConfig.class))
			getServer().shutdown();
		fileManager = FileManager.getInstance().setup(this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerLoginEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new MoveEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new InvClickEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new AbilityActivate(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new McMMOPlayerLevelChange(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new VanillaDamageEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new McMMOExpGain(), this);
		InvClickEvent.setConfig(this);
		Bukkit.getServer().getPluginManager().registerEvents(new InvCloseEvent(), this);
	  	PlayerManager.startSave(this);
		Logger.init("McMMOX");
		Logger.setDebugMode(mConfigManager.getGeneralConfig().isDebugMode());
		Locale.init(mConfigManager);
	}

	// @Initialize(priority = 1)
	// Ignore sanity while in development
	private void sanity(){
		if(ProxySelector.getDefault() == null){
			ProxySelector.setDefault(new ProxySelector() {
				private final List<Proxy> DIRECT_CONNECTION = Collections.unmodifiableList(Collections.singletonList(Proxy.NO_PROXY));

				public void connectFailed(URI arg0, SocketAddress arg1, IOException arg2){
				}

				public List<Proxy> select(URI uri){
					return DIRECT_CONNECTION;
				}
			});
		}
		pluginUpdater = new PluginUpdater(this, "https://contents.cyr1en.com/mcmmox/plinfo");
		pluginUpdater.setOut(true);
		if(mConfigManager.getGeneralConfig().isAutoUpdate()){
			if(pluginUpdater.needsUpdate())
				pluginUpdater.update();
			else
				Logger.info("No updates were found!");
		}
		else{
			Logger.info("New version of McMMOX is available: " + pluginUpdater.getVersion());
			Logger.info("Click to download new version: " + pluginUpdater.getDownloadURL());
		}
	}

	@Initialize(priority = 2)
	private void initPrimaryInstance(){
		localizationFiles = new LocalizationFiles(this, true);
		instance = this;
	}

	@Initialize(priority = 3)
	private void initCmds(){
		getCommand("mcmmox").setExecutor(new McMMOStub());
	}

	@Initialize(priority = 4)
	private void initListener(){
		getServer().getPluginManager().registerEvents(new MoveEvent(), this);
	}

	public static Mcmmox getInstance(){
		if(instance == null)
			throw new NullPointerException("Plugin was not initialized.");
		return instance;
	}

	public static void copyFile(InputStream in, File out) throws Exception{ // https://bukkit.org/threads/extracting-file-from-jar.16962/
		InputStream fis = in;
		FileOutputStream fos = new FileOutputStream(out);
		try{
			byte[] buf = new byte[1024];
			int i = 0;
			while((i = fis.read(buf)) != -1){
				fos.write(buf, 0, i);
			}
		}catch(Exception e){
			throw e;
		}finally{
			if(fis != null){
				fis.close();
			}
			if(fos != null){
				fos.close();
			}
		}
	}

	public String getPluginPrefix(){
		return fileManager.getFile(FileManager.Files.CONFIG).getString("Messages.PluginInfo.PluginPrefix");
	}

	public GeneralConfig getGeneralConfig(){
		return mConfigManager.getGeneralConfig();
	}

	public SwordsConfig getSwordsConfig(){
		return mConfigManager.getSwordsConfig();
	}

	public FileManager getFileManager(){
		return fileManager;
	}
}
