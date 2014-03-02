package com.plomb.plomb.core;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plomb.plomb.BluetoothLeService;
import com.plomb.plomb.CupboardSQLiteOpenHelper;
import com.plomb.plomb.PlombApplication;
import dagger.Module;
import dagger.Provides;
import flow.Parcer;
import java.util.concurrent.Executor;
import javax.inject.Singleton;
import model.Beacons;
import rx.Scheduler;
import rx.schedulers.ExecutorScheduler;

@Module(injects = { BluetoothLeService.class },
    includes = { Beacons.Module.class }, library = true)
public class PlombApplicationModule {

  private PlombApplication application;
  //private CupboardSQLiteOpenHelper databaseHelper;

  public PlombApplicationModule(PlombApplication application) {
    this.application = application;
  }

  @Provides @Singleton @MainThread Scheduler provideMainThread() {
    final Handler handler = new Handler(Looper.getMainLooper());
    return new ExecutorScheduler(new Executor() {
      @Override public void execute(Runnable command) {
        handler.post(command);
      }
    });
  }

  @Provides @Singleton Gson provideGson() {
    return new GsonBuilder().create();
  }

  @Provides @Singleton Parcer<Object> provideParcer(Gson gson) {
    return new GsonParcer<Object>(gson);
  }

  @Provides @Singleton PlombApplication providePlombApplication() {
    return application;
  }

  @Provides @Singleton
  CupboardSQLiteOpenHelper provideDatabaseHelper(PlombApplication application) {
    return new CupboardSQLiteOpenHelper(application);
  }
}
