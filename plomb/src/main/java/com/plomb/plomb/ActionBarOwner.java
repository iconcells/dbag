/*
 * Copyright 2013 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.plomb.plomb;

import android.os.Bundle;
import java.util.ArrayList;
import mortar.MortarContext;
import mortar.MortarScope;
import mortar.Presenter;
import rx.util.functions.Action0;

/** Allows shared configuration of the Android ActionBar. */
public class ActionBarOwner extends Presenter<ActionBarOwner.View> {
  public interface View extends MortarContext {
    void setShowHomeEnabled(boolean enabled);

    void setUpButtonEnabled(boolean enabled);

    void setTitle(CharSequence title);

    void setMenu(ArrayList<MenuAction> actions);

    void setDrawerIndicatorEnabled(boolean enabled);
  }

  public static class Config {
    public final boolean showHomeEnabled;
    public final boolean upButtonEnabled;
    public final boolean drawerEnabled;
    public final CharSequence title;
    public final ArrayList<MenuAction> actions;

    public Config(boolean showHomeEnabled, boolean upButtonEnabled, boolean drawerEnabled,
        CharSequence title, ArrayList<MenuAction> actions) {
      this.showHomeEnabled = showHomeEnabled;
      this.upButtonEnabled = upButtonEnabled;
      this.title = title;
      this.actions = actions;
      this.drawerEnabled = drawerEnabled;
    }
  }

  public static class MenuAction {
    public final CharSequence title;
    public final int showFlags;
    public final Action0 action;

    public MenuAction(CharSequence title, int showFlags, Action0 action) {
      this.title = title;
      this.showFlags = showFlags;
      this.action = action;
    }
  }

  private Config config;

  ActionBarOwner() {
  }

  @Override public void onLoad(Bundle savedInstanceState) {
    super.onLoad(savedInstanceState);
    if (config != null) {
      update();
    }
  }

  public void setConfig(Config config) {
    this.config = config;
    update();
  }

  public Config getConfig() {
    return config;
  }

  @Override protected MortarScope extractScope(View view) {
    return view.getMortarScope();
  }

  private void update() {
    View view = getView();
    if (view == null) {
      return;
    }

    view.setShowHomeEnabled(config.showHomeEnabled);
    view.setUpButtonEnabled(config.upButtonEnabled);
    view.setTitle(config.title);
    view.setMenu(config.actions);
    view.setDrawerIndicatorEnabled(config.drawerEnabled);
  }
}
