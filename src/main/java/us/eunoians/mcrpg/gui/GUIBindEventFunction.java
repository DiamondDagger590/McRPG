package us.eunoians.mcrpg.gui;

import java.util.ArrayList;

@FunctionalInterface
public interface GUIBindEventFunction {

  ArrayList<GUIEventBinder> bindEvents(GUIBuilder guiBuilder);
}
