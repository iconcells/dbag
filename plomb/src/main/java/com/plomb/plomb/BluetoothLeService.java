package com.plomb.plomb;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import javax.inject.Inject;

/**
 * Created by dberrios on 11/27/13.
 */
public class BluetoothLeService extends Service
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  public static final String ACTION_REFRESH_DEVICE_LIST =
      "com.plomb.plomb.ACTION_REFRESH_DEVICE_LIST";
  HandlerThread thread = new HandlerThread("Bluetooth", Process.THREAD_PRIORITY_BACKGROUND);
  @Inject CupboardSQLiteOpenHelper databaseHelper;
  private static final String TAG = BluetoothLeService.class.getSimpleName();
  private NotificationManager notificationManager;
  private static BluetoothLeService instance = null;
  private ScanTimerTask scanTimerTask;
  private PruneTimerTask pruneTimerTask;
  private Timer timer;
  private Timer pruneTimer;

  public static boolean isRunning() {
    return instance != null;
  }

  private static final int NOTIFICATION_ID = 1;
  private int timeBetweenScans;
  private int scanDuration;
  private boolean isEnabled;
  private Notification.Builder notificationBuilder;
  private Handler handler;
  final BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
    @Override public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {

      //Log.e(TAG, "Bluetooth device: " + bluetoothDevice + " " + bluetoothDevice.getName());
      if (!TextUtils.isEmpty(bluetoothDevice.getAddress()) && knownBeacons.containsKey(
          bluetoothDevice.getAddress())) {
        databaseHelper.processDevice(bluetoothDevice, rssi,
            knownBeacons.get(bluetoothDevice.getAddress()));
        Log.d(TAG, "bluetoothDevice: " + bluetoothDevice.getAddress() + " signal: " + rssi);

        //StringBuilder advertisement = new StringBuilder();
        //ByteBuffer buffer = ByteBuffer.wrap(bytes);
        //
        //int capacity = 0;
        //
        //while (buffer.hasRemaining()) {
        //  byte current = buffer.get();
        //  advertisement.append(String.format("%02x ", current));
        //  capacity++;
        //}
        //while (advertisement.toString().endsWith("00 ")) {
        //  advertisement.delete(advertisement.length() - 3, advertisement.length());
        //  capacity--;
        //}
        //if (advertisement.length() > 0) {
        //  advertisement.delete(advertisement.length() - 1, advertisement.length());
        //}
        //Log.d(TAG, "Device: "
        //    + bluetoothDevice.getName()
        //    + " Bytes("
        //    + capacity
        //    + "): '"
        //    + advertisement
        //    + "'");
      }
    }
  };
  private State state;
  private BluetoothAdapter bluetoothAdapter = null;
  private BluetoothManager bluetoothManager = null;

  private HashMap<String, String> knownBeacons = null;
  private SharedPreferences sharedPreferences;

  @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    updatePreferences();
  }

  public static class LocalBinder extends Binder {
    public BluetoothLeService getService() {
      return instance;
    }
  }

  private final IBinder binder = new LocalBinder();

  @Override public IBinder onBind(Intent intent) {
    return binder;
  }

  public boolean initialize() {
    Log.d(TAG, "initialize");

    if (bluetoothManager == null) {
      bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
      if (bluetoothManager == null) {
        Log.e(TAG, "Unable to initialize BluetoothManager.");
        return false;
      }
    }

    bluetoothAdapter = bluetoothManager.getAdapter();
    if (bluetoothAdapter == null) {
      Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
      return false;
    }
    return true;
  }

  @Override public void onCreate() {
    super.onCreate();
    ((PlombApplication) getApplication()).inject(this);
    handler = new Handler(getApplicationContext().getMainLooper());
    scanTimerTask = new ScanTimerTask();
    thread = new HandlerThread("background", Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    Log.d(TAG, "Timer started");
    Log.d(TAG, "BluetoothLeService created");
    showNotification();
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    updatePreferences();
    instance = this;
    startForeground(NOTIFICATION_ID, buildReadyNotification());
    knownBeacons = new HashMap<String, String>();
    //knownBeacons.put("C9:B6:F0:3A:47:A7", "Beacon RF");
    //knownBeacons.put("34:B1:F7:D1:40:71", "SensorTag");
    //knownBeacons.put("E3:BE:5E:34:67:B9", "Beacon RF");
    knownBeacons.put("E9:BB:DC:A3:42:9E", "LAX");
    knownBeacons.put("C1:13:46:6F:46:F7", "SFO");
    knownBeacons.put("90:59:AF:17:09:F5", "Bag A");
    knownBeacons.put("84:DD:20:EE:2E:9A", "Bag B");
    //knownBeacons.put("FA:1A:A1:8A:82:C7", "LAX");
    //knownBeacons.put("FF:DD:25:E9:B9:E9", "SFO");
    //knownBeacons.put("D7:65:6C:FD:F0:91", "Bag 1");
    //knownBeacons.put("D0:BF:45:14:40:E6", "Bag 2");
    state = State.STOPPED;
    initialize();
    //registerReceiver(bluetoothStateReceiver, bluetoothFilter);
  }

  @Override public void onDestroy() {
    Log.d(TAG, "BluetoothLeService stopped");
    notificationManager.cancel(NOTIFICATION_ID);
    instance = null;
    //unregisterReceiver(bluetoothStateReceiver);
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    stopForeground(true);
    super.onDestroy();
  }

  private void showNotification() {
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    notificationBuilder =
        new Notification.Builder(getApplicationContext()).setContentTitle("Plomb Scanning Service")
            .setContentText("Ready")
            .setSmallIcon(R.drawable.ic_launcher)
            .setOngoing(isEnabled)
            .setContentIntent(
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class),
                    Intent.FLAG_ACTIVITY_NEW_TASK));
    //temporarily use 1 as an id.
    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
  }

  private Notification buildReadyNotification() {
    notificationBuilder =
        new Notification.Builder(getApplicationContext()).setContentTitle("Plomb Scanning Service")
            .setContentText("Ready")
            .setSmallIcon(R.drawable.ic_launcher)
            .setOngoing(isEnabled)
            .setContentIntent(
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class),
                    Intent.FLAG_ACTIVITY_NEW_TASK));
    return notificationBuilder.build();
  }

  public void startScanning(final boolean continueAfterStopped) {
    if (bluetoothManager == null) {
      bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    }
    bluetoothAdapter = bluetoothManager.getAdapter();

    if (bluetoothAdapter != null && bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON &&
        isEnabled && (state == State.SLEEPING || state == State.STOPPED)) {
      //do the scan
      state = State.SCANNING;
      setNotification(R.string.scanning, isEnabled);
      bluetoothAdapter.startLeScan(callback);
    } else if (state == State.STOPPED) {
      state = State.STOPPED;
      setNotification(R.string.stopped, isEnabled);
    }

    //add a postDelayed to stop the scanning after a period of time
    handler.postDelayed(new Runnable() {
      @Override public void run() {
        stopScanning(continueAfterStopped);
      }
    }, scanDuration);
  }

  private void setNotification(int notificationTextId, boolean onGoing) {
    notificationBuilder.setContentText(getString(notificationTextId)).setOngoing(onGoing);
    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
  }

  public void stopScanning(final boolean continueAfterStopped) {
    bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    bluetoothAdapter = bluetoothManager.getAdapter();

    if (bluetoothAdapter != null && callback != null && state != State.STOPPED) {
      state = State.STOPPED;
      try {
        bluetoothAdapter.stopLeScan(callback);
      } catch (NullPointerException e) {
        Log.d(TAG, "WTF? How is this possible.");
      }
    }

    if (continueAfterStopped
        && bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON
        && isEnabled) {
      setNotification(R.string.sleeping, isEnabled);
    } else {
      setNotification(R.string.stopped, isEnabled);
      state = State.STOPPED;
    }
  }
  //
  //@Override public int onStartCommand(Intent intent, int flags, int startId) {
  //  return START_STICKY;
  //}

  public void updatePreferences() {
    scanDuration =
        Integer.parseInt(sharedPreferences.getString("preference_scan_duration", "3000"));
    timeBetweenScans =
        Integer.parseInt(sharedPreferences.getString("preference_time_between_scans", "1000"));
    isEnabled = sharedPreferences.getBoolean("preference_enable_background_scanning", true);
    updateTimer();
  }

  @TargetApi(19) @Override public void onTaskRemoved(Intent rootIntent) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
      restartServiceIntent.setPackage(getPackageName());

      PendingIntent restartServicePendingIntent =
          PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent,
              PendingIntent.FLAG_ONE_SHOT);
      AlarmManager alarmService =
          (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
      alarmService.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000,
          restartServicePendingIntent);
    }
    super.onTaskRemoved(rootIntent);
  }

  private void updateTimer() {
    if (isEnabled) {
      //startScanning(true);
      //scanTimerTask = new ScanTimerTask();
      if (timer != null) {
        scanTimerTask.cancel();
        timer.cancel();
        timer.purge();
      }
      timer = new Timer();
      scanTimerTask = new ScanTimerTask();
      timer.scheduleAtFixedRate(scanTimerTask, 0, scanDuration + timeBetweenScans);

      Log.i(TAG, "Starting scanning. Preferences enable background scanning is true");
    } else {
      Log.d(TAG, "Stopping scanning. Preferences enable background scanning is false");
      if (timer != null) {
        timer.cancel();
      }
    }

    if (pruneTimer != null) {
      pruneTimerTask.cancel();
      pruneTimer.cancel();
      pruneTimer.purge();
    }
    pruneTimer = new Timer();
    pruneTimerTask = new PruneTimerTask();
    pruneTimer.scheduleAtFixedRate(pruneTimerTask, 0, 250);
  }

  enum State {
    SCANNING,
    SLEEPING,
    STOPPED
  }

  private class ScanTimerTask extends TimerTask {
    @Override public void run() {
      handler.post(new Runnable() {
        @Override public void run() {
          startScanning(false);
        }
      });
      handler.postDelayed(new Runnable() {
        @Override public void run() {
          stopScanning(false);
        }
      }, scanDuration);
    }
  }

  private class PruneTimerTask extends TimerTask {
    @Override public void run() {
      databaseHelper.pruneDevices();
    }
  }

  @dagger.Module(injects = BluetoothLeService.class, library = true, complete = false)
  public static class Module {
  }
}
