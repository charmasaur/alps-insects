package com.github.charmasaur.alpsinsects.ui.fragments;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.charmasaur.alpsinsects.R;
import com.github.charmasaur.alpsinsects.db.FieldGuideDatabase;
import com.github.charmasaur.alpsinsects.provider.DataProviderFactory;
import com.github.charmasaur.alpsinsects.util.ImageResizer;
import com.github.charmasaur.alpsinsects.util.NonBrokenImageViewTouch;

/**
 * Shows an image that can be panned and zoomed.
 *
 * <p>Activities containing this fragment may optionally implement {@link Callback} if they wish to
 * control the hiding and showing of labels.
 */
public class ImageDetailFragment extends Fragment {
  private static final String TAG = ImageDetailFragment.class.getSimpleName();

  private static final String IMAGE_PATH_DATA_EXTRA = "imagePath";
  private static final String IMAGE_ID_EXTRA = "imageId";

  /**
   * Callback interface to communicate about the showing and hiding of labels.
   */
  public interface Callback {
    /**
     * Listener to be notified of changes to visibillity of labels.
     */
    interface Listener {
      void onVisibilityChanged(boolean showLabels);
    }

    /**
     * Called when the image is tapped.
     */
    void onImageTapped();

    /**
     * Registers a listener to be notified of visibility changes. Must call the listener
     * synchronously with the current state.
     */
    void registerLabelVisibilityListener(Listener listener);

    /**
     * Unregisters a listener. Must not call the listener.
     */
    void unregisterLabelVisibilityListener(Listener listener);
  }

  @Nullable
  private Callback callback;

  private String imagePath;
  @Nullable
  private String caption;
  @Nullable
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

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    try {
      callback = (Callback) activity;
    } catch (ClassCastException e) {
      callback = null;
    }
  }

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
    imagePath = getStringOrNull(cursor, FieldGuideDatabase.MEDIA_FILENAME);
    if (imagePath == null) {
      throw new RuntimeException("Missing image path for image: "
          + getArguments().getString(IMAGE_ID_EXTRA));
    }
    caption = getStringOrNull(cursor, FieldGuideDatabase.MEDIA_CAPTION);
    credit = getStringOrNull(cursor, FieldGuideDatabase.MEDIA_CREDIT);
    cursor.close();
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
      if (callback != null) {
        callback.registerLabelVisibilityListener(callbackListener);
      }
      return v;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // set the default image display type
    mImageView.setDisplayType(DisplayType.FIT_IF_BIGGER);
    mImageView.setImageBitmap(ImageResizer.decodeSampledBitmapFromStream(
        DataProviderFactory.getDataProvider(getActivity().getApplicationContext())
            .getSpeciesImage(imagePath)));
    if (caption != null) {
      mImageDescription.setVisibility(View.VISIBLE);
      mImageDescription.setText(Html.fromHtml(caption));
    } else {
      mImageDescription.setVisibility(View.GONE);
    }
    if (credit != null) {
      mImageCredit.setVisibility(View.VISIBLE);
      mImageCredit.setText(Html.fromHtml(credit));
    } else {
      mImageCredit.setVisibility(View.GONE);
    }

    mImageView.setSingleTapListener(new ImageViewTouch.OnImageViewTouchSingleTapListener() {
      @Override
      public void onSingleTapConfirmed() {
        if (callback == null) {
          if (imageDetailsLayout.getVisibility() == View.VISIBLE) {
            imageDetailsLayout.setVisibility(View.INVISIBLE);
          } else {
            imageDetailsLayout.setVisibility(View.VISIBLE);
          }
        } else {
          callback.onImageTapped();
        }
      }
    });
  }

  @Override
  public void onDestroyView() {
    if (callback != null) {
      callback.unregisterLabelVisibilityListener(callbackListener);
    }
    super.onDestroyView();
  }

  @Override
  public void onDetach() {
    callback = null;
    super.onDetach();
  }

  private static String getStringOrNull(Cursor cursor, String columnName) {
    int columnIndex = cursor.getColumnIndex(columnName);
    if (cursor.isNull(columnIndex)) {
      return null;
    }
    return cursor.getString(columnIndex);
  }

  private final Callback.Listener callbackListener = new Callback.Listener() {
    @Override
    public void onVisibilityChanged(boolean showLabels) {
      imageDetailsLayout.setVisibility(showLabels ? View.VISIBLE : View.INVISIBLE);
    }
  };
}
