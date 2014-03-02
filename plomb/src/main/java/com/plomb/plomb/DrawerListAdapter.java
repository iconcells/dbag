package com.plomb.plomb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by dberrios on 2/25/14.
 */
public class DrawerListAdapter extends BaseAdapter {
  private String items[];
  private int selected;
  private LayoutInflater inflater;

  public DrawerListAdapter(Context context) {
    inflater = LayoutInflater.from(context);
    items = new String[] {
        "Beacons", "Profile"
    };
    selected = 0;
  }

  @Override public int getCount() {
    return items.length;
  }

  @Override public String getItem(int position) {
    return items[position];
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
    }
    ((TextView) convertView).setText(items[position]);
    if (position == selected) {
      convertView.setBackgroundResource(R.color.drawer_background_selected);
    } else {
      convertView.setBackgroundResource(R.color.drawer_background_normal);
    }
    return convertView;
  }

  public void setSelected(int position) {
    selected = position;
    notifyDataSetChanged();
  }
}
