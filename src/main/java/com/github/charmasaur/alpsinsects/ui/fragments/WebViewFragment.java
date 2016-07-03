package com.github.charmasaur.alpsinsects.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.github.charmasaur.alpsinsects.R;

/**
 * Displays a web view showing a particular HTML file.
 */
public class WebViewFragment extends Fragment {
  private static final String ARGUMENT_ASSET_FILENAME = "asset_filename";

  public static WebViewFragment newInstance(String assetFilename) {
    Bundle arguments = new Bundle();
    arguments.putString(ARGUMENT_ASSET_FILENAME, assetFilename);

    WebViewFragment fragment = new WebViewFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_webview, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    WebView webView = (WebView) getView().findViewById(R.id.dummy);
    webView.loadUrl("file:///android_asset/" + getArguments().getString(ARGUMENT_ASSET_FILENAME));
  }
}
