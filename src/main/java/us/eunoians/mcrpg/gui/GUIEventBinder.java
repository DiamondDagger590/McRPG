package us.eunoians.mcrpg.gui;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class GUIEventBinder {

  private static ArrayList<String> defaultEvents;
  private int slot;
  @Getter
  private List<String> boundEventList;
  //If we feel the API could use the addition in the future
  //private static ArrayList<String> externalEvents;


  public GUIEventBinder(int slot, List<String> boundEventList) {
    this.slot = slot;
    this.boundEventList = boundEventList;
  }

  public static void loadGUIEventBinder() {
    defaultEvents = new ArrayList<String>();
    defaultEvents.add("Close");
    defaultEvents.add("Open");
    defaultEvents.add("Back");
    defaultEvents.add("OpenFile");
  }

  public static ArrayList<String> getDefaultEvents() {
    return (ArrayList<String>) defaultEvents.clone();
  }

  public int getSlot() {
    return slot;
  }


}
