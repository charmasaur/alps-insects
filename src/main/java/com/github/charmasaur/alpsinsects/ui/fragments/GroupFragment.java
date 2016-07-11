package com.github.charmasaur.alpsinsects.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.github.charmasaur.alpsinsects.R;
import com.github.charmasaur.alpsinsects.db.FieldGuideDatabase;
import com.github.charmasaur.alpsinsects.provider.DataProvider;
import com.github.charmasaur.alpsinsects.provider.DataProviderFactory;
import com.github.charmasaur.alpsinsects.util.ImageResizer;

/**
 * Shows information about a group.
 */
public class GroupFragment extends Fragment {
  private static final String TAG = GroupFragment.class.getSimpleName();

  private static final String ARGUMENT_GROUP_NAME = "speciesgroup";

  /**
   * Callback interface to be notified when a species is selected. Activities using this fragment
   * must implement this interface.
   */
  public interface Callback {
    void onSpeciesSelected(String speciesId, String name, @Nullable String subname);
  }

  private Callback callback;

  private DataProvider dataProvider;
  private Cursor speciesListCursor;
  private Cursor groupDetailsCursor;

  private boolean detailsCollapsed = true;
  /**
   * Returns a new {@link GroupFragment} instance corresponding to a particular group.
   *
   * @param groupName the {@link GROUPS_ORDER} value of the group
   */
  public static GroupFragment newInstance(String groupName) {
    Bundle arguments = new Bundle();
    arguments.putString(ARGUMENT_GROUP_NAME, groupName);

    GroupFragment fragment = new GroupFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    try {
      callback = (Callback) activity;
    } catch (ClassCastException e) {
      throw new RuntimeException("Container activity does not implement callback.");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_group, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    String groupOrder = getArguments().getString(ARGUMENT_GROUP_NAME);
    if (groupOrder == null) {
      throw new RuntimeException("Group order missing");
    }

    FieldGuideDatabase db = FieldGuideDatabase.getInstance(getActivity().getApplicationContext());
    dataProvider = DataProviderFactory.getDataProvider(getActivity().getApplicationContext());

    speciesListCursor =
        db.getSpeciesInGroup(groupOrder, SpeciesListCursorAdapter.getRequiredColumns());
    if (speciesListCursor == null) {
      throw new RuntimeException("No species list found for group: " + groupOrder);
    }

    groupDetailsCursor = db.getGroupDetails(groupOrder, null);
    if (groupDetailsCursor == null) {
      throw new RuntimeException("No group details found for group: " + groupOrder);
    }

    ListView listView = (ListView) getView().findViewById(R.id.species_list);
    listView.setFastScrollEnabled(true);
    listView.addHeaderView(createDetailsView(listView));

    final SpeciesListCursorAdapter adapter =
        new SpeciesListCursorAdapter(getActivity(), speciesListCursor, 0, dataProvider);

    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "Click" + position + " " + id);
        // Since we have a header view, the position in the adapter is actually 1 less (or fewer? I
        // don't know...) than the reported position.
        int adapterPosition = position - 1;
        callback.onSpeciesSelected(Long.toString(id), adapter.getLabelAtPosition(adapterPosition),
            adapter.getSublabelAtPosition(adapterPosition));
      }
    });
  }

  @Override
  public void onDestroy() {
    groupDetailsCursor.close();
    speciesListCursor.close();

    super.onDestroy();
  }

  @Override
  public void onDetach() {
    callback = null;
    super.onDetach();
  }

  private View createDetailsView(ViewGroup container) {
    View view =
        getActivity().getLayoutInflater().inflate(R.layout.group_details_item, container, false);

    ((ImageView) view.findViewById(R.id.icon)).setImageBitmap(
        ImageResizer.decodeSampledBitmapFromStream(dataProvider.getGroupIcon(
            getDetailsColumnValue(FieldGuideDatabase.GROUPS_ICON_WHITE_FILENAME))));

    ((TextView) view.findViewById(R.id.description)).setText(
        Html.fromHtml(getDetailsColumnValue(FieldGuideDatabase.GROUPS_DESCRIPTION)));

    View creditHeadingView = view.findViewById(R.id.icon_credit_heading);
    TextView creditView = (TextView) view.findViewById(R.id.icon_credit);
    if (hasDetailsColumnValue(FieldGuideDatabase.GROUPS_ICON_CREDIT)) {
      creditHeadingView.setVisibility(View.VISIBLE);
      creditView.setVisibility(View.VISIBLE);
      creditView.setText(getDetailsColumnValue(FieldGuideDatabase.GROUPS_ICON_CREDIT));
    } else {
      creditHeadingView.setVisibility(View.GONE);
      creditView.setVisibility(View.GONE);
    }

    View licenseHeadingView = view.findViewById(R.id.icon_license_heading);
    TextView licenseView = (TextView) view.findViewById(R.id.icon_license);
    if (hasDetailsColumnValue(FieldGuideDatabase.GROUPS_LICENSE_LINK)) {
      licenseHeadingView.setVisibility(View.VISIBLE);
      licenseView.setVisibility(View.VISIBLE);
      licenseView.setText(getDetailsColumnValue(FieldGuideDatabase.GROUPS_LICENSE_LINK));
    } else {
      licenseHeadingView.setVisibility(View.GONE);
      licenseView.setVisibility(View.GONE);
    }

    final View detailsView = view.findViewById(R.id.details);
    final ImageView detailsIcon = (ImageView) view.findViewById(R.id.expand_icon);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        detailsCollapsed = !detailsCollapsed;
        detailsView.setVisibility(detailsCollapsed ? View.GONE : View.VISIBLE);
        detailsIcon.setImageDrawable(getActivity().getResources().getDrawable(
            detailsCollapsed
                ? R.drawable.ic_expand_more_white_24dp : R.drawable.ic_expand_less_white_24dp));
      }
    });

    return view;
  }

  private boolean hasDetailsColumnValue(String columnName) {
    return !groupDetailsCursor.isNull(groupDetailsCursor.getColumnIndex(columnName));
  }

  private String getDetailsColumnValue(String columnName) {
    return groupDetailsCursor.getString(groupDetailsCursor.getColumnIndex(columnName));
  }
}
