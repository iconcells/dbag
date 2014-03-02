package com.plomb.plomb.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.plomb.plomb.R;
import com.plomb.plomb.screen.ProfileScreen;
import javax.inject.Inject;
import mortar.Mortar;

public class ProfileView extends LinearLayout {
  public static final String TAG = ProfileView.class.getSimpleName();
  @Inject ProfileScreen.Presenter presenter;
  @InjectView(R.id.profile_name) TextView profileName;
  //private TextView dottie;

  public ProfileView(Context context) {
    super(context);
    Mortar.inject(context, this);
  }

  public ProfileView(Context context, AttributeSet attrs) {
    super(context, attrs);
    Mortar.inject(context, this);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.inject(this);
    presenter.takeView(this);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    presenter.dropView(this);
  }

  public void setProfileName(String name) {
    profileName.setText(name);
  }
}
