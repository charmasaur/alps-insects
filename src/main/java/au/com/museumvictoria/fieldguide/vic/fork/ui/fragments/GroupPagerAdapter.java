package au.com.museumvictoria.fieldguide.vic.fork.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;
import au.com.museumvictoria.fieldguide.vic.fork.provider.DataProvider;

/**
 * Shows information about a group.
 */
public class GroupPagerAdapter extends PagerAdapter {
  private static final String TAG = SpeciesDetailPagerAdapter.class.getSimpleName();

  /**
   * Callback interface to be notified when a species is selected.
   */
  public interface Callback {
    void onSpeciesSelected(String speciesId, String name, @Nullable String subname);
  }

  private static final int TAB_COUNT = 2;
  private static final int SPECIES_LIST_POSITION = 0;
  private static final int OTHER_POSITION = 1;

  private final LayoutInflater layoutInflater;
  private final DataProvider dataProvider;
  private final Callback callback;
  private final Cursor speciesListCursor;
  private final Cursor groupDetailsCursor;
  private final String[] tabNames = new String[TAB_COUNT];

  public GroupPagerAdapter(LayoutInflater layoutInflater, FieldGuideDatabase db, String groupOrder,
      DataProvider dataProvider, Callback callback) {
    super();

    this.layoutInflater = layoutInflater;
    this.dataProvider = dataProvider;
    this.callback = callback;

    speciesListCursor =
      db.getSpeciesInGroup(groupOrder, SpeciesListCursorAdapter.getRequiredColumns());
    if (speciesListCursor == null) {
      throw new RuntimeException("No species list found for group: " + groupOrder);
    }

    groupDetailsCursor = db.getGroupDetails(groupOrder, null);
    if (groupDetailsCursor == null) {
      throw new RuntimeException("No group details found for group: " + groupOrder);
    }

    Context context = layoutInflater.getContext();
    tabNames[SPECIES_LIST_POSITION] = context.getString(R.string.group_tab_species_list);
    tabNames[OTHER_POSITION] = context.getString(R.string.group_tab_other);
  }

  public void destroy() {
    groupDetailsCursor.close();
    speciesListCursor.close();
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    View view;
    switch (position) {
      case SPECIES_LIST_POSITION:
        view = createSpeciesListView(container);
        break;
      case OTHER_POSITION:
        view = createOtherView(container);
        break;
      default:
        throw new RuntimeException("Unrecognised position: " + position);
    }
    container.addView(view);
    return view;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  @Override
  public int getCount() {
    return TAB_COUNT;
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return tabNames[position];
  }

  private View createSpeciesListView(ViewGroup container) {
    View view = layoutInflater.inflate(R.layout.group_tab_species_list, container, false);

    ListView listView = (ListView) view.findViewById(R.id.species_list);
    listView.setFastScrollEnabled(true);

    final SpeciesListCursorAdapter adapter = new SpeciesListCursorAdapter(
        layoutInflater.getContext(), speciesListCursor, 0, dataProvider);

    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "Click" + position + " " + id);
        callback.onSpeciesSelected(Long.toString(id), adapter.getLabelAtPosition(position),
            adapter.getSublabelAtPosition(position));
      }
    });

    return view;
  }

  private View createOtherView(ViewGroup container) {
    View view = layoutInflater.inflate(R.layout.group_tab_other, container, false);

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

    return view;
  }

  private boolean hasDetailsColumnValue(String columnName) {
    return !groupDetailsCursor.isNull(groupDetailsCursor.getColumnIndex(columnName));
  }

  private String getDetailsColumnValue(String columnName) {
    return groupDetailsCursor.getString(groupDetailsCursor.getColumnIndex(columnName));
  }
}
