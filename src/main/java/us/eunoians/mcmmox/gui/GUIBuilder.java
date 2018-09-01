package us.eunoians.mcmmox.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.util.IOUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GUIBuilder {

	/**
	 * The actual file path of the gui inside of the file
	 */
	@Getter
	private String path;
	/**
	 * The inventory or the actual 'gui'
	 */
	@Getter
	private Inventory inv;
	/**
	 * The name of the gui file path without the folder modifications
	 */
	@Getter
	private String rawPath;
	/**
	 * The name of the file the gui is located in
	 */
	@Getter
	private String rawFileName;
	/**
	 * An array of events that are bound to slots
	 */
	@Getter
	private ArrayList<GUIEventBinder> boundEvents;
	/**
	 * The file instance
	 */
	private File f;
	/**
	 * The file configuration instance
	 */
	private FileConfiguration config;

	private ArrayList<GUIItem> items;

	@Getter
	private McMMOPlayer player;

	public GUIBuilder(String fileName, String guiPath, McMMOPlayer player) {
		this.player = player;
		this.rawFileName = fileName;
		this.f = new File(Mcmmox.getInstance().getDataFolder(), File.separator + "guis" + File.separator + fileName);
		if (!f.exists())
      IOUtil.saveResource(Mcmmox.getInstance(), "guis" + "/" + fileName, false);
    this.config =YamlConfiguration.loadConfiguration(f);
    this.rawPath =guiPath;
    this.path ="GUI."+guiPath +".";
    this.inv =
	generateGUI();
    this.boundEvents =

	bindEvents();

}

	public GUIBuilder(String fileName, String guiPath, FileConfiguration config, File f, McMMOPlayer player) {
		this.player = player;
		this.rawFileName = fileName;
		this.rawPath = guiPath;
		this.config = config;
		this.path = "GUI." + guiPath + ".";
		this.f = f;
		this.inv = generateGUI();
		this.boundEvents = bindEvents();

	}

	/**
	 * Construct a new gui from the provided elements. Reuse the FileConfiguration so that way it doesnt have to do an I/O
	 *
	 * @return A new gui builder that is the same contents as the previous one but this will update placeholders.
	 */
	public GUIBuilder clone() {
		return new GUIBuilder(this.rawFileName, this.rawPath, this.config, this.f, this.player);
	}

	private Inventory generateGUI() {
		Inventory inv = Bukkit.createInventory(null, config.getInt(path + "Size"),
				Methods.color(config.getString(path + "Title")));
		items = new ArrayList<GUIItem>();
		for (String itemName : config.getConfigurationSection(path + "Items").getKeys(false)) {
			ItemStack item;
			Material type = Material.getMaterial(config.getString(path + "Items." + itemName + ".Material"));
			item = new ItemStack(type, 1);
			ItemMeta meta = item.getItemMeta();
			if (type.equals(Material.PLAYER_HEAD)) {
				SkullMeta sm = (SkullMeta) meta;
				sm.setOwningPlayer(player.getOfflineMcMMOPlayer());
			}
			meta.setDisplayName(Methods.color(config.getString(path + "Items." + itemName + ".Name")));
			if (config.contains(path + "Items." + itemName + ".Lore")) {
				List<String> lore = config.getStringList(path + "Items." + itemName + ".Lore");
				lore = Methods.colorLore(lore);
				meta.setLore(lore);
			}
			item.setItemMeta(meta);
			GUIItem i = new GUIItem(item, config.getInt(path + ".Items." + itemName + ".Slot"));
			items.add(i);
		}
		ItemStack filler;
		if (!config.contains(path + "FillerItem")) {
			return inv;
		}
		Material fillerType = Material.getMaterial(config.getString(path + "FillerItem.Material"));
		filler = new ItemStack(fillerType, 1);
		ItemMeta meta = filler.getItemMeta();
		meta.setDisplayName(Methods.color(config.getString(path + "FillerItem.Name")));
		filler.setItemMeta(meta);
		if (config.contains(path + "FillerItem.Lore")) {
			List<String> lore = config.getStringList(path + "FillerItem.Lore");
			lore = Methods.colorLore(lore);
			meta.setLore(lore);
			filler.setItemMeta(meta);
		}
		inv = Methods.fillInventory(inv, filler, items);
		return inv;

	}

	private ArrayList<GUIEventBinder> bindEvents() {
		ArrayList<GUIEventBinder> binder = new ArrayList<GUIEventBinder>();
		if (!config.contains(path + "Events")) {
			return binder;
		}
		for (String slotString : config.getConfigurationSection(path + "Events").getKeys(false)) {
			GUIEventBinder boundEvent = new GUIEventBinder(Integer.parseInt(slotString), (ArrayList) config.getStringList(path + "Events." + slotString));
			binder.add(boundEvent);
		}
		return binder;
	}

	public void setNewInventory(Inventory newInv) {
		this.inv = newInv;
	}

	public void replacePlaceHolders(McMMOPlayer player) {
		if (rawPath.equalsIgnoreCase("MainGUI")) {
			items.stream().filter(i -> i.getItemStack().hasItemMeta()).filter(i -> i.getItemStack().getItemMeta().hasLore()).forEach(i -> {
				ItemMeta meta = i.getItemStack().getItemMeta();
				List<String> lore2 = new ArrayList<>();
				meta.getLore().stream().forEach(s -> {
					lore2.add(s.replaceAll("%Power_Level", Integer.toString(player.getPowerLevel())).replaceAll("%Ability_Points%", Integer.toString(player.getAbilityPoints())));
				});
				meta.setLore(lore2);
				i.getItemStack().setItemMeta(meta);
			});
		}
	}

}
