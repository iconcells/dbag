package com.plomb.plomb.screen;

import android.os.Bundle;
import com.plomb.plomb.ActionBarOwner;
import com.plomb.plomb.R;
import com.plomb.plomb.core.Main;
import com.plomb.plomb.view.ProfileView;
import flow.Flow;
import flow.Layout;
import javax.inject.Inject;
import javax.inject.Singleton;
import mortar.Blueprint;
import mortar.ViewPresenter;

/**
 * Created by dberrios on 2/25/14.
 */
@Layout(R.layout.profile_view)
public class ProfileScreen implements Blueprint {

  public static final String TAG = ProfileScreen.class.getSimpleName();

  @Override public String getMortarScopeName() {
    return getClass().getName();
  }

  @Override public Object getDaggerModule() {
    return new Module();
  }

  @dagger.Module(injects = ProfileView.class, addsTo = Main.Module.class)
  static class Module {
    //@Provides List<Beacon> provideDotties(Dotties dotties) {
    //  return dotties.getDotties();
    //}
  }

  @Singleton
  public static class Presenter extends ViewPresenter<ProfileView> {
    private final Flow flow;
    private final ActionBarOwner actionBarOwner;

    @Inject Presenter(Flow flow, ActionBarOwner actionBarOwner) {
      this.flow = flow;
      this.actionBarOwner = actionBarOwner;
    }

    @Override protected void onLoad(Bundle savedInstanceState) {
      super.onLoad(savedInstanceState);
      ProfileView view = getView();
      if (view == null) {
        return;
      }
      view.setProfileName("David Berrios");
      boolean hasUp = true;
      boolean drawerEnabled = true;
      String title = "Profile";
      actionBarOwner.setConfig(new ActionBarOwner.Config(true, hasUp, drawerEnabled, title, null));
    }

    //public void onDottieSelected(Dottie dottie) {
    //  Log.d(TAG, "Flowing to BeaconScreen");
    //  flow.goTo(new BeaconScreen(dottie));
    //}
  }
}
