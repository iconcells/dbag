package com.plomb.plomb.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.plomb.plomb.R;
import com.plomb.plomb.screen.BeaconScreen;
import javax.inject.Inject;
import mortar.Mortar;

/**
 * Created by dberrios on 2/21/14.
 */
public class BeaconView extends LinearLayout {
  public static final String TAG = BeaconView.class.getSimpleName();
  @Inject BeaconScreen.Presenter presenter;

  public BeaconView(Context context, AttributeSet attrs) {
    super(context, attrs);
    Mortar.inject(context, this);
    Log.d(TAG, "BeaconView(Context context, AttributeSet attrs)");
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
  public void setBeaconText(String dottieText) {
    TextView dottie = (TextView) findViewById(R.id.beacon_text);
    dottie.setText(dottieText);
  }
}
