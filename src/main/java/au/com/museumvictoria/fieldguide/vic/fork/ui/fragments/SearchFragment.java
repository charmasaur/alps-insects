package au.com.museumvictoria.fieldguide.vic.fork.ui.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.adapter.SpeciesListCursorAdapter;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;
import au.com.museumvictoria.fieldguide.vic.fork.util.Utilities;

/**
 * Gets started to handle tapping on a search result (I think). And maybe for searches from the
 * system widget or something? Shows a list of search results when a search actually is completed
 * (rather than just an item selected from the instant list).
 */
public class SearchFragment extends Fragment {
  private static final String TAG = SearchFragment.class.getSimpleName();

  /**
   * Callback interface to be notified when a species is selected. Activities using this fragment
   * must implement this interface.
   */
  public interface Callback {
    void onSpeciesSelected(String speciesId, String name, @Nullable String subname);
  }

  private Callback callback;

  private TextView mTextView;
  private ListView mListView;

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
    return inflater.inflate(R.layout.fragment_search_results, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mTextView = (TextView) getView().findViewById(R.id.text);
    mListView = (ListView) getView().findViewById(R.id.list);

    searchSpecies(getArguments().getString(SearchManager.QUERY));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onDetach() {
    callback = null;
    super.onDetach();
  }

  private void searchSpecies(String query) {
    FieldGuideDatabase fgdb =
      FieldGuideDatabase.getInstance(getActivity().getApplicationContext());
    Cursor cursor = fgdb.getSpeciesMatches(query);

    if (cursor == null) {
      mTextView.setText(getString(R.string.no_results, new Object[] { query }));
    } else {
      // Display the number of results
      int count = cursor.getCount();
      String countString = getResources().getQuantityString(R.plurals.search_results, count,
          new Object[] { count, query });
      mTextView.setText(countString);

      // Specify the columns we want to display in the result
      // String[] from = new String[]
      //     { FieldGuideDatabase.SPECIES_LABEL, FieldGuideDatabase.SPECIES_SUBLABEL };

      // Specify the corresponding layout elements where we want the columns to go
      // int[] to = new int[] { R.id.speciesLabel, R.id.speciesSublabel };

      // Create a simple cursor adapter for the definitions and apply them to the ListView
      // SimpleCursorAdapter words =
      //     new SimpleCursorAdapter(this, R.layout.layout2_species_list_2, cursor, from, to, 0);
      // mListView.setAdapter(words);

      final SpeciesListCursorAdapter adapter =
          new SpeciesListCursorAdapter(getActivity().getApplicationContext(), cursor, 0);

      mListView.setAdapter(adapter);
      mListView.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          Log.w("SearchActivity", "Displaying species details for " + id);
          Log.w("SearchActivity", view.toString());

          callback.onSpeciesSelected(Long.toString(id), adapter.getLabelAtPosition(position),
              adapter.getSublabelAtPosition(position));
        }
      });
    }
  }
}
