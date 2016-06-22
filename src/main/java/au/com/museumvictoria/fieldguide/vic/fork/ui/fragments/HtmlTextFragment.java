package au.com.museumvictoria.fieldguide.vic.fork.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;
import au.com.museumvictoria.fieldguide.vic.fork.util.ImageResizer;
import au.com.museumvictoria.fieldguide.vic.fork.util.Utilities;

/**
 * Displays a block of HTML text.
 */
public class HtmlTextFragment extends Fragment {
  private static final String ARGUMENT_RESOURCE_ID = "resource_id";

  public static HtmlTextFragment newInstance(int resourceId) {
    Bundle arguments = new Bundle();
    arguments.putInt(ARGUMENT_RESOURCE_ID, resourceId);

    HtmlTextFragment fragment = new HtmlTextFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_html_text, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    TextView textView = (TextView) getView().findViewById(R.id.dummy);
    textView.setText(Html.fromHtml(
        getActivity().getString(getArguments().getInt(ARGUMENT_RESOURCE_ID))));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }
}
