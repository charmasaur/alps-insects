package au.com.museumvictoria.fieldguide.vic.fork.ui.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.adapter.SpeciesListCursorAdapter;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;


/**
 * A list of species in a group. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link SpeciesItemDetailFragment}.
 *
 * <p>Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
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

  private SimpleAdapter sa;
  private ListView mListView;
  private Cursor mCursor;
  private FieldGuideDatabase fgdb;

  /**
   * TODO: Document exactly what groupName we expect.
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

    fgdb = FieldGuideDatabase.getInstance(getActivity()
        .getApplicationContext());

    // TODO: Does it makes sense to move this to the adapter?
    mCursor = fgdb.getSpeciesInGroup(getArguments().getString(ARGUMENT_GROUP_NAME),
          new String[] { BaseColumns._ID, FieldGuideDatabase.SPECIES_LABEL,
              FieldGuideDatabase.SPECIES_SUBLABEL, FieldGuideDatabase.SPECIES_THUMBNAIL });

    mListView = (ListView) getView().findViewById(R.id.species_list);
    mListView.setFastScrollEnabled(true);

    final SpeciesListCursorAdapter adapter =
        new SpeciesListCursorAdapter(getActivity().getApplicationContext(), mCursor, 0);

    mListView.setAdapter(adapter);
    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "Click" + position + " " + id);
        // TODO: Need a better way to get IDs.
        callback.onSpeciesSelected(Long.toString(id), adapter.getLabelAtPosition(position),
            adapter.getSublabelAtPosition(position));
      }
    });
  }


  @Override
  public void onDestroy() {
    mCursor.close();
    fgdb.close();

    super.onDestroy();
  }

  @Override
  public void onDetach() {
    callback = null;
    super.onDetach();
  }
}
