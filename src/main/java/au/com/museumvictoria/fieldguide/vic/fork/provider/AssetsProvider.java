package au.com.museumvictoria.fieldguide.vic.fork.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class AssetsProvider extends ContentProvider {

  private AssetManager assetManager;
  public static String AUTHORITY = "au.com.museumvictoria.fieldguide.vic.fork.FieldGuideAssestsProvider";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/fieldguide");

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getType(Uri uri) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean onCreate() {
    assetManager = getContext().getAssets();
        return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {
    // TODO Auto-generated method stub
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
