package us.eunoians.mcrpg.gui;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

public class CustomBackButton extends GUIItem {


  @Getter
  @Setter
  private String event;

  public CustomBackButton(String event, ItemStack item, int slot){
    super(item, slot);
    this.event = event;
  }

}
