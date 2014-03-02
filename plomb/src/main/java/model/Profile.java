package model;

import com.plomb.plomb.core.MainThread;
import dagger.Provides;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Scheduler;

/**
 * Created by dberrios on 2/25/14.
 */
public class Profile {

  final Scheduler mainThread;
  final Executor messagePollThread;

  @Inject Profile(@MainThread Scheduler mainThread, Executor messagePollThread) {
    this.mainThread = mainThread;
    this.messagePollThread = messagePollThread;
  }

  @dagger.Module(injects = Profile.class, library = true, complete = false)
  public static class Module {
    @Provides @Singleton Executor provideMessagePollThread() {
      return Executors.newSingleThreadExecutor();
    }
  }
}
