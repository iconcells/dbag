package com.plomb.plomb;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.List;
import model.DeviceSummary;
import nl.qbusict.cupboard.CupboardFactory;
import nl.qbusict.cupboard.QueryResultIterable;

/**
 * Created by iamstuffed on 12/3/13.
 */
public class CupboardSQLiteOpenHelper extends SQLiteOpenHelper {
  private static final String DATABASE_NAME = "bluetoothdevice.db";
  private static final int DATABASE_VERSION = 2;
  private Context context;

  static {
    //register our models
    CupboardFactory.cupboard().register(DeviceSummary.class);
  }

  public CupboardSQLiteOpenHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.context = context;
  }

  @Override public void onCreate(SQLiteDatabase db) {
    //this will ensure that all tables are created
    CupboardFactory.cupboard().withDatabase(db).createTables();
    //TODO: create indexes
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //this will upgrade tables, adding columns and new tables.
    //Note that existing columns will not be converted
    CupboardFactory.cupboard().withDatabase(db).upgradeTables();
  }

  public void processDevice(BluetoothDevice device, int rssi) {
    //first check if the database already has a row
    DeviceSummary deviceSummary = CupboardFactory.cupboard()
        .withDatabase(getReadableDatabase())
        .query(DeviceSummary.class)
        .withSelection("address = ?", device.getAddress())
        .get();
    final long lastSeenTime = System.currentTimeMillis();
    if (deviceSummary == null) {
      //insert it
      DeviceSummary newDevice = new DeviceSummary();
      newDevice.lastSeenTime = lastSeenTime;
      newDevice.address = device.getAddress();
      newDevice.rssi = rssi;
      newDevice.name = device.getName();
      CupboardFactory.cupboard().withDatabase(getWritableDatabase()).put(newDevice);
      //bus.post(new RefreshBluetoothDevicesList());
      context.sendBroadcast(new Intent(BluetoothLeService.ACTION_REFRESH_DEVICE_LIST));
    } else if (lastSeenTime - deviceSummary.lastSeenTime > 500) {
      deviceSummary.rssi = rssi;
      deviceSummary.lastSeenTime = lastSeenTime;
      CupboardFactory.cupboard().withDatabase(getWritableDatabase()).put(deviceSummary);
      context.sendBroadcast(new Intent(BluetoothLeService.ACTION_REFRESH_DEVICE_LIST));
    }
  }

  public void pruneDevices() {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    long pruneTime =
        Integer.parseInt(sharedPreferences.getString("preference_time_before_prune", "8000"));
    long currentTime = System.currentTimeMillis();
    ArrayList<Long> devicesToRemove = new ArrayList<Long>();

    //get the cursor for this query
    //QueryResultIterable<DeviceSummary> itr = CupboardFactory.cupboard()
    //    .withDatabase(getReadableDatabase())
    //    .query(DeviceSummary.class)
    //    .query();

    Cursor cursor = CupboardFactory.cupboard()
        .withDatabase(getReadableDatabase())
        .query(DeviceSummary.class)
        .getCursor();
    //TODO: fix this code block.
    try {
      int lastSeenTimeIndex = cursor.getColumnIndex("lastSeenTime");
      int _idIndex = cursor.getColumnIndex("_id");
      //for (DeviceSummary device : itr) {
      while (cursor.moveToNext()) {
        long lastSeenTime = cursor.getLong(lastSeenTimeIndex);
        long _id = cursor.getLong(_idIndex);
        if (currentTime - lastSeenTime > pruneTime) {
          devicesToRemove.add(_id);
        }
      }
    } catch (Exception e) {
    } finally {
      cursor.close();
    }

    boolean updated = false;
    for (Long id : devicesToRemove) {
      updated = true;
      CupboardFactory.cupboard()
          .withDatabase(getWritableDatabase())
          .delete(DeviceSummary.class, id);
    }

    if (updated) {
      //do something
      //need to notify data set changed for listviews
      context.sendBroadcast(new Intent(BluetoothLeService.ACTION_REFRESH_DEVICE_LIST));
    }
  }

  public List<DeviceSummary> getDevices() {
    QueryResultIterable<DeviceSummary> devices = CupboardFactory.cupboard()
        .withDatabase(getReadableDatabase())
        .query(DeviceSummary.class)
        .orderBy("rssi desc")
        .query();
    ArrayList<DeviceSummary> outgoingDevices = new ArrayList<DeviceSummary>();
    for (DeviceSummary device : devices) {
      outgoingDevices.add(device);
    }

    return outgoingDevices;
  }
}
