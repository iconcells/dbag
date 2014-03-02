package com.plomb.plomb.screen;

import android.os.Bundle;
import android.util.Log;
import com.plomb.plomb.ActionBarOwner;
import com.plomb.plomb.CupboardSQLiteOpenHelper;
import com.plomb.plomb.R;
import com.plomb.plomb.core.Main;
import com.plomb.plomb.view.BeaconsListView;
import dagger.Provides;
import flow.Flow;
import flow.Layout;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import model.Beacon;
import model.Beacons;
import model.DeviceSummary;
import mortar.Blueprint;
import mortar.ViewPresenter;

@Layout(R.layout.beacons_list_view)
public class BeaconsListScreen implements Blueprint {
  public static final String TAG = BeaconsListScreen.class.getSimpleName();

  @Override public String getMortarScopeName() {
    return getClass().getName();
  }

  @Override public Object getDaggerModule() {
    return new Module();
  }

  @dagger.Module(injects = BeaconsListView.class, addsTo = Main.Module.class)
  static class Module {
    @Provides List<Beacon> provideBeacons(Beacons beacons) {
      return beacons.getBeacons();
    }
  }

  @Singleton
  public static class Presenter extends ViewPresenter<BeaconsListView> {
    private final Flow flow;
    private final List<Beacon> beacons;
    private final ActionBarOwner actionBarOwner;
    @Inject CupboardSQLiteOpenHelper databaseHelper;

    @Inject Presenter(Flow flow, List<Beacon> beacons, ActionBarOwner actionBarOwner) {
      this.flow = flow;
      this.beacons = beacons;
      this.actionBarOwner = actionBarOwner;
    }

    @Override protected void onLoad(Bundle savedInstanceState) {
      super.onLoad(savedInstanceState);
      BeaconsListView view = getView();
      if (view == null) {
        return;
      }
      view.showBeacons(beacons);
      boolean hasUp = true;
      boolean drawerEnabled = true;
      String title = "Beacon List";
      actionBarOwner.setConfig(new ActionBarOwner.Config(true, hasUp, drawerEnabled, title, null));
    }

    public void onBeaconSelected(Beacon beacon) {
      Log.d(TAG, "Flowing to BeaconScreen");
      flow.goTo(new BeaconScreen(beacon));
    }

    public void onBeaconListRefreshed() {
      List<Beacon> beacons = new ArrayList<Beacon>();
      List<DeviceSummary> devices = databaseHelper.getDevices();
      for (DeviceSummary device : devices) {
        beacons.add(new Beacon(device.address));
      }
      getView().showBeacons(beacons);
    }
  }
}