package com.github.charmasaur.alpsinsects.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.charmasaur.alpsinsects.R;
import com.github.charmasaur.alpsinsects.db.FieldGuideDatabase;
import com.github.charmasaur.alpsinsects.ui.ImageDetailActivity;
import com.github.charmasaur.alpsinsects.provider.DataProvider;
import com.github.charmasaur.alpsinsects.util.ImageResizer;

/**
 * Populates the species details pages.
 */
public class SpeciesDetailPagerAdapter extends PagerAdapter {
  private static final String TAG = SpeciesDetailPagerAdapter.class.getSimpleName();

  private static final int TAB_COUNT = 1;
  private static final int DETAILS_POSITION = 0;

  private final LayoutInflater layoutInflater;
  private final DataProvider dataProvider;
  private final String speciesId;
  private final Cursor detailsCursor;
  private final Cursor imagesCursor;
  private final String[] tabNames = new String[TAB_COUNT];

  public SpeciesDetailPagerAdapter(LayoutInflater layoutInflater, FieldGuideDatabase db,
      DataProvider dataProvider, String speciesId) {
    super();

    this.layoutInflater = layoutInflater;
    this.dataProvider = dataProvider;
    this.speciesId = speciesId;

    detailsCursor = db.getSpeciesDetails(speciesId, null);
    if (detailsCursor == null) {
      throw new RuntimeException("No species details found for species: " + speciesId);
    }

    imagesCursor = db.getSpeciesImages(speciesId, null);
    if (imagesCursor == null) {
      throw new RuntimeException("No images found for species: " + speciesId);
    }

    Context context = layoutInflater.getContext();
    tabNames[DETAILS_POSITION] = context.getString(R.string.species_tab_details);
  }

  public void destroy() {
    imagesCursor.close();
    detailsCursor.close();
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    View view;
    switch (position) {
      case DETAILS_POSITION:
        view = createDetailsView(container);
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

  private View createDetailsView(ViewGroup container) {
    View view = layoutInflater.inflate(R.layout.species_tab_details, container, false);

    LinearLayout imagesView = (LinearLayout) view.findViewById(R.id.speciesimages);

    Log.i(TAG, "Loading images");

    final Context context = layoutInflater.getContext();
    int imgThumbSize =
        context.getResources().getDimensionPixelSize(R.dimen.species_detail_thumbnail_size);
    int imgThumbPadding =
        context.getResources().getDimensionPixelSize(R.dimen.image_detail_pager_margin);

    int imagesCounter = 0;
    for (imagesCursor.moveToFirst(); !imagesCursor.isAfterLast(); imagesCursor.moveToNext()) {
      ImageView image = new ImageView(context);
      image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
          LinearLayout.LayoutParams.WRAP_CONTENT));
      image.setPadding(imgThumbPadding, imgThumbPadding, imgThumbPadding, imgThumbPadding);

      image.setImageBitmap(ImageResizer.decodeSampledBitmapFromStream(
          dataProvider.getSpeciesImage(imagesCursor.getString(
              imagesCursor.getColumnIndex(FieldGuideDatabase.MEDIA_FILENAME))),
          imgThumbSize, imgThumbSize));

      final int imagePosition = imagesCounter;
      image.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          final Intent intent = new Intent(context, ImageDetailActivity.class);
          intent.putExtra(ImageDetailActivity.EXTRA_SPECIES_ID, speciesId);
          intent.putExtra(ImageDetailActivity.EXTRA_IMAGE, imagePosition);
          intent.putExtra(ImageDetailActivity.EXTRA_SPECIES_LABEL,
              getColumnValue(FieldGuideDatabase.SPECIES_LABEL));
          context.startActivity(intent);
        }
      });

      imagesView.addView(image);

      ++imagesCounter;
    }
    Log.i(TAG, "Loaded " + imagesCounter + " images");

    ((TextView) view.findViewById(R.id.label)).setText(
        Html.fromHtml(getColumnValue(FieldGuideDatabase.SPECIES_LABEL)));

    TextView sublabelView = (TextView) view.findViewById(R.id.sublabel);
    String sublabel = getColumnValue(FieldGuideDatabase.SPECIES_SUBLABEL);
    if (sublabel == null) {
      sublabelView.setVisibility(View.GONE);
    } else {
      sublabelView.setText(Html.fromHtml(sublabel));
      sublabelView.setVisibility(View.VISIBLE);
    }

    ((TextView) view.findViewById(R.id.description)).setText(Html.fromHtml(
        getColumnValue(FieldGuideDatabase.SPECIES_DESCRIPTION)));

    ((TextView) view.findViewById(R.id.taxa_order))
        .setText(getColumnValue(FieldGuideDatabase.SPECIES_TAXA_ORDER));
    ((TextView) view.findViewById(R.id.taxa_family))
        .setText(getColumnValue(FieldGuideDatabase.SPECIES_TAXA_FAMILY));

    if (hasColumnValue(FieldGuideDatabase.SPECIES_TAXA_GENUS)) {
      view.findViewById(R.id.taxa_genus_row).setVisibility(View.VISIBLE);
      ((TextView) view.findViewById(R.id.taxa_genus))
        .setText(getColumnValue(FieldGuideDatabase.SPECIES_TAXA_GENUS));
    } else {
      view.findViewById(R.id.taxa_genus_row).setVisibility(View.GONE);
    }

    if (hasColumnValue(FieldGuideDatabase.SPECIES_TAXA_SPECIES)) {
      view.findViewById(R.id.taxa_species_row).setVisibility(View.VISIBLE);
      ((TextView) view.findViewById(R.id.taxa_species))
        .setText(getColumnValue(FieldGuideDatabase.SPECIES_TAXA_SPECIES));
    } else {
      view.findViewById(R.id.taxa_species_row).setVisibility(View.GONE);
    }

    View licenseHeadingView = view.findViewById(R.id.license_heading);
    TextView licenseView = (TextView) view.findViewById(R.id.license);

    String license = hasColumnValue(FieldGuideDatabase.SPECIES_LICENSE)
        ? getColumnValue(FieldGuideDatabase.SPECIES_LICENSE) : null;

    if (hasColumnValue(FieldGuideDatabase.SPECIES_LICENSE_LINK)) {
      licenseHeadingView.setVisibility(View.VISIBLE);
      licenseView.setVisibility(View.VISIBLE);

      String licenseLink = getColumnValue(FieldGuideDatabase.SPECIES_LICENSE_LINK);
      licenseView.setText(Html.fromHtml("<a href=\"" + licenseLink + "\">"
          + (license == null ? licenseLink : license) + "</a>"));
      licenseView.setMovementMethod(LinkMovementMethod.getInstance());
    } else if (license != null) {
      licenseHeadingView.setVisibility(View.VISIBLE);
      licenseView.setVisibility(View.VISIBLE);

      licenseView.setText(license);
      licenseView.setMovementMethod(null);
    } else {
      licenseHeadingView.setVisibility(View.GONE);
      licenseView.setVisibility(View.GONE);
    }

    return view;
  }

  private boolean hasColumnValue(String columnName) {
    return !detailsCursor.isNull(detailsCursor.getColumnIndex(columnName));
  }

  private String getColumnValue(String columnName) {
    return detailsCursor.getString(detailsCursor.getColumnIndex(columnName));
  }
}
