package au.com.museumvictoria.fieldguide.vic.fork.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;
import au.com.museumvictoria.fieldguide.vic.fork.ui.ImageDetailActivity;
import au.com.museumvictoria.fieldguide.vic.fork.util.ImageResizer;
import au.com.museumvictoria.fieldguide.vic.fork.util.Utilities;

/**
 * Populates the species details pages.
 */
public class SpeciesDetailPagerAdapter extends PagerAdapter {
  private static final String TAG = SpeciesDetailPagerAdapter.class.getSimpleName();

  private static final int TAB_COUNT = 2;
  private static final int DETAILS_POSITION = 0;
  private static final int OTHER_POSITION = 1;

  private final LayoutInflater layoutInflater;
  private final String speciesId;
  private final Cursor detailsCursor;
  private final Cursor imagesCursor;
  private final String[] tabNames = new String[TAB_COUNT];

  public SpeciesDetailPagerAdapter(LayoutInflater layoutInflater, FieldGuideDatabase db,
      String speciesId) {
    super();

    this.layoutInflater = layoutInflater;
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
    tabNames[OTHER_POSITION] = context.getString(R.string.species_tab_other);
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

  private View createDetailsView(ViewGroup container) {
    View view = layoutInflater.inflate(R.layout.species_tab_details, container, false);

    LinearLayout imagesView = (LinearLayout) view.findViewById(R.id.speciesimages);

    Log.i(TAG, "Loading images");

    final Context context = layoutInflater.getContext();
    int imgThumbSize =
      context.getResources().getDimensionPixelSize(R.dimen.species_image_thumbnail_size);
    int imgThumbPadding =
      context.getResources().getDimensionPixelSize(R.dimen.image_detail_pager_margin);

    int imagesCounter = 0;
    for (imagesCursor.moveToFirst(); !imagesCursor.isAfterLast(); imagesCursor.moveToNext()) {
      String imagePath = Utilities.SPECIES_IMAGES_FULL_PATH
          + imagesCursor.getString(imagesCursor.getColumnIndex(FieldGuideDatabase.MEDIA_FILENAME));

      ImageView image = new ImageView(context);
      image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
          LinearLayout.LayoutParams.WRAP_CONTENT));
      image.setPadding(imgThumbPadding, imgThumbPadding, imgThumbPadding, imgThumbPadding);

      image.setImageBitmap(
          ImageResizer.decodeSampledBitmapFromFile(Utilities.getFullExternalDataPath(
              context, imagePath), imgThumbSize, imgThumbSize));
      // TODO: Try to use this. Also consider trying to use ZipResourceFile.getAssetFileDescriptor instead. It looks like the ImageResizer used to work with those. See http://stackoverflow.com/questions/13031240/reading-mp3-from-expansion-file -- looks like the zip needs to be uncompressed to do this.
      //try {
      //  image.setImageBitmap(ImageResizer.decodeSampledBitmapFromStream(
      //      Utilities.getAssetInputStreamZipFile(getActivity().getApplicationContext(), imagePath), imgThumbSize,
      //      imgThumbSize));
      //} catch (Exception e) {
      //  Log.i(TAG, "Exception loading image bitmap:" + e);
      //  image.setImageBitmap(null);
      //}
      //try {
      //  android.content.res.AssetFileDescriptor fd =
      //    Utilities.getAssetsFileDescriptor(getActivity(), imagePath);
      //  Log.i(TAG, "FD: " + fd);
      //  image.setImageBitmap(ImageResizer.decodeSampledBitmapFromFD(
      //      fd.getFileDescriptor(), imgThumbSize, imgThumbSize));
      //} catch (java.io.IOException e) {
      //  Log.i(TAG, "Exception loading image bitmap:" + e);
      //  image.setImageBitmap(null);
      //}
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
        getColumnValue(FieldGuideDatabase.SPECIES_LABEL));

    TextView sublabelView = (TextView) view.findViewById(R.id.sublabel);
    sublabelView.setText(getColumnValue(FieldGuideDatabase.SPECIES_SUBLABEL));
    // TODO: Set italic iff it's meant to be.
    sublabelView.setTypeface(sublabelView.getTypeface(), Typeface.ITALIC);

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

    return view;
  }

  private View createOtherView(ViewGroup container) {
    View view = layoutInflater.inflate(R.layout.species_tab_other, container, false);

    // TODO: This.
    ((TextView) view.findViewById(R.id.license)).setText("A license");
    ((TextView) view.findViewById(R.id.license_link)).setText("A license link");

    return view;
  }

  private boolean hasColumnValue(String columnName) {
    return !detailsCursor.isNull(detailsCursor.getColumnIndex(columnName));
  }

  private String getColumnValue(String columnName) {
    return detailsCursor.getString(detailsCursor.getColumnIndex(columnName));
  }
}
