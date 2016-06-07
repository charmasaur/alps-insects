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
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import au.com.museumvictoria.fieldguide.vic.fork.model.Images;
import au.com.museumvictoria.fieldguide.vic.fork.model.Species;
import au.com.museumvictoria.fieldguide.vic.fork.util.Utilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class FieldGuideDatabase {
  private static final String TAG = FieldGuideDatabase.class.getSimpleName();

  // database
  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "fieldguide";
  private static final String SPECIES_TABLE_NAME = "species";
  private static final String IMAGES_TABLE_NAME = "images";

  // column mapping
  public static final String SPECIES_IDENTIFIER = "identifier";
  public static final String SPECIES_LABEL = "label";
  public static final String SPECIES_SUBLABEL = "sublabel";
  public static final String SPECIES_SEARCHTEXT = "searchText";
  public static final String SPECIES_THUMBNAIL = "squareThumbnail";
  public static final String SPECIES_GROUP = "groupLabel";
  public static final String SPECIES_SUBGROUP = "subgroupLabel";
  public static final String SPECIES_DESCRIPTION = "description";
  public static final String SPECIES_BITE = "bite";
  public static final String SPECIES_BIOLOGY = "biology";
  public static final String SPECIES_DIET = "diet";
  public static final String SPECIES_HABITAT = "habitat";
  public static final String SPECIES_NATIVE_STATUS = "nativeStatus";
  public static final String SPECIES_DISTINCTIVE = "distinctive";
  public static final String SPECIES_DISTRIBUTION = "distribution";
  public static final String SPECIES_DEPTH = "depth";
  public static final String SPECIES_LOCATION = "location";
  public static final String SPECIES_IS_COMMERCIAL = "isCommercial";
  public static final String SPECIES_TAXA_PHYLUM = "taxaPhylum";
  public static final String SPECIES_TAXA_CLASS = "taxaClass";
  public static final String SPECIES_TAXA_ORDER = "taxaOrder";
  public static final String SPECIES_TAXA_FAMILY = "taxaFamily";
  public static final String SPECIES_TAXA_GENUS = "taxaGenus";
  public static final String SPECIES_TAXA_SPECIES = "taxaSpecies";
  public static final String SPECIES_TAXA_SUBSPECIES = "taxaSubspecies";
  public static final String SPECIES_COMMON_NAMES = "commonNames";
  public static final String SPECIES_OTHER_NAMES = "otherNames";
  public static final String SPECIES_SEARCHICON = "searchIcon";
  public static final String SPECIES_BUTTERFLY_START = "butterflyStart";
  public static final String SPECIES_BUTTERFLY_END = "butterflyEnd";
  public static final String SPECIES_DISTRIBUTION_MAP = "distributionMap";

  public static final String MEDIA_FILENAME = "filename";
  public static final String MEDIA_CAPTION = "caption";
  public static final String MEDIA_CREDIT = "credit";
  public static final String MEDIA_IDENTIFIER = "identifier";

  private final FieldGuideOpenHelper mDatabaseOpenHelper;

  private static FieldGuideDatabase mInstance = null;

  private SQLiteDatabase mDatabase;

  private int currCount = 0;
  private int totalCount = 0;

  private FieldGuideDatabase(Context context) {
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
    //mDatabaseOpenHelper.close();
  }

  public int getCurrCount() {
    return currCount;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public long getSpeciesCount() {
    return DatabaseUtils.queryNumEntries(mDatabase, SPECIES_TABLE_NAME);
  }

  @Nullable
  public Cursor getSpeciesMatches(String query) {
    String[] columns = new String[] { BaseColumns._ID, SPECIES_IDENTIFIER, SPECIES_LABEL, SPECIES_SUBLABEL, SPECIES_THUMBNAIL };

    return getSpeciesMatches(query, columns);
  }

  @Nullable
  public Cursor getSpeciesMatches(String query, String[] columns) {
    Log.w(TAG, "Searching species for " + query);

    String selection = SPECIES_SEARCHTEXT + " LIKE ?";
    String[] selectionArgs = new String[] {"%"+query+"%"};

    return query(SPECIES_TABLE_NAME, columns, selection, selectionArgs, null, SPECIES_LABEL);
  }

  @Nullable
  public Cursor getSpeciesList(String groupLabel) {
    String[] columns = new String[] { BaseColumns._ID, SPECIES_IDENTIFIER, SPECIES_LABEL, SPECIES_SUBLABEL, SPECIES_THUMBNAIL, SPECIES_SUBGROUP };
    String selection = null;
    String[] selectionArgs = null;
    String groupBy = null;
    String orderBy = SPECIES_LABEL;

    // get species for a given group if available
    // or default to all species
    if (groupLabel != null && !groupLabel.equals("ALL")) {
      // do nothing
      Log.w(TAG, "Getting species list for '" + groupLabel + "'");
      selection = SPECIES_GROUP + " = ?";
      selectionArgs = new String[] { groupLabel };
      orderBy = SPECIES_SUBGROUP;
    }

    Cursor cursor = query(SPECIES_TABLE_NAME, columns, selection, selectionArgs, groupBy, orderBy);

    if (cursor == null) {
      Log.w(TAG, "Cursor is null");
      return null;
    } else if (!cursor.moveToFirst()) {
      cursor.close();
      Log.w(TAG, "Cursor has nothing. closing.");
      return null;
    }

    return cursor;
  }

  @Nullable
  public Cursor getSpeciesGroups() {
    Log.w(TAG, "Getting species groups");

    // TODO: Does this get each group once, or can it duplicate them?
    String[] columns = new String[] { BaseColumns._ID, SPECIES_GROUP };
    Cursor cursor = query(SPECIES_TABLE_NAME, columns, null, null, SPECIES_GROUP, SPECIES_GROUP);

    if (cursor == null) {
      Log.w(TAG, "Species Group Cursor is null");
      return null;
    } else if (!cursor.moveToFirst()) {
      cursor.close();
      Log.w(TAG, "Species Group Cursor has nothing. closing.");
      return null;
    }

    return cursor;
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
    //String selection = SPECIES_IDENTIFIER + " = ?";
    String selection = BaseColumns._ID + " = ?";
    String[] selectionArgs = new String[] { identifier };

    //return query(SPECIES_TABLE_NAME + "," + MEDIA_TABLE_NAME, columns, selection, selectionArgs, null, null);
    return query(SPECIES_TABLE_NAME, columns, selection, selectionArgs, null, null);
  }

  public Cursor getSpeciesImages(String identifier) {
    String selection = SPECIES_IDENTIFIER + " = ?";
    String[] selectionArgs = new String[] { identifier };

    Log.w(TAG, "Getting species images for: " + identifier);

    //return query(SPECIES_TABLE_NAME + "," + MEDIA_TABLE_NAME, columns, selection, selectionArgs, null, null);
    return query(IMAGES_TABLE_NAME, null, selection, selectionArgs, null, null);
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
    // builder.setTables(SPECIES_TABLE_NAME +
    // " LEFT OUTER JOIN bar ON ("+SPECIES_TABLE_NAME+".identifier = "+MEDIA_TABLE_NAME+".identifier)");
    // builder.setTables(SPECIES_TABLE_NAME + "," + MEDIA_TABLE_NAME);
    builder.setTables(tables);

    Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns, selection,
        selectionArgs, groupBy, null, orderBy);

    if (cursor == null) {
      return null;
    } else if (!cursor.moveToFirst()) {
      cursor.close();
      return null;
    }

    Log.w(TAG, "Returning " + cursor.getCount() + " species");

    return cursor;
  }

  private Species cursorToSpecies(Cursor cursor) {
    Species sp = new Species();

    sp.setIdentifier(cursor.getString(1));
    sp.setLabel(cursor.getString(2));
    sp.setSublabel(cursor.getString(3));
    sp.setSquareThumbnail(cursor.getString(4));

    return sp;
  }

  private final class FieldGuideOpenHelper extends SQLiteOpenHelper {
    private static final String SPECIES_TABLE_CREATE = "CREATE TABLE "
        + SPECIES_TABLE_NAME
        + " (_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL, identifier TEXT, label TEXT, sublabel TEXT, searchText TEXT, squareThumbnail TEXT, searchIcon TEXT, groupLabel TEXT, subgroupLabel TEXT, description TEXT, bite TEXT, biology TEXT, diet TEXT, habitat TEXT, nativeStatus TEXT, distinctive TEXT, distribution TEXT, depth TEXT, location TEXT, isCommercial BOOL, taxaPhylum TEXT, taxaClass TEXT, taxaOrder TEXT, taxaFamily TEXT, taxaGenus TEXT, taxaSpecies TEXT, taxaSubspecies TEXT, commonNames TEXT, otherNames TEXT, butterflyStart TEXT, butterflyEnd TEXT, distributionMap TEXT); ";
    private static final String IMAGES_TABLE_CREATE = "CREATE TABLE "
        + IMAGES_TABLE_NAME
        + " (_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL, identifier TEXT, filename TEXT, caption TEXT, credit TEXT); ";

    private final Context mHelperContext;

    public FieldGuideOpenHelper(Context context) {

      // Uncomment the following line for SQL debug statements
      // make sure you comment out the following 'super' statment
      //super(context, DATABASE_NAME, new SQLiteCursorFactory(true), DATABASE_VERSION);
      super(context, DATABASE_NAME, null, DATABASE_VERSION);

      mHelperContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      Log.i(TAG, "onCreate");

      db.execSQL(SPECIES_TABLE_CREATE);
      db.execSQL(IMAGES_TABLE_CREATE);

      Log.i(TAG, "Starting thread to load database");
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            loadData(db);
          } catch (IOException e) {
            throw new RuntimeException(e);
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }
      }).start();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.i(TAG, "onUpgrade: from version " + oldVersion + " to " + newVersion);
      db.execSQL("DROP TABLE IF EXISTS " + SPECIES_TABLE_NAME);
      db.execSQL("DROP TABLE IF EXISTS " + IMAGES_TABLE_NAME);
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
              SPECIES_IDENTIFIER,
              SPECIES_LABEL,
              SPECIES_SUBLABEL,
              SPECIES_SEARCHTEXT,
              SPECIES_THUMBNAIL,
              SPECIES_GROUP,
              SPECIES_SUBGROUP,
              SPECIES_DESCRIPTION,
              SPECIES_BITE,
              SPECIES_BIOLOGY,
              SPECIES_DIET,
              SPECIES_HABITAT,
              SPECIES_NATIVE_STATUS,
              SPECIES_DISTINCTIVE,
              SPECIES_DISTRIBUTION,
              SPECIES_DEPTH,
              SPECIES_LOCATION,
              SPECIES_IS_COMMERCIAL,
              SPECIES_TAXA_PHYLUM,
              SPECIES_TAXA_CLASS,
              SPECIES_TAXA_ORDER,
              SPECIES_TAXA_FAMILY,
              SPECIES_TAXA_GENUS,
              SPECIES_TAXA_SPECIES,
              SPECIES_TAXA_SUBSPECIES,
              SPECIES_COMMON_NAMES,
              SPECIES_OTHER_NAMES,
              SPECIES_SEARCHICON,
              SPECIES_BUTTERFLY_START,
              SPECIES_BUTTERFLY_END,
              SPECIES_DISTRIBUTION_MAP),
          speciesColumns);

      Map<String, Integer> imagesColumns = new HashMap<>();
      String imageSQL = getInsertSQL(
          IMAGES_TABLE_NAME,
          Arrays.asList(MEDIA_FILENAME, MEDIA_CAPTION, MEDIA_CREDIT, MEDIA_IDENTIFIER),
          imagesColumns);

      Log.i(TAG, "Reading assets");
      JsonReader reader = new JsonReader(new InputStreamReader(
            Utilities.getAssetInputStream(mHelperContext, Utilities.SPECIES_DATA_FILE)));

      JsonParser parser = new JsonParser();
      JsonObject json = parser.parse(reader).getAsJsonObject();
      double version = json.get("version").getAsDouble();
      JsonArray splist = json.get("data").getAsJsonArray();

      totalCount = splist.size();

      db.beginTransaction();

      SQLiteStatement speciesStatement = db.compileStatement(speciesSQL);
      SQLiteStatement imageStatement = db.compileStatement(imageSQL);

      Log.i(TAG, "Populating database");
      try {
        Gson gson = new Gson();
        for (int i=0; i<splist.size(); i++) {
          Species s = gson.fromJson(splist.get(i), Species.class);

          speciesStatement.bindString(speciesColumns.get(SPECIES_IDENTIFIER), s.getIdentifier());
          speciesStatement.bindString(speciesColumns.get(SPECIES_LABEL), s.getLabel());
          speciesStatement.bindString(speciesColumns.get(SPECIES_SUBLABEL), s.getSublabel());
          speciesStatement.bindString(speciesColumns.get(SPECIES_SEARCHTEXT), s.getSearchText());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_THUMBNAIL), s.getSquareThumbnail());
          speciesStatement.bindString(speciesColumns.get(SPECIES_GROUP), s.getGroup());
          speciesStatement.bindString(speciesColumns.get(SPECIES_SUBGROUP), s.getSubgroup());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_DESCRIPTION), s.getDetails().getDescription());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_BITE), s.getDetails().getBite());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_BIOLOGY), s.getDetails().getBiology());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_DIET), s.getDetails().getDiet());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_HABITAT), s.getDetails().getHabitat());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_NATIVE_STATUS), s.getDetails().getNativeStatus());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_DISTINCTIVE), s.getDetails().getDistinctive());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_DISTRIBUTION), s.getDetails().getDistribution());
          speciesStatement.bindString(speciesColumns.get(SPECIES_DEPTH), "");
          speciesStatement.bindString(speciesColumns.get(SPECIES_LOCATION), "");
          speciesStatement.bindLong(
              speciesColumns.get(SPECIES_IS_COMMERCIAL), s.getDetails().isCommercial() ? 1 : 0);
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_TAXA_PHYLUM), s.getDetails().getTaxaPhylum());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_TAXA_CLASS), s.getDetails().getTaxaClass());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_TAXA_ORDER), s.getDetails().getTaxaOrder());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_TAXA_FAMILY), s.getDetails().getTaxaFamily());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_TAXA_GENUS), s.getDetails().getTaxaGenus());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_TAXA_SPECIES), s.getDetails().getTaxaSpecies());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_TAXA_SUBSPECIES), s.getDetails().getTaxaSubSpecies());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_COMMON_NAMES), s.getDetails().getCommonNames());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_OTHER_NAMES), s.getDetails().getOtherNames());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_SEARCHICON),
              "content://au.com.museumvictoria.fieldguide.vic.fork.FieldGuideAssestsProvider/"
                  + s.getSquareThumbnail());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_BUTTERFLY_START), s.getDetails().getButterflyStart());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_BUTTERFLY_END), s.getDetails().getButterflyEnd());
          speciesStatement.bindString(
              speciesColumns.get(SPECIES_DISTRIBUTION_MAP), s.getDetails().getDistributionMap());

          speciesStatement.executeInsert();
          speciesStatement.clearBindings();

          Iterator<Images> imgs = s.getImages().iterator();
          while (imgs.hasNext()) {
            Images img = imgs.next();

            imageStatement.bindString(imagesColumns.get(MEDIA_FILENAME), img.getFilename());
            imageStatement.bindString(imagesColumns.get(MEDIA_CAPTION), img.getImageDescription());
            imageStatement.bindString(imagesColumns.get(MEDIA_CREDIT), img.getCredit());
            imageStatement.bindString(imagesColumns.get(MEDIA_IDENTIFIER), s.getIdentifier());

            imageStatement.executeInsert();
            imageStatement.clearBindings();
          }

          ++currCount;

        }

        db.setTransactionSuccessful();

      } finally {
        imageStatement.close();
        speciesStatement.close();
        db.endTransaction();
      }

      Log.i(TAG, "Done populating database");
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
