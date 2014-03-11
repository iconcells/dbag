package com.plomb.plomb.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.plomb.plomb.R;
import com.plomb.plomb.screen.BeaconScreen;
import com.plomb.plomb.screen.BeaconsListScreen;
import com.plomb.plomb.screen.StatusScreen;
import javax.inject.Inject;

import model.Beacon;
import model.Status;
import mortar.Mortar;

import java.util.List;

/**
 * Created by dberrios on 2/21/14.
 */
public class StatusView extends LinearLayout {
  public static final String TAG = StatusView.class.getSimpleName();
  @Inject StatusScreen.Presenter presenter;

  public StatusView(Context context, AttributeSet attrs) {
    super(context, attrs);
    Mortar.inject(context, this);
    Log.d(TAG, "StatusView(Context context, AttributeSet attrs)");
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    presenter.takeView(this);
    Log.d(TAG, "finished inflating");
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    presenter.dropView(this);
  }

  public void toast(String message) {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  //TODO: set the actual fields you want to display
  public void showCheckinText() {
    TextView checkin = (TextView) findViewById(R.id.beacon_text);
    checkin.setVisibility(View.VISIBLE);
    checkin.setText("Checkin at airport");
  }

    private static class StatusListAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<Status> statuses;
        private StatusScreen.Presenter presenter;
        private int position;



        public StatusListAdapter(LayoutInflater inflater, List<Status> statuses,
                                  StatusScreen.Presenter presenter) {
            this.statuses = statuses;
            this.inflater = inflater;
            this.presenter = presenter;
            position = -1;
        }

        @Override public int getCount() {
            return statuses == null ? 0 : statuses.size();
        }

        @Override public Status getItem(int position) {
            return statuses.get(position);
        }

        @Override public long getItemId(int position) {
            return position;
        }

        @Override public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.status_item_view, parent, false);
            }

            //TODO: use ViewHolder
            Status status = getItem(position);
            /*TextView beaconName = (TextView) convertView.findViewById(R.id.beacon_name_text);
            beaconName.setText(status.getName());*/

            ImageView imgStatus = (ImageView) convertView;

            switch (position){
                case 0:

                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                default:


            }



            convertView.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    presenter.onStatusSelected(position);
                }
            });
            return convertView;
        }
    }
}
