package model;

import com.plomb.plomb.core.MainThread;
import dagger.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;
import rx.Scheduler;

@Singleton
public class Beacons {
  private List<Beacon> beacons;

  final Scheduler mainThread;
  final Executor messagePollThread;

  @Inject Beacons(@MainThread Scheduler mainThread, Executor messagePollThread) {
    this.mainThread = mainThread;
    this.messagePollThread = messagePollThread;

    //Beacon one = new Beacon("1");
    //Beacon two = new Beacon("2");

    //beacons = Arrays.asList(one, two);
    beacons = new ArrayList<Beacon>();
  }

  public List<Beacon> getBeacons() {
    return beacons;
  }

  public Observable<Beacon> getBeacon(int beaconIndex) {
    return Observable.from(beacons.get(beaconIndex));
  }

  @dagger.Module(injects = Beacons.class, library = true, complete = false)
  public static class Module {
    @Provides @Singleton Executor provideMessagePollThread() {
      return Executors.newSingleThreadExecutor();
    }
  }
}
