package au.com.museumvictoria.fieldguide.vic.fork.ui.fragments;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;
import au.com.museumvictoria.fieldguide.vic.fork.ui.ImageDetailActivity;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.SpeciesDetailPagerAdapter;
import au.com.museumvictoria.fieldguide.vic.fork.util.ImageResizer;
import au.com.museumvictoria.fieldguide.vic.fork.util.Utilities;
import au.com.museumvictoria.fieldguide.vic.fork.util.Utils;

/**
 * Shows information about a species.
 */
public class SpeciesItemDetailFragment extends Fragment {
  private static final String TAG = SpeciesItemDetailFragment.class.getSimpleName();

  private static final String ARGUMENT_SPECIES_ID = "speciesId";

  private FieldGuideDatabase fgdb;
  private Cursor cursor;
  private Cursor cursorImages;
  private String speciesId;

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

    speciesId = getArguments().getString(ARGUMENT_SPECIES_ID);
    Log.w(TAG, "Getting details for " + speciesId);

    fgdb = FieldGuideDatabase.getInstance(getActivity().getApplicationContext());
    cursor = fgdb.getSpeciesDetails(speciesId, null);
    if (cursor == null) {
      throw new RuntimeException("No species details found for species: " + speciesId);
    }

    String label = cursor.getString(cursor.getColumnIndex("label"));
    cursorImages = fgdb.getSpeciesImages(speciesId, null);
    if (cursorImages == null) {
      throw new RuntimeException("No images found for species: " + speciesId);
    }

