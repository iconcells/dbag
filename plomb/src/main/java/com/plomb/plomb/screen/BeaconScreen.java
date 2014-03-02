package com.plomb.plomb.screen;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import com.plomb.plomb.ActionBarOwner;
import com.plomb.plomb.R;
import com.plomb.plomb.core.Main;
import com.plomb.plomb.view.BeaconView;
import dagger.Provides;
import flow.Flow;
import flow.HasParent;
import flow.Layout;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import model.Beacon;
import model.Beacons;
import mortar.Blueprint;
import mortar.ViewPresenter;
import rx.util.functions.Action0;
//import rx.Subscription;
//import rx.util.functions.Action0;
//import rx.util.functions.Action1;

@Layout(R.layout.beacon_view)
public class BeaconScreen implements HasParent<BeaconsListScreen>, Blueprint {
  private static final String TAG = BeaconScreen.class.getSimpleName();
  private final Beacon beacon;

  public BeaconScreen(Beacon beacon) {
    Log.d(TAG, "BeaconScreen");
    this.beacon = beacon;
  }

  @Override public String getMortarScopeName() {
    return "BeaconScreen{beacon=" + beacon + '}';
  }

  @Override public Object getDaggerModule() {
    return new Module();
  }

  @Override public BeaconsListScreen getParent() {
    return new BeaconsListScreen();
  }

  @dagger.Module(injects = BeaconView.class, addsTo = Main.Module.class)
  public class Module {
    @Provides Beacon provideBeacon(Beacons beacons) {
      Log.d(TAG, "Providing beacon");
      return beacon;
    }
  }

  @Singleton
  public static class Presenter extends ViewPresenter<BeaconView> {
    private final Beacon beacon;
    private final ActionBarOwner actionBarOwner;
    private final Flow flow;

    @Inject Presenter(Beacon beacon, Flow flow, ActionBarOwner actionBarOwner) {
      this.beacon = beacon;
      this.flow = flow;
      this.actionBarOwner = actionBarOwner;
    }

    @Override protected void onLoad(Bundle savedInstanceState) {
      super.onLoad(savedInstanceState);
      BeaconView view = getView();
      if (view == null) {
        return;
      }
      boolean hasUp = true;
      boolean drawerEnabled = false;
      String title = "Beacon";
      actionBarOwner.setConfig(
          new ActionBarOwner.Config(true, hasUp, drawerEnabled, title, createMenuActions()));

      //dottie.subscribe(new Action1<Beacon>() {
      //  @Override public void call(Beacon dottie) {
      //    BeaconView view = getView();
      //    if (view == null) {
      //      return;
      //    }
      //    Presenter.this.dottie = dottie;

      view.setBeaconText(beacon.getAddress());

      //});
      //Presenter.this.dottie = dottie;
      //view.setDottieText(dottie.getAddress());
    }

    public ArrayList<ActionBarOwner.MenuAction> createMenuActions() {
      ArrayList<ActionBarOwner.MenuAction> actions = new ArrayList<>();
      ActionBarOwner.MenuAction menu =
          new ActionBarOwner.MenuAction("Beacon", MenuItem.SHOW_AS_ACTION_NEVER, new Action0() {
            @Override public void call() {
              getView().toast("Beacons action selected");
            }
          });
      ActionBarOwner.MenuAction menu2 =
          new ActionBarOwner.MenuAction("Second", MenuItem.SHOW_AS_ACTION_NEVER, new Action0() {
            @Override public void call() {
              getView().toast("Second menu item selected");
            }
          });
      actions.add(menu);
      actions.add(menu2);
      return actions;
    }
  }
}
