package au.com.museumvictoria.fieldguide.vic.fork.provider;

import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class AssetsProvider extends ContentProvider {
  public static final String AUTHORITY =
      "au.com.museumvictoria.fieldguide.vic.fork.provider.FieldGuideAssetsProvider";

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    return null;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public ParcelFileDescriptor openFile(Uri uri, String mode)
      throws FileNotFoundException {
    ParcelFileDescriptor fd = DataProviderFactory.getDataProvider(getContext())
        .getSpeciesThumbnailParcelFileDescriptor(uri.getPath().substring(1));
    if (fd == null) {
      throw new FileNotFoundException("No asset found: " + uri);
    }
    return fd;
  }

  @Override
  public AssetFileDescriptor openAssetFile(Uri uri, String mode)
      throws FileNotFoundException {
    AssetFileDescriptor fd = DataProviderFactory.getDataProvider(getContext())
        .getSpeciesThumbnailAssetFileDescriptor(uri.getPath().substring(1));
    if (fd == null) {
      throw new FileNotFoundException("No asset found: " + uri);
    }
    return fd;
  }
}
