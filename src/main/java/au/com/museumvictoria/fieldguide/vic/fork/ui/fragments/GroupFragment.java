package au.com.museumvictoria.fieldguide.vic.fork.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;
import au.com.museumvictoria.fieldguide.vic.fork.provider.DataProviderFactory;

/**
 * Shows information about a group.
 */
public class GroupFragment extends Fragment {
  private static final String ARGUMENT_GROUP_NAME = "speciesgroup";

  /**
   * Callback interface to be notified when a species is selected. Activities using this fragment
   * must implement this interface.
   */
  public interface Callback {
    void onSpeciesSelected(String speciesId, String name, @Nullable String subname);
  }

  private Callback callback;

  private GroupPagerAdapter adapter;

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

    String groupOrder = getArguments().getString(ARGUMENT_GROUP_NAME);
    if (groupOrder == null) {
      throw new RuntimeException("Group order missing");
    }

    adapter = new GroupPagerAdapter(getActivity().getLayoutInflater(),
        FieldGuideDatabase.getInstance(getActivity().getApplicationContext()), groupOrder,
        DataProviderFactory.getDataProvider(getActivity().getApplicationContext()),
        adapterCallback);
    if (adapter.getCount() < 2) {
      getView().findViewById(R.id.pagerTabStrip).setVisibility(View.GONE);
    }
    ((ViewPager) getView().findViewById(R.id.viewPager)).setAdapter(adapter);
  }


  @Override
  public void onDestroy() {
    adapter.destroy();

    super.onDestroy();
  }

  @Override
  public void onDetach() {
    callback = null;
    super.onDetach();
  }

  private final GroupPagerAdapter.Callback adapterCallback = new GroupPagerAdapter.Callback() {
    @Override
    public void onSpeciesSelected(String speciesId, String name, @Nullable String subname) {
      callback.onSpeciesSelected(speciesId, name, subname);
    }
  };
}
