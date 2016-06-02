package au.com.museumvictoria.fieldguide.vic.fork.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import au.com.museumvictoria.fieldguide.vic.fork.R;

public class SpeciesDetailPagerAdapter extends PagerAdapter {
  private static final String TAG = SpeciesDetailPagerAdapter.class.getSimpleName();

  public interface Callback {
    void doIt();
  }
  private final LayoutInflater layoutInflater;
  private final String[] speciestabs;
  private final Callback callback;

  public SpeciesDetailPagerAdapter(LayoutInflater layoutInflater, String[] speciestabs,
      Callback callback) {
    super();
    this.layoutInflater = layoutInflater;
    this.speciestabs = speciestabs;
    this.callback = callback;
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    String fragmentName = speciestabs[position];
    View view;
    if (fragmentName.toLowerCase().equals("details")) {
      view = layoutInflater.inflate(R.layout.fragment_species_item_details, container, false);
    } else if (fragmentName.toLowerCase().equals("commonly seen")) {
      view = layoutInflater.inflate(
          R.layout.fragment_species_item_location, container, false);
    } else if (fragmentName.toLowerCase().equals("scarcity")) {
      view = layoutInflater.inflate(
          R.layout.fragment_species_item_scarcity, container, false);
    } else {
      view = null;
    }
    container.addView(view);
    callback.doIt();
    return view;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  @Override
  public int getCount() {
    return speciestabs.length;
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return this.speciestabs[position];
  }
}
