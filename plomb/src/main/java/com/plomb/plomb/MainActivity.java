package com.plomb.plomb;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.plomb.plomb.core.Main;
import com.plomb.plomb.core.MainView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import model.DeviceSummary;
import mortar.Mortar;
import mortar.MortarActivityScope;
import mortar.MortarContext;
import mortar.MortarScope;

import static android.content.Intent.ACTION_MAIN;
import static android.content.Intent.CATEGORY_LAUNCHER;

public class MainActivity extends Activity implements MortarContext, ActionBarOwner.View {

  private static final String TAG = MainActivity.class.getSimpleName();
  private MortarActivityScope activityScope;
  private ArrayList<ActionBarOwner.MenuAction> actionBarMenuActions;
  @InjectView(R.id.container) MainView mainView;
  @Inject ActionBarOwner actionBarOwner;
  //@Inject Main.Presenter presenter;
  @InjectView(R.id.drawer_layout) DrawerLayout drawerLayout;
  private ActionBarDrawerToggle drawerToggle;
  @InjectView(R.id.drawer_list) ListView drawerList;
  private DrawerListAdapter drawerListAdapter;
  private Main main;
  @Inject Bus bus;
  //@Inject CupboardSQLiteOpenHelper databaseHelper;
  private BroadcastReceiver listUpdatedReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (isWrongInstance()) {
      finish();
      return;
    }

    main = new Main();

    MortarScope parentScope = ((PlombApplication) getApplication()).getRootScope();
    activityScope = Mortar.requireActivityScope(parentScope, main);
    Mortar.inject(this, this);
    activityScope.onCreate(savedInstanceState);
    //((PlombApplication) getApplication()).inject(this);

    setContentView(R.layout.activity_main);
    ButterKnife.inject(this);

    drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer,
        R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
      @Override public void onDrawerClosed(View drawerView) {
        super.onDrawerClosed(drawerView);
        invalidateOptionsMenu();
      }

      @Override public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        invalidateOptionsMenu();
      }
    };

    drawerLayout.setDrawerListener(drawerToggle);
    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    actionBarOwner.takeView(this);

    drawerListAdapter = new DrawerListAdapter(this);
    drawerList.setAdapter(drawerListAdapter);

    drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "Setting selected position");
        drawerListAdapter.setSelected(position);
        drawerLayout.closeDrawer(GravityCompat.START);
        getMainView().getPresenter().onDrawerItemSelected(position);
      }
    });

    drawerListAdapter.setSelected(0);
    //scanReceiver = new BroadcastReceiver() {
    //  @Override public void onReceive(Context context, Intent intent) {
    //    if (intent.getAction().contentEquals(BluetoothLeService.ACTION_REFRESH_DEVICE_LIST)) {
    //      getMainView().getPresenter().onDrawerItemSelected();
    //    }
    //  }
    //}

    listUpdatedReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction().contentEquals(BluetoothLeService.ACTION_REFRESH_DEVICE_LIST)) {
          bus.post(new DeviceListRefreshEvent());
        }
      }
    };
  }

  @Override protected void onResume() {
    super.onResume();
    bus.register(this);
    IntentFilter intentFilter = new IntentFilter(BluetoothLeService.ACTION_REFRESH_DEVICE_LIST);
    registerReceiver(listUpdatedReceiver, intentFilter);
  }

  @Override protected void onPause() {
    bus.unregister(this);
    unregisterReceiver(listUpdatedReceiver);
    super.onPause();
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    activityScope.onSaveInstanceState(outState);
    super.onSaveInstanceState(outState);
  }

  @Override protected void onDestroy() {
    actionBarOwner.dropView(this);

    if (activityScope != null) {
      activityScope.destroy();
      activityScope = null;
    }
    if (BluetoothLeService.isRunning()) {
      stopService(new Intent(this, BluetoothLeService.class));
    }
    super.onDestroy();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    if (actionBarMenuActions != null) {
      for (final ActionBarOwner.MenuAction action : actionBarMenuActions) {
        menu.add(action.title)
            .setShowAsActionFlags(action.showFlags)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
              @Override public boolean onMenuItemClick(MenuItem item) {
                action.action.call();
                return true;
              }
            });
      }
    }
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      //return view.onUpPressed();
      if (drawerToggle.onOptionsItemSelected(item)) {
        return true;
      } else {
        MainView view = getMainView();
        view.onUpPressed();
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onBackPressed() {
    MainView view = getMainView();
    if (!view.onBackPressed()) {
      super.onBackPressed();
    }
  }

  //public void restoreActionBar() {
  //  ActionBar actionBar = getActionBar();
  //  actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
  //  actionBar.setDisplayShowTitleEnabled(true);
  //  actionBar.setTitle(mTitle);
  //}

  @Override public void setShowHomeEnabled(boolean enabled) {
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowHomeEnabled(enabled);
  }

  @Override public void setUpButtonEnabled(boolean enabled) {
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(enabled);
    actionBar.setHomeButtonEnabled(enabled);
  }

  @Override public void setDrawerIndicatorEnabled(boolean enabled) {
    drawerToggle.setDrawerIndicatorEnabled(enabled);
  }

  @Override public MortarScope getMortarScope() {
    return activityScope;
  }

  private MainView getMainView() {

    //ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
    return mainView;
  }

  @Override public void setMenu(ArrayList<ActionBarOwner.MenuAction> actions) {
    if (actions != actionBarMenuActions) {
      actionBarMenuActions = actions;
      invalidateOptionsMenu();
    }
  }

  /**
   * Dev tools and the play store (and others?) launch with a different intent, and so
   * lead to a redundant instance of this activity being spawned. <a
   * href="http://stackoverflow.com/questions/17702202/find-out-whether-the-current-activity-will-be-task-root-eventually-after-pendin"
   * >Details</a>.
   */
  private boolean isWrongInstance() {
    if (!isTaskRoot()) {
      Intent intent = getIntent();
      boolean isMainAction = intent.getAction() != null && intent.getAction().equals(ACTION_MAIN);
      return intent.hasCategory(CATEGORY_LAUNCHER) && isMainAction;
    }
    return false;
  }

  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    drawerToggle.syncState();
  }

  @Subscribe public void onStartService(StartServiceEvent event) {
    startService(new Intent(this, BluetoothLeService.class));
  }

  @Subscribe public void onStopService(StopServiceEvent event) {
    stopService(new Intent(this, BluetoothLeService.class));
  }
}
