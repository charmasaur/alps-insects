package com.github.charmasaur.alpsinsects.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.charmasaur.alpsinsects.R;

/**
 * Displays the "get involved" text.
 */
public class GetInvolvedFragment extends Fragment {
  public static GetInvolvedFragment newInstance() {
    GetInvolvedFragment fragment = new GetInvolvedFragment();
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_get_involved, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    TextView textView = (TextView) getView().findViewById(R.id.linktextview);
    textView.setText(Html.fromHtml(
        getActivity().getString(R.string.get_involved_bowerbird)));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }
}