    //displaySpeciesInformation();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    String[] speciestabs = getResources().getStringArray(
        R.array.tab_details_titles);

    Log.d(TAG, "Adding species detail view pager");

    ViewPager mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
    if (mViewPager != null) {
      mViewPager.setAdapter(new SpeciesDetailPagerAdapter(
        getActivity().getLayoutInflater(), speciestabs, pagerCallback));
    }
  }

  private final SpeciesDetailPagerAdapter.Callback pagerCallback =
      new SpeciesDetailPagerAdapter.Callback() {
    @Override
    public void doIt() {
      displaySpeciesInformation();
    }
  };


  @Override
  public void onDestroy() {

    cursorImages.close();
    cursor.close();

    super.onDestroy();
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  public void displaySpeciesInformation() {
    Resources r = getResources();

    String speciesnamedisplay = "";

    TextView tvCommonName = (TextView) getActivity().findViewById(R.id.sdCommonname);
    if (tvCommonName != null) {

      ((TextView) getActivity().findViewById(R.id.sdCommonname)).setText(getColumnValue("label"));

      TextView sdSpeciesname = (TextView) getActivity().findViewById(R.id.sdSpeciesname);
      String speciesname = getColumnValue("sublabel");
      speciesnamedisplay = speciesname;

      sdSpeciesname.setText(speciesname);
      if (speciesname.startsWith("Phylum") || speciesname.startsWith("Class") || speciesname.startsWith("Order") || speciesname.startsWith("Family")) {
        sdSpeciesname.setTypeface(null, Typeface.NORMAL);
      } else {
        speciesnamedisplay = "<em>" + speciesname + "</em>";
      }

      ((TextView) getActivity().findViewById(R.id.sdDescription))
          .setText(Html.fromHtml(getColumnValue("description")));
      ((TextView) getActivity().findViewById(R.id.sdBiology)).setText(Html
          .fromHtml(getColumnValue("biology")));
      ((TextView) getActivity().findViewById(R.id.sdHabitat)).setText(Html
          .fromHtml(getColumnValue("habitat")));
      ((TextView) getActivity().findViewById(R.id.sdNativeStatus))
          .setText(Html.fromHtml(getColumnValue("nativeStatus")));
      ((TextView) getActivity().findViewById(R.id.sdTaxaPhylum))
          .setText(getColumnValue("taxaPhylum"));
      ((TextView) getActivity().findViewById(R.id.sdTaxaClass))
          .setText(getColumnValue("taxaClass"));
      ((TextView) getActivity().findViewById(R.id.sdTaxaOrder))
          .setText(getColumnValue("taxaOrder"));
      ((TextView) getActivity().findViewById(R.id.sdTaxaFamily))
          .setText(getColumnValue("taxaFamily"));
      ((TextView) getActivity().findViewById(R.id.sdTaxaGenus))
          .setText(getColumnValue("taxaGenus"));
      ((TextView) getActivity().findViewById(R.id.sdTaxaSpecies))
          .setText(getColumnValue("taxaSpecies"));
      ((TextView) getActivity().findViewById(R.id.sdDistinctive)).setText(Html.fromHtml(getColumnValue("distinctive")));


      String otherNamesValue = getColumnValue("otherNames");
      TextView otherNames = (TextView) getActivity()
          .findViewById(R.id.sdOtherNames);
      if (!TextUtils.isEmpty(otherNamesValue)) {
        otherNames.setText(Html.fromHtml(otherNamesValue));
      } else {
        TextView otherNamesLabel = (TextView) getActivity()
            .findViewById(R.id.otherNamesLabel);
        otherNamesLabel.setVisibility(View.GONE);
        otherNames.setVisibility(View.GONE);
      }

      String dietValue = getColumnValue("diet");
      TextView diet = (TextView) getActivity().findViewById(R.id.sdDiet);
      if (!TextUtils.isEmpty(dietValue)) {
        diet.setText(Html.fromHtml(dietValue));
      } else {
        TextView dietLabel = (TextView) getActivity()
            .findViewById(R.id.dietLabel);
        dietLabel.setVisibility(View.GONE);
        diet.setVisibility(View.GONE);
      }

      String biteValue = getColumnValue("bite");
      TextView bite = (TextView) getActivity().findViewById(R.id.sdBite);
      if (!TextUtils.isEmpty(biteValue)) {
        bite.setText(Html.fromHtml(biteValue));
      } else {
        TextView biteLabel = (TextView) getActivity()
            .findViewById(R.id.biteLabel);
        biteLabel.setVisibility(View.GONE);
        bite.setVisibility(View.GONE);
      }

      String butterflyStartValue = "";
      TextView flight = (TextView) getActivity().findViewById(R.id.sdFlight);
      if (!TextUtils.isEmpty(butterflyStartValue)) {
        flight.setText(butterflyStartValue + " - ");
      } else {
        TextView flightLabel = (TextView) getActivity()
            .findViewById(R.id.flightLabel);
        flightLabel.setVisibility(View.GONE);
        flight.setVisibility(View.GONE);
      }

      int commercialValue = cursor.getInt(cursor
          .getColumnIndex("isCommercial"));
      TextView commercial = (TextView) getActivity()
          .findViewById(R.id.sdCommercial);
      if (commercialValue > 0) {
        commercial.setText("Yes");
      } else {
        TextView commercialLabel = (TextView) getActivity()
            .findViewById(R.id.commercialLabel);
        commercialLabel.setVisibility(View.GONE);
        commercial.setVisibility(View.GONE);
      }
    }

    // Load images.
    LinearLayout speciesimages = (LinearLayout) getActivity().findViewById(R.id.speciesimages);

    Log.i(TAG, "Loading images");
    if (speciesimages != null && speciesimages.getChildCount() == 0) {

      int imgThumbSize = r.getDimensionPixelSize(R.dimen.species_image_thumbnail_size);
      int imgThumbPadding = r.getDimensionPixelSize(R.dimen.image_detail_pager_margin);

      int counter = 0;
      for (cursorImages.moveToFirst(); !cursorImages.isAfterLast(); cursorImages.moveToNext()) {
        String imagePath = Utilities.SPECIES_IMAGES_FULL_PATH
            + cursorImages.getString(cursorImages.getColumnIndex(FieldGuideDatabase.MEDIA_FILENAME));

        ImageView image = new ImageView(getActivity());
        image.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        image.setPadding(imgThumbPadding, imgThumbPadding,imgThumbPadding, imgThumbPadding);

        image.setImageBitmap(ImageResizer.decodeSampledBitmapFromFile(Utilities.getFullExternalDataPath(getActivity(), imagePath),imgThumbSize, imgThumbSize));
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
        final int imagePosition = counter;
        image.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            final Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
            intent.putExtra(ImageDetailActivity.EXTRA_SPECIES_ID, speciesId);
            intent.putExtra(ImageDetailActivity.EXTRA_IMAGE, imagePosition);
            getActivity().startActivity(intent);
          }
        });

        speciesimages.addView(image);

        ++counter;
      }
      Log.i(TAG, "Loaded " + counter + " images.");
    }

    // Load Location and Depth information
    ImageView depthShoreImage = (ImageView) getActivity().findViewById(R.id.depthShoreImage);
    if (depthShoreImage != null) {
      String[] depthList = getColumnValue("depth").split(";;");

      ImageView depthShallowImage = (ImageView) getActivity()
          .findViewById(R.id.depthShallowImage);
      ImageView depthDeepImage = (ImageView) getActivity()
          .findViewById(R.id.depthDeepImage);

      Drawable layers1 = r.getDrawable(R.drawable.depth_shore_01);
      Drawable layers2 = r.getDrawable(R.drawable.depth_shallow_01);
      Drawable layers3 = r.getDrawable(R.drawable.depth_deep_01);
      for (int i = 0; i < depthList.length; i++) {
        if (depthList[i].startsWith("Shore")) {
          layers1 = r.getDrawable(R.drawable.depth_shore_02);
        }
        if (depthList[i].startsWith("Shallow")) {
          layers2 = r.getDrawable(R.drawable.depth_shallow_02);
        }
        if (depthList[i].startsWith("Deep")) {
          layers3 = r.getDrawable(R.drawable.depth_deep_02);
        }
      }
      depthShoreImage.setImageDrawable(layers1);
      depthShallowImage.setImageDrawable(layers2);
      depthDeepImage.setImageDrawable(layers3);

      // Load Commonly Seen information
      String[] locationList = getColumnValue("location").split(";;");

      ImageView locationImage = (ImageView) getActivity()
          .findViewById(R.id.locationImage);

      Drawable[] layers = new Drawable[locationList.length + 1];
      int locVal = R.drawable.specieslocation_background;
      layers[0] = r.getDrawable(locVal);
      for (int i = 0; i < locationList.length; i++) {
        if (locationList[i].startsWith("Midwater")) {
          locVal = R.drawable.specieslocation_midwater;
        } else if (locationList[i].startsWith("Surface")) {
          locVal = R.drawable.specieslocation_surface;
        } else if (locationList[i].startsWith("Above")) {
          locVal = R.drawable.specieslocation_abovesurface;
        } else if (locationList[i].startsWith("On")) {
          locVal = R.drawable.specieslocation_onornearseafloor;
        } else {
          locVal = R.drawable.specieslocation_background;
        }

        layers[i + 1] = r.getDrawable(locVal);
      }
      LayerDrawable layerDrawable = new LayerDrawable(layers);
      locationImage.setImageDrawable(layerDrawable);
    }


    // Load up the Distribution map
    //ImageView distributionImage = (ImageView) getActivity().findViewById(R.id.distributionImage);
    //if (distributionImage != null) {
    //  String imgpath = Utilities.SPECIES_DISTRIBUTION_MAPS_PATH + getColumnValue("distributionMap");
    //  int width = distributionImage.getMeasuredWidth();
    //  distributionImage.setImageBitmap(ImageResizer.decodeSampledBitmapFromFile(Utilities.getFullExternalDataPath(getActivity(), imgpath), width, width));
    //}
    TextView distribution = (TextView) getActivity().findViewById(R.id.sdDistribution);
    if (distribution != null) {
      distribution.setText(Html.fromHtml(getColumnValue("distribution")));
    }




    // Load Scarcity information
    TextView statusTextDSE = (TextView) getActivity().findViewById(R.id.statusTextDSE);
    if (statusTextDSE != null) {
      TextView tsinfo = (TextView) getActivity().findViewById(R.id.tstatusInfo);
      if (tsinfo != null) {
        tsinfo.setText(Html.fromHtml(r
            .getString(R.string.threatenedstatusinfo)));
        tsinfo.setMovementMethod(LinkMovementMethod.getInstance());
      }

    }

  }

  private String getColumnValue(String columnName) {
    String colValue = cursor.getString(cursor.getColumnIndex(columnName));

    if (columnName.equalsIgnoreCase("nativeStatus")) {
      return ((!TextUtils.isEmpty(colValue)) ? colValue.trim()
          : "Recorded in Australia");
    }

    return ((!TextUtils.isEmpty(colValue)) ? colValue.trim() : "");
  }

  private static int getStatusLevel(String statusText) {

    if (statusText.toLowerCase().startsWith("critically")) {
      return 20;
    } else if (statusText.toLowerCase().startsWith("endangered")) {
      return 40;
    } else if (statusText.toLowerCase().startsWith("vulnerable")) {
      return 60;
    } else if (statusText.toLowerCase().startsWith("near")) {
      return 80;
    }

    return 100;
  }

  private static void setStatusDrawable(String statusText, View statusView) {

    int statusLevel = getStatusLevel(statusText);
    // statusView.setProgress(1);
    // statusView.setMax(100);
    // statusView.setProgress(statusLevel);

    LinearLayout parent = (LinearLayout) statusView.getParent();
    LayoutParams lp = (LayoutParams) parent.getLayoutParams();
    int parentWidth = parent.getWidth();
    int parentHeight = parent.getHeight();

    double statusLevelD = getStatusLevel(statusText);
    int widthD = (int) (statusLevelD / 100) * parent.getMeasuredWidth();

    int width = (statusLevel / 100) * parent.getMeasuredWidth();

    // statusView.setLayoutParams(new LayoutParams(widthD,
    // parent.getMeasuredHeight()));

    switch (statusLevel) {
    case 20:
      // statusView.setProgressDrawable(r.getDrawable(R.drawable.threat_status_critically_endangered));
      statusView
          .setBackgroundResource(R.drawable.threat_status_critically_endangered);
      break;
    case 40:
      // statusView.setProgressDrawable(r.getDrawable(R.drawable.threat_status_endangered));
      statusView
          .setBackgroundResource(R.drawable.threat_status_endangered);
      break;
    case 60:
      // statusView.setProgressDrawable(r.getDrawable(R.drawable.threat_status_vulnerable));
      statusView
          .setBackgroundResource(R.drawable.threat_status_vulnerable);
      break;
    case 80:
      // statusView.setProgressDrawable(r.getDrawable(R.drawable.threat_status_near_threatened));
      statusView
          .setBackgroundResource(R.drawable.threat_status_near_threatened);
      break;
    default:
      // statusView.setProgressDrawable(r.getDrawable(R.drawable.threat_status_not_listed));
      statusView
          .setBackgroundResource(R.drawable.threat_status_not_listed);

      break;
    }
  }

}
