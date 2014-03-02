package model;

import java.io.Serializable;

/**
 * Created by iamstuffed on 12/3/13.
 */
public class DeviceSummary implements Serializable {
  public Long _id;
  public String address;
  public String name;
  public int rssi;
  public Long lastSeenTime;
}
