package au.com.museumvictoria.fieldguide.vic.fork.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;

/**
 * Shows information about a species.
 */
public class SpeciesItemDetailFragment extends Fragment {
  private static final String ARGUMENT_SPECIES_ID = "speciesId";

  private SpeciesDetailPagerAdapter adapter;

  public static SpeciesItemDetailFragment newInstance(String speciesId) {
    Bundle arguments = new Bundle();
    arguments.putString(ARGUMENT_SPECIES_ID, speciesId);

    SpeciesItemDetailFragment fragment = new SpeciesItemDetailFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_item_detail, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    String speciesId = getArguments().getString(ARGUMENT_SPECIES_ID);
    if (speciesId == null) {
      throw new RuntimeException("Species ID missing");
    }

    adapter = new SpeciesDetailPagerAdapter(getActivity().getLayoutInflater(),
        FieldGuideDatabase.getInstance(getActivity().getApplicationContext()), speciesId);
    ((ViewPager) getView().findViewById(R.id.viewPager)).setAdapter(adapter);
  }

  @Override
  public void onDestroy() {
    adapter.destroy();

    super.onDestroy();
  }
}
