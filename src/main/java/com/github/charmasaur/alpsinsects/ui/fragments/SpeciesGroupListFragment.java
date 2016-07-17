package com.github.charmasaur.alpsinsects.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.GridView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import com.github.charmasaur.alpsinsects.R;
import com.github.charmasaur.alpsinsects.db.FieldGuideDatabase;
import com.github.charmasaur.alpsinsects.provider.DataProvider;
import com.github.charmasaur.alpsinsects.provider.DataProviderFactory;
import com.github.charmasaur.alpsinsects.util.ImageResizer;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;

/**
 * Displays a list of groups.
 */
public class SpeciesGroupListFragment extends Fragment {
  private static final String TAG = SpeciesGroupListFragment.class.getSimpleName();

  /**
   * Callback interface to be notified when a group is selected. Activities using this fragment
   * must implement this interface.
   */
  public interface Callback {
    void onGroupSelected(String groupName, String groupOrder);
  }

  private Callback callback;

  private GridView groupList;
  private Cursor mCursor;
  private FieldGuideDatabase database;
  private GroupListCursorAdapter mAdapter;

  public static SpeciesGroupListFragment newInstance() {
    return new SpeciesGroupListFragment();
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
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    database = FieldGuideDatabase.getInstance(getContext());

    // CursorAdapters need an _id column with integer values. We use the auto-generated rowid
    // column.
    mCursor = database.getSpeciesGroups(GroupListCursorAdapter.getRequiredColumns());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_species_group_list, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Log.i(TAG, "Loading grouped items");

    groupList = (GridView) getView().findViewById(R.id.group_list);
    groupList.setFastScrollEnabled(true);
    mAdapter = new GroupListCursorAdapter(getActivity().getApplicationContext(), mCursor, 0,
        DataProviderFactory.getDataProvider(getActivity().getApplicationContext()));
    groupList.setAdapter(mAdapter);
    groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "Click" + position + " " + mAdapter.getItem(position));
        callback.onGroupSelected(mAdapter.getGroupNameAtPosition(position),
            mAdapter.getGroupOrderAtPosition(position));
      }
    });

    Log.i(TAG, "Done loading items");
  }

  @Override
  public void onDestroy() {
    mCursor.close();
    super.onDestroy();
  }

  @Override
  public void onDetach() {
    callback = null;
    super.onDetach();
  }

  private static final class GroupListCursorAdapter extends CursorAdapter
      implements SectionIndexer {
    private final AlphabetIndexer mAlphabetIndexer;
    private final DataProvider dataProvider;

    public GroupListCursorAdapter(Context context, Cursor c, int flags,
        DataProvider dataProvider) {
      super(context, c, flags);
      this.dataProvider = dataProvider;

      mAlphabetIndexer = new AlphabetIndexer(c, c.getColumnIndex(FieldGuideDatabase.GROUPS_LABEL),
          " ABCDEFGHIJKLMNOPQRTSUVWXYZ");
      mAlphabetIndexer.setCursor(c);
    }

    @Override
    public int getPositionForSection(int section) {
      return mAlphabetIndexer.getPositionForSection(section);
    }

    @Override
    public int getSectionForPosition(int position) {
      return mAlphabetIndexer.getSectionForPosition(position);
    }

    @Override
    public Object[] getSections() {
      return mAlphabetIndexer.getSections();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      String groupLabel = getGroupName(cursor);
      String iconLabel =
          cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.GROUPS_ICON_DARK_FILENAME));

      TextView txtView1 = (TextView) view.findViewById(R.id.label);
      txtView1.setText(groupLabel);

      ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(
          new SVGBuilder()
              .readFromInputStream(dataProvider.getGroupIcon(iconLabel))
              .setColorSwap(0xFF000000, 0xFFFFFFFF)
              .build()
              .getDrawable());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      LayoutInflater inflater = LayoutInflater.from(context);
      View newView = inflater.inflate(R.layout.group_item, parent, false);
      return newView;
    }

    public String getGroupOrderAtPosition(int position) {
      Cursor cursor = (Cursor) getItem(position);
      return cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.GROUPS_ORDER));
    }

    public String getGroupNameAtPosition(int position) {
      return getGroupName((Cursor) getItem(position));
    }

    public static String[] getRequiredColumns() {
      return new String[] { FieldGuideDatabase.GROUPS_ID + " AS _id",
        FieldGuideDatabase.GROUPS_ORDER, FieldGuideDatabase.GROUPS_ICON_DARK_FILENAME,
        FieldGuideDatabase.GROUPS_LABEL };
    }

    private static String getGroupName(Cursor cursor) {
      return cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.GROUPS_LABEL));
    }
  }
}
