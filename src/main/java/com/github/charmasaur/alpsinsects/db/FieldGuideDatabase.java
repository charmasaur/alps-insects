package au.com.museumvictoria.fieldguide.vic.fork.db;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.DatabaseUtils.InsertHelper;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;
import android.util.Log;

import au.com.museumvictoria.fieldguide.vic.fork.model.Group;
import au.com.museumvictoria.fieldguide.vic.fork.model.Images;
import au.com.museumvictoria.fieldguide.vic.fork.model.Species;
import au.com.museumvictoria.fieldguide.vic.fork.provider.AssetsProvider;
import au.com.museumvictoria.fieldguide.vic.fork.provider.DataProvider;
import au.com.museumvictoria.fieldguide.vic.fork.provider.DataProviderFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class FieldGuideDatabase {
  private static final String TAG = FieldGuideDatabase.class.getSimpleName();

  // database
  private static final int DATABASE_VERSION = 4;
  private static final String DATABASE_NAME = "fieldguide";
  private static final String SPECIES_TABLE_NAME = "species";
  private static final String IMAGES_TABLE_NAME = "images";
  private static final String GROUPS_TABLE_NAME = "groups";

  // species column mapping
  /**
   * A numeric identifier of the species.
   */
  public static final String SPECIES_ID = "rowid";
  public static final String SPECIES_LABEL = "label";
  public static final String SPECIES_SUBLABEL = "sublabel";
  public static final String SPECIES_SEARCHTEXT = "searchText";
  public static final String SPECIES_THUMBNAIL = "squareThumbnail";
  public static final String SPECIES_DESCRIPTION = "description";
  public static final String SPECIES_TAXA_ORDER = "taxaOrder";
  public static final String SPECIES_TAXA_FAMILY = "taxaFamily";
  public static final String SPECIES_TAXA_GENUS = "taxaGenus";
  public static final String SPECIES_TAXA_SPECIES = "taxaSpecies";
  public static final String SPECIES_LICENSE = "license";
  public static final String SPECIES_LICENSE_LINK = "licenseLink";
  public static final String SPECIES_SEARCHICON = "searchIcon";

  // images column mapping
  /**
   * A numeric identifier of the image.
   */
  public static final String MEDIA_ID = "rowid";
  public static final String MEDIA_FILENAME = "filename";
  public static final String MEDIA_CAPTION = "caption";
  public static final String MEDIA_CREDIT = "credit";
  public static final String MEDIA_SPECIES_ID = "speciesId";

  // groups column mapping
  /**
   * A numeric identifier of the group.
   */
  public static final String GROUPS_ID = "rowid";
  public static final String GROUPS_ORDER = "orderORDER";
  public static final String GROUPS_LABEL = "label";
  public static final String GROUPS_ICON_WHITE_FILENAME = "iconWhiteFilename";
  public static final String GROUPS_ICON_DARK_FILENAME = "iconDarkFilename";
  public static final String GROUPS_ICON_CREDIT = "iconCredit";
  public static final String GROUPS_LICENSE_LINK = "licenseLink";
  public static final String GROUPS_DESCRIPTION = "description";

  private final FieldGuideOpenHelper mDatabaseOpenHelper;

  private static FieldGuideDatabase mInstance = null;

  private final DataProvider dataProvider;
  private SQLiteDatabase mDatabase;

  private FieldGuideDatabase(Context context) {
    dataProvider = DataProviderFactory.getDataProvider(context);
    mDatabaseOpenHelper = new FieldGuideOpenHelper(context);
  }

  public static FieldGuideDatabase getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new FieldGuideDatabase(context.getApplicationContext());
    }
    return mInstance;
  }

  public void open() throws SQLException {
    mDatabase = mDatabaseOpenHelper.getReadableDatabase();
  }

  public void close() {
    mDatabaseOpenHelper.close();
  }

  public long getSpeciesCount() {
    return DatabaseUtils.queryNumEntries(mDatabase, SPECIES_TABLE_NAME);
  }

  @Nullable
  public Cursor getSpeciesMatches(String query) {
    String[] columns = new String[] { SPECIES_ID, SPECIES_LABEL, SPECIES_SUBLABEL,
        SPECIES_THUMBNAIL };

    return getSpeciesMatches(query, columns);
  }

  @Nullable
  public Cursor getSpeciesMatches(String query, String[] columns) {
    Log.w(TAG, "Searching species for " + query);

    String selection = SPECIES_SEARCHTEXT + " LIKE ?";
    String[] selectionArgs = new String[] {"%"+query+"%"};

    return query(SPECIES_TABLE_NAME, columns, selection, selectionArgs, null, SPECIES_LABEL);
  }

  /**
   * Returns a {@link Cursor} pointing to the species in a particular group.
   *
   * @param groupOrder the {@link GROUPS_ORDER} value of the group to query
   * @param columns the columns to fetch
   */
  @Nullable
  public Cursor getSpeciesInGroup(String groupOrder, String[] columns) {
    String selection = SPECIES_TAXA_ORDER + " = ?";
    String[] selectionArgs = new String[] { groupOrder };

    return query(
        SPECIES_TABLE_NAME,
        columns,
        SPECIES_TAXA_ORDER + " = ?",
        new String[] { groupOrder },
        null /* groupBy */,
        SPECIES_LABEL /* orderBy */);
  }

  @Nullable
  public Cursor getSpeciesGroups(String[] columns) {
    Log.i(TAG, "Getting species groups");

    return query(GROUPS_TABLE_NAME, columns, null, null, null, null);
  }

  /**
   * Returns a Cursor positioned at the species detail specified by rowId
   *
   * @param identifier species identifier
   * @param columns the columns to include, if null then all are included
   * @return {@link Cursor} positioned to matching word, or null if not found
   */
  @Nullable
  public Cursor getSpeciesDetails(String identifier, String[] columns) {
    String selection = SPECIES_ID + " = ?";
    String[] selectionArgs = new String[] { identifier };

    return query(SPECIES_TABLE_NAME, columns, selection, selectionArgs, null, null);
  }

  public Cursor getSpeciesImages(String speciesId, String[] columns) {
    Log.w(TAG, "Getting species images for: " + speciesId);

    return query(IMAGES_TABLE_NAME, columns, MEDIA_SPECIES_ID + " = ?", new String[] { speciesId },
        null, null);
  }

  public Cursor getImageDetails(String imageId, String[] columns) {
    Log.w(TAG, "Getting image details for: " + imageId);

    return query(IMAGES_TABLE_NAME, columns, MEDIA_ID + " = ?", new String[] { imageId }, null,
        null);
  }

  @Nullable
  public Cursor getGroupDetails(String order, String[] columns) {
    String selection = GROUPS_ORDER + " = ?";
    String[] selectionArgs = new String[] { order };

    return query(GROUPS_TABLE_NAME, columns, selection, selectionArgs, null, null);
  }

  /**
   * Performs a database query.
   *
   * @param columns the columns to return
   * @param selection the selection clause
   * @param selectionArgs selection arguments for "?" components in the selection
   * @param groupBy the GROUP BY column name
   * @param orderBy the ORDER BY column name
   * @return a {@link Cursor} over all rows matching the query
   */
  @Nullable
  private Cursor query(String tables, String[] columns, String selection, String[] selectionArgs,
      String groupBy, String orderBy) {
    /*
     * The SQLiteBuilder provides a map for all possible columns requested
     * to actual columns in the database, creating a simple column alias
     * mechanism by which the ContentProvider does not need to know the real
     * column names
     */
    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
    builder.setTables(tables);

    Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns, selection,
        selectionArgs, groupBy, null, orderBy);

    if (cursor == null) {
      Log.i(TAG, "Null cursor");
      return null;
    } else if (!cursor.moveToFirst()) {
      Log.i(TAG, "Empty cursor");
      cursor.close();
      return null;
    }

    Log.w(TAG, "Returning " + cursor.getCount() + " species");

    return cursor;
  }

  private final class FieldGuideOpenHelper extends SQLiteOpenHelper {
    private static final String SPECIES_TABLE_CREATE = "CREATE TABLE " + SPECIES_TABLE_NAME + " ("
        + SPECIES_LABEL + " TEXT, "
        + SPECIES_SUBLABEL + " TEXT, "
        + SPECIES_SEARCHTEXT + " TEXT, "
        + SPECIES_THUMBNAIL + " TEXT, "
        + SPECIES_DESCRIPTION + " TEXT, "
        + SPECIES_TAXA_ORDER + " TEXT, "
        + SPECIES_TAXA_FAMILY + " TEXT, "
        + SPECIES_TAXA_GENUS + " TEXT, "
        + SPECIES_TAXA_SPECIES + " TEXT, "
        + SPECIES_LICENSE + " TEXT, "
        + SPECIES_LICENSE_LINK + " TEXT, "
        + SPECIES_SEARCHICON + " TEXT); ";
    private static final String IMAGES_TABLE_CREATE = "CREATE TABLE " + IMAGES_TABLE_NAME + " ("
        + MEDIA_FILENAME + " TEXT, "
        + MEDIA_CAPTION + " TEXT, "
        + MEDIA_CREDIT + " TEXT, "
        + MEDIA_SPECIES_ID + " TEXT); ";
    private static final String GROUPS_TABLE_CREATE = "CREATE TABLE " + GROUPS_TABLE_NAME + " ("
        + GROUPS_ORDER + " TEXT, "
        + GROUPS_LABEL + " TEXT, "
        + GROUPS_ICON_WHITE_FILENAME + " TEXT, "
        + GROUPS_ICON_DARK_FILENAME + " TEXT, "
        + GROUPS_ICON_CREDIT + " TEXT, "
        + GROUPS_LICENSE_LINK + " TEXT, "
        + GROUPS_DESCRIPTION + " TEXT); ";

    private final Context mHelperContext;

    public FieldGuideOpenHelper(Context context) {

      // Uncomment the following line for SQL debug statements
      // make sure you comment out the following 'super' statment
      //super(context, DATABASE_NAME, new SQLiteCursorFactory(true), DATABASE_VERSION);
      super(context, DATABASE_NAME, null, DATABASE_VERSION);

      mHelperContext = context;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
      Log.i(TAG, "onCreate");

      db.execSQL(SPECIES_TABLE_CREATE);
      db.execSQL(IMAGES_TABLE_CREATE);
      db.execSQL(GROUPS_TABLE_CREATE);

      try {
        loadData(db);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.i(TAG, "onUpgrade: from version " + oldVersion + " to " + newVersion);
      db.execSQL("DROP TABLE IF EXISTS " + SPECIES_TABLE_NAME);
      db.execSQL("DROP TABLE IF EXISTS " + IMAGES_TABLE_NAME);
      db.execSQL("DROP TABLE IF EXISTS " + GROUPS_TABLE_NAME);
      onCreate(db);
    }

    private String getInsertSQL(String tableName, List<String> columnNames,
        Map<String, Integer> columnIndices) {
      StringBuilder columnsBuilder = new StringBuilder()
          .append("INSERT INTO ").append(tableName).append(" (");
      StringBuilder valuesBuilder = new StringBuilder(") VALUES (");
      int index = 1;
      for (String columnName : columnNames) {
        columnIndices.put(columnName, index);

        columnsBuilder.append(columnName);
        valuesBuilder.append("?");
        if (index < columnNames.size()) {
          columnsBuilder.append(", ");
          valuesBuilder.append(", ");
        }
        ++index;
      }
      valuesBuilder.append(");");
      return columnsBuilder.append(valuesBuilder.toString()).toString();
    }

    private void loadData(SQLiteDatabase db) throws IOException, JSONException {
      Log.i(TAG, "Loading database");

      Map<String, Integer> speciesColumns = new HashMap<>();
      String speciesSQL = getInsertSQL(
          SPECIES_TABLE_NAME,
          Arrays.asList(
              SPECIES_LABEL,
              SPECIES_SUBLABEL,
              SPECIES_SEARCHTEXT,
              SPECIES_THUMBNAIL,
              SPECIES_DESCRIPTION,
              SPECIES_TAXA_ORDER,
              SPECIES_TAXA_FAMILY,
              SPECIES_TAXA_GENUS,
              SPECIES_TAXA_SPECIES,
              SPECIES_LICENSE,
              SPECIES_LICENSE_LINK,
              SPECIES_SEARCHICON),
          speciesColumns);

      Map<String, Integer> imagesColumns = new HashMap<>();
      String imageSQL = getInsertSQL(
          IMAGES_TABLE_NAME,
          Arrays.asList(MEDIA_FILENAME, MEDIA_CAPTION, MEDIA_CREDIT, MEDIA_SPECIES_ID),
          imagesColumns);

      Map<String, Integer> groupsColumns = new HashMap<>();
      String groupsSQL = getInsertSQL(
          GROUPS_TABLE_NAME,
          Arrays.asList(
            GROUPS_ORDER,
            GROUPS_LABEL,
            GROUPS_ICON_WHITE_FILENAME,
            GROUPS_ICON_DARK_FILENAME,
            GROUPS_ICON_CREDIT,
            GROUPS_LICENSE_LINK,
            GROUPS_DESCRIPTION),
          groupsColumns);

      Log.i(TAG, "Reading assets");
      JsonReader reader = new JsonReader(new InputStreamReader(dataProvider.getData()));

      JsonParser parser = new JsonParser();
      JsonObject json = parser.parse(reader).getAsJsonObject();
      double version = json.get("version").getAsDouble();
      JsonArray splist = json.get("species_data").getAsJsonArray();
      JsonArray groupsList = json.get("groups_data").getAsJsonArray();

      db.beginTransaction();

      SQLiteStatement speciesStatement = db.compileStatement(speciesSQL);
      SQLiteStatement imageStatement = db.compileStatement(imageSQL);
      SQLiteStatement groupsStatement = db.compileStatement(groupsSQL);

      Log.i(TAG, "Populating database");
      try {
        Gson gson = new Gson();
        for (int i = 0; i < splist.size(); i++) {
          Species s = gson.fromJson(splist.get(i), Species.class);

          Log.i(TAG, s.getLabel() + s.getSublabel() + s.getSearchText());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_LABEL), s.getLabel());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_SUBLABEL), s.getSublabel());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_SEARCHTEXT), s.getSearchText());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_THUMBNAIL),
              s.getSquareThumbnail());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_DESCRIPTION), s.getDescription());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_TAXA_ORDER), s.getOrder());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_TAXA_FAMILY), s.getFamily());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_TAXA_GENUS), s.getGenus());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_TAXA_SPECIES), s.getSpecies());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_LICENSE), s.getLicense());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_LICENSE_LINK),
              s.getLicenseLink());
          maybeBind(speciesStatement, speciesColumns.get(SPECIES_SEARCHICON),
              "content://" + AssetsProvider.AUTHORITY + "/" + s.getSquareThumbnail());

          long speciesId = speciesStatement.executeInsert();
          speciesStatement.clearBindings();

          Iterator<Images> imgs = s.getImages().iterator();
          while (imgs.hasNext()) {
            Images img = imgs.next();

            maybeBind(imageStatement, imagesColumns.get(MEDIA_FILENAME), img.getFilename());
            maybeBind(imageStatement, imagesColumns.get(MEDIA_CAPTION), img.getImageDescription());
            maybeBind(imageStatement, imagesColumns.get(MEDIA_CREDIT), img.getCredit());
            maybeBind(imageStatement, imagesColumns.get(MEDIA_SPECIES_ID),
                Long.toString(speciesId));

            imageStatement.executeInsert();
            imageStatement.clearBindings();
          }
        }

        for (int i = 0; i < groupsList.size(); ++i) {
          Group group = gson.fromJson(groupsList.get(i), Group.class);

          maybeBind(groupsStatement, groupsColumns.get(GROUPS_ORDER), group.getOrder());
          maybeBind(groupsStatement, groupsColumns.get(GROUPS_LABEL), group.getLabel());
          maybeBind(groupsStatement, groupsColumns.get(GROUPS_ICON_WHITE_FILENAME),
              group.getIconWhiteFilename());
          maybeBind(groupsStatement, groupsColumns.get(GROUPS_ICON_DARK_FILENAME),
              group.getIconDarkFilename());
          maybeBind(groupsStatement, groupsColumns.get(GROUPS_ICON_CREDIT),
              group.getIconCredit());
          maybeBind(groupsStatement, groupsColumns.get(GROUPS_LICENSE_LINK),
              group.getLicenseLink());
          maybeBind(groupsStatement, groupsColumns.get(GROUPS_DESCRIPTION),
              group.getDescription());

          groupsStatement.executeInsert();
          groupsStatement.clearBindings();
        }


        db.setTransactionSuccessful();

      } finally {
        groupsStatement.close();
        imageStatement.close();
        speciesStatement.close();
        db.endTransaction();
      }

      Log.i(TAG, "Done populating database");
    }

    private void maybeBind(SQLiteStatement statement, int column, @Nullable String string) {
      if (string == null) {
        return;
      }
      statement.bindString(column, string);
    }
  }

  private static final class SQLiteCursorFactory implements CursorFactory {
    private boolean debugQueries = false;

    public SQLiteCursorFactory() {
      this.debugQueries = false;
    }

    public SQLiteCursorFactory(boolean debugQueries) {
      this.debugQueries = debugQueries;
    }

    @Override
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable,
        SQLiteQuery query) {
      if (debugQueries) {
        Log.d(TAG, "SQL: " + query.toString());
      }
      return new SQLiteCursor(db, masterQuery, editTable, query);
    }
  }
}
