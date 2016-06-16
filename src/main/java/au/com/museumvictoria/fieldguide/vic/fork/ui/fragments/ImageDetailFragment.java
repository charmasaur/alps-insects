package au.com.museumvictoria.fieldguide.vic.fork.ui.fragments;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;
import au.com.museumvictoria.fieldguide.vic.fork.ui.ImageDetailActivity;
import au.com.museumvictoria.fieldguide.vic.fork.util.NonBrokenImageViewTouch;
import au.com.museumvictoria.fieldguide.vic.fork.util.Utilities;

public class ImageDetailFragment extends Fragment {
  private static final String TAG = ImageDetailFragment.class.getSimpleName();

  private static final String IMAGE_PATH_DATA_EXTRA = "imagePath";
  private static final String IMAGE_ID_EXTRA = "imageId";

  private String imagePath;
  private String caption;
  private String credit;

  private NonBrokenImageViewTouch mImageView;
  private TextView mImageDescription;
  private TextView mImageCredit;
  private RelativeLayout imageDetailsLayout;

  public static ImageDetailFragment newInstance(String imageId) {
    ImageDetailFragment f = new ImageDetailFragment();

    Bundle args = new Bundle();
    args.putString(IMAGE_ID_EXTRA, imageId);
    f.setArguments(args);

    return f;
  }

  public ImageDetailFragment() {}

  /**
   * Populate image number from extra, use the convenience factory method
   * {@link ImageDetailFragment#newInstance(int)} to create this fragment.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FieldGuideDatabase db = FieldGuideDatabase.getInstance(getActivity().getApplicationContext());
    if (getArguments() == null) {
      throw new RuntimeException("Missing arguments");
    }
    Cursor cursor = db.getImageDetails(getArguments().getString(IMAGE_ID_EXTRA), null);
    imagePath = cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.MEDIA_FILENAME));
    caption = cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.MEDIA_CAPTION));
    credit = cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.MEDIA_CREDIT));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      // Inflate and locate the main ImageView
      final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
      mImageView = (NonBrokenImageViewTouch) v.findViewById(R.id.imageView);
      mImageDescription = (TextView) v.findViewById(R.id.imageDescription);
      mImageCredit = (TextView) v.findViewById(R.id.imageCredit);
      imageDetailsLayout = (RelativeLayout) v.findViewById(R.id.imageDetailsLayout);
      return v;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Drawable d = null;
    try {
      d = Drawable.createFromStream(Utilities.getAssetInputStream(getActivity(),
          Utilities.SPECIES_IMAGES_FULL_PATH + imagePath), null);
    } catch (Exception e) {
      Log.i(TAG, "Failed to load drawable");
    }

    // set the default image display type
    mImageView.setDisplayType( DisplayType.FIT_IF_BIGGER );
    mImageView.setImageDrawable(d);
    mImageDescription.setText(Html.fromHtml(caption));
    mImageCredit.setText(Html.fromHtml(credit));

    mImageView.setSingleTapListener(new ImageViewTouch.OnImageViewTouchSingleTapListener() {
      @Override
      public void onSingleTapConfirmed() {
        if (imageDetailsLayout.getVisibility() == View.VISIBLE) {
          imageDetailsLayout.setVisibility(View.INVISIBLE);
        } else {
          imageDetailsLayout.setVisibility(View.VISIBLE);
        }
      }
    });
  }
}
