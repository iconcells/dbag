package com.plomb.plomb.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.plomb.plomb.R;
import com.plomb.plomb.util.FlowOwnerView;
import javax.inject.Inject;
import mortar.Blueprint;
import mortar.Mortar;

public class MainView extends FlowOwnerView<Blueprint> {
  @Inject Main.Presenter presenter;
  @InjectView(R.id.container) MainView mainView;

  public MainView(Context context, AttributeSet attrs) {
    super(context, attrs);
    Mortar.inject(context, this);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.inject(this);
    getPresenter().takeView(mainView);
  }

  @Override protected ViewGroup getContainer() {
    return this;
  }

  public void toast(String toastText) {
    Toast.makeText(getContext(), toastText, Toast.LENGTH_SHORT).show();
  }

  @Override public Main.Presenter getPresenter() {
    return presenter;
  }
}
