package com.github.charmasaur.alpsinsects.provider;

import android.content.res.AssetFileDescriptor;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;

import java.io.InputStream;
import java.io.FileDescriptor;

/**
 * Provides field guide data.
 */
public interface DataProvider {
  @Nullable
  InputStream getGroupIcon(String iconLabel);

  @Nullable
  InputStream getSpeciesThumbnail(String imageLabel);

  /**
   * Returns a file descriptor of an image blob.
   */
  @Nullable
  ParcelFileDescriptor getSpeciesThumbnailParcelFileDescriptor(String imageLabel);

  /**
   * Returns a file descriptor of an image slice.
   */
  @Nullable
  AssetFileDescriptor getSpeciesThumbnailAssetFileDescriptor(String imageLabel);

  @Nullable
  InputStream getSpeciesImage(String imageLabel);

  @Nullable
  InputStream getData();
}
