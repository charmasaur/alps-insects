package au.com.museumvictoria.fieldguide.vic.fork.ui.fragments;

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
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;
import au.com.museumvictoria.fieldguide.vic.fork.util.ImageResizer;
import au.com.museumvictoria.fieldguide.vic.fork.util.Utilities;

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
    void onGroupSelected(String groupName);
  }

  private Callback callback;

	private ListView mListView;
	private Cursor mCursor;
	private FieldGuideDatabase database;
	private SpeciesCursorAdapter mAdapter; 
	
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    try {
      callback = (Callback) activity;
    } catch (ClassCastException e) {
      Log.e(TAG, "Container activity does not implement callback.");
      callback = null;
    }
  }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_species_group_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		database = FieldGuideDatabase.getInstance(getActivity().getApplicationContext());

		Log.i(TAG, "Loading grouped items");

		mCursor = database.getSpeciesGroups();
		
		mListView = (ListView) getView().findViewById(R.id.group_list);
		mListView.setFastScrollEnabled(true);
		mAdapter = new SpeciesCursorAdapter(getActivity().getApplicationContext(), mCursor, 0);
		mListView.setAdapter(mAdapter);

		Log.i(TAG, "Done loading items");
	}
	
	@Override
	public void onDestroy() {
		mCursor.close();
		database.close();
		
		super.onDestroy();
	}

  @Override
  public void onDetach() {
    callback = null;
  }
	
	private final class SpeciesCursorAdapter extends CursorAdapter implements SectionIndexer {
		private AlphabetIndexer mAlphabetIndexer;

		public SpeciesCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);

			mAlphabetIndexer = new AlphabetIndexer(c, c.getColumnIndex(FieldGuideDatabase.SPECIES_GROUP),
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
			final String groupLabel =
        cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_GROUP));
			String iconLabel = groupLabel.toLowerCase().replaceAll(" ", "").replaceAll(",", "");
			String iconPath = Utilities.SPECIES_GROUPS_PATH + iconLabel + ".png"; 
			
			TextView txtView1 = (TextView) view.findViewById(R.id.speciesLabel);
			txtView1.setText(groupLabel);
			
			ImageView imgView = (ImageView) view.findViewById(R.id.speciesIcon);
			// imgView.setImageBitmap(ImageResizer.decodeSampledBitmapFromAsset(getActivity().getAssets(), iconPath, 75, 75));
	        
			Log.w(TAG, "Getting AssetsFileDescriptor for species group icon: " + iconPath);
//			InputStream istr = null;
//			try {
//				istr = Utilities.getAssetInputStream(getActivity(), iconPath);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			imgView.setImageBitmap(ImageResizer.decodeSampledBitmapFromStream(istr, 75, 75));
			imgView.setImageBitmap(ImageResizer.decodeSampledBitmapFromFile(Utilities.getFullExternalDataPath(getActivity(), iconPath), 75, 75));

			
			TextView txtView2 = (TextView) view.findViewById(R.id.speciesSublabel);
			// txtView2.setText(cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_SUBLABEL)));
			txtView2.setVisibility(View.GONE);

      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Log.i(TAG, "View clicked: " + v);
          callback.onGroupSelected(groupLabel);
        }
      });
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View newView = inflater.inflate(R.layout.species_list, parent, false);
			return newView;
		}

	}
	
}
