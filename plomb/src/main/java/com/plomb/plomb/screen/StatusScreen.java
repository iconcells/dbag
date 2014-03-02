package com.plomb.plomb.screen;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import com.plomb.plomb.ActionBarOwner;
import com.plomb.plomb.CheckinEvent;
import com.plomb.plomb.Constants;
import com.plomb.plomb.CupboardSQLiteOpenHelper;
import com.plomb.plomb.DeviceListRefreshEvent;
import com.plomb.plomb.LuggageOnPlaneEvent;
import com.plomb.plomb.R;
import com.plomb.plomb.StartServiceEvent;
import com.plomb.plomb.StopServiceEvent;
import com.plomb.plomb.core.Main;
import com.plomb.plomb.view.StatusView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import flow.Flow;
import flow.Layout;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import model.DeviceSummary;
import mortar.Blueprint;
import mortar.ViewPresenter;
import rx.util.functions.Action0;

@Layout(R.layout.status_view)
public class StatusScreen implements Blueprint {
  private static final String TAG = StatusScreen.class.getSimpleName();

  public StatusScreen() {
    Log.d(TAG, "StatusScreen");
  }

  @Override public String getMortarScopeName() {
    return "StatusScreen{}";
  }

  @Override public Object getDaggerModule() {
    return new Module();
  }

  //@Override public BeaconsListScreen getParent() {
  //  return new BeaconsListScreen();
  //}

  @dagger.Module(injects = StatusView.class, addsTo = Main.Module.class)
  public class Module {
    //@Provides Beacon provideBeacon(Beacons beacons) {
    //  Log.d(TAG, "Providing beacon");
    //  return beacon;
    //}
  }

  @Singleton
  public static class Presenter extends ViewPresenter<StatusView> {
    private final ActionBarOwner actionBarOwner;
    private final Flow flow;
    private Bus bus;
    private CupboardSQLiteOpenHelper databaseHelper;
    private int currentStatus;

    @Inject Presenter(Flow flow, ActionBarOwner actionBarOwner, Bus bus,
        CupboardSQLiteOpenHelper databaseHelper) {
      this.flow = flow;
      this.actionBarOwner = actionBarOwner;
      this.bus = bus;
      this.currentStatus = 0;
      this.databaseHelper = databaseHelper;
    }

    @Override protected void onLoad(Bundle savedInstanceState) {
      super.onLoad(savedInstanceState);
      StatusView view = getView();
      if (view == null) {
        return;
      }

      boolean hasUp = true;
      boolean drawerEnabled = true;
      String title = "Status";
      actionBarOwner.setConfig(
          new ActionBarOwner.Config(true, hasUp, drawerEnabled, title, createMenuActions()));
      bus.register(this);
    }

    @Override protected void onSave(Bundle outState) {
      super.onSave(outState);
      bus.unregister(this);
    }

    public ArrayList<ActionBarOwner.MenuAction> createMenuActions() {
      ArrayList<ActionBarOwner.MenuAction> actions = new ArrayList<>();
      ActionBarOwner.MenuAction menu =
          new ActionBarOwner.MenuAction("Start", MenuItem.SHOW_AS_ACTION_NEVER, new Action0() {
            @Override public void call() {
              bus.post(new StartServiceEvent());
            }
          });
      ActionBarOwner.MenuAction menu2 =
          new ActionBarOwner.MenuAction("Stop", MenuItem.SHOW_AS_ACTION_NEVER, new Action0() {
            @Override public void call() {
              bus.post(new StopServiceEvent());
            }
          });
      actions.add(menu);
      actions.add(menu2);
      return actions;
    }

    @Subscribe public void onCheckinConditionsMet(CheckinEvent event) {
      getView().showCheckinText();
    }

    @Subscribe public void onDeviceListRefresh(DeviceListRefreshEvent event) {
      List<DeviceSummary> devices = databaseHelper.getDevices();
      if (checkinConditionsMet(devices)) {
        bus.post(new CheckinEvent());
      }
      //if (checkLuggageOnPlane(devices)) {
      //  bus.post(new LuggageOnPlaneEvent());
      //}
      //if (checkDepartedLAX(devices)) {
      //  bus.post(new DepartedLAXEvent());
      //}
      //if (checkArrivedSFO(devices)) {
      //  bus.post(new ArrivedSFOEvent());
      //}
      //if (checkLuggageLost(devices)) {
      //  bus.post(new LuggageLostEvent());
      //}
    }

    private boolean checkinConditionsMet(List<DeviceSummary> devices) {
      int lax,sfo, a, b;
      lax = sfo = a = b = -1000;
      for (DeviceSummary device: devices) {
        if (device.name.contentEquals(Constants.LAX_ADDRESS)) {
          lax = device.rssi;
        } else if (device.name.contentEquals(Constants.SFO_ADDRESS)) {
          sfo = device.rssi;
        } else if (device.name.contentEquals(Constants.BAG_A)) {
          a = device.rssi;
        } else if (device.name.contentEquals(Constants.BAG_B)) {
          b = device.rssi;
        }
      }

      if (lax > -75 && sfo < -90 && a > -60 && b > -50) {
        return true;
      }
      //LAX is close > -75
      //SFO is far < -90
      //BAG A is close > -60
      //BAG B is close > -50
      return false;
    }
  }
}
