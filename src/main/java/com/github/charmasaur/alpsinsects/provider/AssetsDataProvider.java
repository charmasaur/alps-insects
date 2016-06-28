package au.com.museumvictoria.fieldguide.vic.fork.provider;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileDescriptor;

public final class AssetsDataProvider implements DataProvider {
  private static final String TAG = AssetsDataProvider.class.getSimpleName();

  private static final String DATA_PATH = "data/generaData.json";
  private static final String GROUP_ICON_PATH = "data/images/groups/";
  private static final String SPECIES_THUMBNAIL_PATH = "data/images/species/";
  private static final String SPECIES_IMAGE_PATH = "data/images/species/";

  private final AssetManager assetManager;

  public AssetsDataProvider(AssetManager assetManager) {
    if (assetManager == null) {
      throw new RuntimeException("Null assetManager");
    }
    this.assetManager = assetManager;
  }

  @Nullable
  public InputStream getGroupIcon(String iconLabel) {
    try {
      return assetManager.open(GROUP_ICON_PATH + iconLabel);
    } catch (IOException e) {
      Log.i(TAG, "Exception getting group icon " + iconLabel + ":" + e);
    }
    return null;
  }

  @Nullable
  public InputStream getSpeciesThumbnail(String imageLabel) {
    try {
      return assetManager.open(SPECIES_THUMBNAIL_PATH + imageLabel);
    } catch (IOException e) {
      Log.i(TAG, "Exception getting thumbnail " + imageLabel + ":" + e);
    }
    return null;
  }

  @Nullable
  public ParcelFileDescriptor getSpeciesThumbnailParcelFileDescriptor(String imageLabel) {
    return null;
  }

  @Nullable
  public AssetFileDescriptor getSpeciesThumbnailAssetFileDescriptor(String imageLabel) {
    try {
      return assetManager.openFd(SPECIES_THUMBNAIL_PATH + imageLabel);
    } catch (IOException e) {
      Log.i(TAG, "Exception getting thumbnail PFD " + imageLabel + ":" + e);
    }
    return null;
  }

  @Nullable
  public InputStream getSpeciesImage(String imageLabel) {
    try {
      return assetManager.open(SPECIES_IMAGE_PATH + imageLabel);
    } catch (IOException e) {
      Log.i(TAG, "Exception getting species image " + imageLabel + ":" + e);
    }
    return null;
  }

  @Nullable
  public InputStream getData() {
    try {
      return assetManager.open(DATA_PATH);
    } catch (IOException e) {
      Log.i(TAG, "Exception getting data:" + e);
    }
    return null;
  }
}
