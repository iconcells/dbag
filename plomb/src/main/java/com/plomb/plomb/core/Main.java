package com.plomb.plomb.core;

import android.os.Bundle;
import com.plomb.plomb.ActionBarModule;
import com.plomb.plomb.ActionBarOwner;
import com.plomb.plomb.screen.BeaconsListScreen;
import com.plomb.plomb.screen.ProfileScreen;
import com.plomb.plomb.screen.StatusScreen;
import com.plomb.plomb.util.FlowOwner;
import dagger.Provides;
import flow.Flow;
import flow.HasParent;
import flow.Parcer;
import javax.inject.Inject;
import javax.inject.Singleton;
import mortar.Blueprint;

public class Main implements Blueprint {

  @Override public String getMortarScopeName() {
    return getClass().getName();
  }

  @Override public Object getDaggerModule() {
    return new Module();
  }

  @dagger.Module( //
      includes = ActionBarModule.class,
      injects = { MainView.class, Main.class },
      addsTo = PlombApplicationModule.class,
      library = true)
  public static class Module {
    @Provides @MainScope Flow provideFlow(Presenter presenter) {
      return presenter.getFlow();
    }
  }

  @Singleton public static class Presenter extends FlowOwner<Blueprint, MainView> {
    private final ActionBarOwner actionBarOwner;
    private int position;

    @Inject Presenter(Parcer<Object> flowParcer, ActionBarOwner actionBarOwner) {
      super(flowParcer);
      this.actionBarOwner = actionBarOwner;
    }

    @Override public void showScreen(Blueprint newScreen, Flow.Direction flowDirection) {
      boolean hasUp = newScreen instanceof HasParent;
      String title = newScreen.getClass().getSimpleName();
      actionBarOwner.setConfig(new ActionBarOwner.Config(true, hasUp, true, title, null));

      super.showScreen(newScreen, flowDirection);
    }

    @Override protected Blueprint getFirstScreen() {
      return new StatusScreen();
    }

    //TODO: place this in a DrawerOwner type class.
    public void onDrawerItemSelected(int position) {
      if (this.position != position) {
        switch (position) {
          case 0:
            super.showScreen(new StatusScreen(), null);
            this.position = position;
            break;
          case 1:
            super.showScreen(new BeaconsListScreen(), null);
            this.position = position;
            break;
          case 2:
            super.showScreen(new ProfileScreen(), null);
            this.position = position;
            break;
          default:
            break;
        }
      }
    }

    @Override public void onLoad(Bundle savedInstanceState) {
      super.onLoad(savedInstanceState);
      if (savedInstanceState != null) {
        position = savedInstanceState.getInt("position", 0);
        onDrawerItemSelected(position);
      }
    }

    @Override public void onSave(Bundle outState) {
      super.onSave(outState);
      outState.putInt("position", position);
    }
  }
}
