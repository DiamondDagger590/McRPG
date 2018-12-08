package us.eunoians.mcrpg.gui;

import lombok.Getter;

import java.util.ArrayList;

public class GUIEventBinder {

  private int slot;
  @Getter
  private ArrayList<String> boundEventList;
  private static ArrayList<String> defaultEvents;
  //If we feel the API could use the addition in the future
  //private static ArrayList<String> externalEvents;


  public GUIEventBinder(int slot, ArrayList<String> boundEventList) {
    this.slot = slot;
    this.boundEventList = boundEventList;
  }

  public int getSlot() {
    return slot;
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


}
