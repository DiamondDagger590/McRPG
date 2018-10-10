package us.eunoians.mcmmox.gui;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;

import java.util.List;

/*
This is the base class for any gui.
Any child class of this can be used by the GUITracker to ensure that it works
*/
public abstract class GUI {

  /**
   * The actual gui builder
   */
  @Getter
  @Setter
  private GUIBuilder gui;
  /**
   * If when a gui closes if the player should be wiped from the GUITracker
   */
  @Getter
  @Setter
  private boolean clearData = false;

  public GUI(GUIBuilder gui) {
    this.gui = gui;
  }

	/*protected Inventory fillInventory(Inventory inv, ItemStack filler, ArrayList<GUIItem> items) {
		for(GUIItem item : items) {
			inv.setItem(item.getSlot(), item.getItemStack());
		}
		for(int i = 0; i < inv.getSize(); i ++) {
			ItemStack testItem = inv.getItem(i);
			if(testItem == null) {
				inv.setItem(i, filler);
			}
		}
		return inv;
	}*/

  protected List<String> colorLore(List<String> lore) {
    for (int i = 0; i < lore.size(); i++) {
      String s = lore.get(i);
      lore.set(i, Methods.color(s));
    }
    return lore;
  }

  public McMMOPlayer getPlayer() {
    return gui.getPlayer();
  }
}