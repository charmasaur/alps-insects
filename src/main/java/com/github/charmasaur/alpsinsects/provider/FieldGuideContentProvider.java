package com.github.charmasaur.alpsinsects.provider;

import java.io.FileNotFoundException;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.github.charmasaur.alpsinsects.db.FieldGuideDatabase;

public class FieldGuideContentProvider extends ContentProvider {
  private static final String TAG = FieldGuideContentProvider.class.getSimpleName();

  public static final String AUTHORITY =
      "com.github.charmasaur.alpsinsects.provider.FieldGuideContentProvider";

  private FieldGuideDatabase mDatabase;

  // UriMatcher stuff
  private static final int SEARCH_SPECIES = 0;
  private static final int GET_DETAILS = 1;
  private static final int SEARCH_SUGGEST = 2;
  private static final int SPECIES_ICON = 3;
  private static final UriMatcher sURIMatcher = buildUriMatcher();

  /**
   * Builds up a UriMatcher for search suggestion and shortcut refresh
   * queries.
   */
  private static UriMatcher buildUriMatcher() {
    UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    // to get details...
    matcher.addURI(AUTHORITY, "fieldguide", SEARCH_SPECIES);
    matcher.addURI(AUTHORITY, "fieldguide/#", GET_DETAILS);
    // to get suggestions...
    matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
    matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

    matcher.addURI(AUTHORITY, SearchManager.SUGGEST_COLUMN_ICON_1, 4);

    return matcher;
  }

  @Override
  public boolean onCreate() {
    mDatabase = FieldGuideDatabase.getInstance(getContext());
    return true;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    // Use the UriMatcher to see what kind of query we have and format the
    // db query accordingly

    Log.w(TAG, "Provider query: " + uri);

    switch (sURIMatcher.match(uri)) {
    case SEARCH_SUGGEST:
      if (selectionArgs == null) {
        throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
      }
      Log.w(TAG, "Provider query with SEARCH_SUGGEST");
      Log.w(TAG, selectionArgs[0]);
      return getSuggestions(selectionArgs[0]);
    case SEARCH_SPECIES:
      if (selectionArgs == null) {
        throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
      }
      Log.w(TAG, "Provider query with SEARCH_SPECIES");
      return search(selectionArgs[0]);
    case GET_DETAILS:
      Log.w(TAG, "Provider query with GET_DETAILS");
      return getSpeciesDetails(uri);
    default:
      Log.w(TAG, "Provider query with DEFAULT");
      throw new IllegalArgumentException("Unknown Uri: " + uri);
    }
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public AssetFileDescriptor openAssetFile(Uri uri, String mode)
      throws FileNotFoundException {
    throw new FileNotFoundException("No asset found: " + uri);
  }

  private Cursor getSuggestions(String query) {
    query = query.toLowerCase();

    // Need _id so that the suggestions can show in a list.
    String[] columns = new String[] { FieldGuideDatabase.SPECIES_ID + " AS _id",
        FieldGuideDatabase.SPECIES_LABEL + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
        FieldGuideDatabase.SPECIES_SEARCHICON + " AS " + SearchManager.SUGGEST_COLUMN_ICON_1,
        FieldGuideDatabase.SPECIES_ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };

    return mDatabase.getSpeciesMatches(query, columns);
  }

  private Cursor search(String query) {
    query = query.toLowerCase();
    String[] columns = new String[] {
        FieldGuideDatabase.SPECIES_ID,
        FieldGuideDatabase.SPECIES_LABEL,
        FieldGuideDatabase.SPECIES_SUBLABEL,
        FieldGuideDatabase.SPECIES_THUMBNAIL };

    return mDatabase.getSpeciesMatches(query);
  }

  private Cursor getSpeciesDetails(Uri uri) {
    String rowId = uri.getLastPathSegment();
    String[] columns = new String[] {
        FieldGuideDatabase.SPECIES_ID,
        FieldGuideDatabase.SPECIES_LABEL,
        FieldGuideDatabase.SPECIES_SUBLABEL,
        FieldGuideDatabase.SPECIES_THUMBNAIL };

    return mDatabase.getSpeciesDetails(rowId, columns);
  }
}
