package com.plomb.plomb.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.plomb.plomb.R;
import com.plomb.plomb.screen.BeaconsListScreen;
import java.util.List;
import javax.inject.Inject;
import model.Beacon;
import mortar.Mortar;

public class BeaconsListView extends ListView {
  @Inject BeaconsListScreen.Presenter presenter;

  public BeaconsListView(Context context, AttributeSet attrs) {
    super(context, attrs);
    Mortar.inject(context, this);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    presenter.takeView(this);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    presenter.dropView(this);
  }

  public void showBeacons(final List<Beacon> beacons) {
    Context context = getContext();
    BeaconsListAdapter listAdapter =
        new BeaconsListAdapter(LayoutInflater.from(context), beacons, presenter);
    setAdapter(listAdapter);

    //setOnItemClickListener(new OnItemClickListener() {
    //  @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    //    presenter.onDottieSelected(dotties.get(position));
    //  }
    //});
  }

  private static class BeaconsListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<Beacon> beacons;
    private BeaconsListScreen.Presenter presenter;

    public BeaconsListAdapter(LayoutInflater inflater, List<Beacon> beacons,
        BeaconsListScreen.Presenter presenter) {
      this.beacons = beacons;
      this.inflater = inflater;
      this.presenter = presenter;
    }

    @Override public int getCount() {
      return beacons == null ? 0 : beacons.size();
    }

    @Override public Beacon getItem(int position) {
      return beacons.get(position);
    }

    @Override public long getItemId(int position) {
      return position;
    }

    @Override public View getView(final int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = inflater.inflate(R.layout.beacons_item_view, parent, false);
      }

      ((TextView) convertView).setText(getItem(position).getAddress());
      convertView.setOnClickListener(new OnClickListener() {
        @Override public void onClick(View v) {
          presenter.onBeaconSelected(beacons.get(position));
        }
      });
      return convertView;
    }
  }
}
