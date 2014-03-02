package model;

/**
 * Created by dberrios on 2/21/14.
 */
public class Beacon {

  private String address;
  private String name;
  private int rssi;

  public Beacon(String address, String name, int rssi ) {
    this.address = address;
    this.name = name;
    this.rssi = rssi;
  }

  public String getAddress() {
    return address;
  }

  public void setBeacon(String beacon) {
    this.address = beacon;
  }

  @Override public String toString() {
    return address;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getRssi() {
    return rssi;
  }

  public void setRssi(int rssi) {
    this.rssi = rssi;
  }
}
