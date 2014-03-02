package model;

/**
 * Created by dberrios on 2/21/14.
 */
public class Beacon {

  private String address;

  public Beacon(String address) {
    this.address = address;
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
}
