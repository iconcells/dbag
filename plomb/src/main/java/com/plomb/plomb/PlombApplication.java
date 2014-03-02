package com.plomb.plomb;

import android.app.Application;
import com.plomb.plomb.core.PlombApplicationModule;
import dagger.ObjectGraph;
import java.util.Arrays;
import java.util.List;
import mortar.Mortar;
import mortar.MortarScope;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

public class PlombApplication extends Application {
  private MortarScope rootScope;
  private ObjectGraph graph;

  @Override public void onCreate() {
    super.onCreate();

    RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
      @Override public void handleError(Throwable e) {
        throw new RuntimeException(e);
      }
    });

    graph = ObjectGraph.create(getModules().toArray());
    rootScope = Mortar.createRootScope(BuildConfig.DEBUG, graph);
  }

  protected List<Object> getModules() {
    return Arrays.<Object>asList(new PlombApplicationModule(this));
  }

  public void inject(Object object) {
    graph.inject(object);
  }

  public MortarScope getRootScope() {
    return rootScope;
  }
}
