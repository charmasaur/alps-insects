package au.com.museumvictoria.fieldguide.vic.fork.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;
import au.com.museumvictoria.fieldguide.vic.fork.provider.DataProvider;
import au.com.museumvictoria.fieldguide.vic.fork.util.ImageResizer;

public class SpeciesListCursorAdapter extends CursorAdapter implements SectionIndexer {
  private final DataProvider dataProvider;
  private final AlphabetIndexer indexer;
  private final Resources resources;

  public SpeciesListCursorAdapter(Context context, Cursor c, int flags,
      DataProvider dataProvider) {
    super(context, c, flags);

    this.dataProvider = dataProvider;
    resources = context.getResources();

    indexer = new AlphabetIndexer(c, c.getColumnIndex(FieldGuideDatabase.SPECIES_LABEL),
        " ABCDEFGHIJKLMNOPQRTSUVWXYZ");
    indexer.setCursor(c);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    LayoutInflater inflater = LayoutInflater.from(context);
    return inflater.inflate(R.layout.species_list, parent, false);
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    TextView labelView = (TextView) view.findViewById(R.id.speciesLabel);
    TextView sublabelView = (TextView) view.findViewById(R.id.speciesSublabel);
    ImageView iconView = (ImageView) view.findViewById(R.id.speciesIcon);

    labelView.setText(Html.fromHtml(getLabel(cursor)));
    String sublabel = getSublabel(cursor);
    if (sublabel == null) {
      sublabelView.setVisibility(View.GONE);
    } else {
      sublabelView.setVisibility(View.VISIBLE);
      sublabelView.setText(Html.fromHtml(sublabel));
    }

    int iconSize = resources.getDimensionPixelSize(R.dimen.species_list_thumbnail_size);
    iconView.setImageBitmap(ImageResizer.decodeSampledBitmapFromStream(
        dataProvider.getSpeciesThumbnail(
            cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_THUMBNAIL))),
        iconSize, iconSize));
  }

  @Override
  public int getPositionForSection(int section) {
    return indexer.getPositionForSection(section);
  }

  @Override
  public int getSectionForPosition(int position) {
    return indexer.getSectionForPosition(position);
  }

  @Override
  public Object[] getSections() {
    return indexer.getSections();
  }

  public String getLabelAtPosition(int position) {
    return getLabel((Cursor) getItem(position));
  }

  @Nullable
  public String getSublabelAtPosition(int position) {
    return getSublabel((Cursor) getItem(position));
  }

  public static String[] getRequiredColumns() {
    return new String[] { FieldGuideDatabase.SPECIES_ID + " AS _id",
        FieldGuideDatabase.SPECIES_LABEL, FieldGuideDatabase.SPECIES_SUBLABEL,
        FieldGuideDatabase.SPECIES_THUMBNAIL };
  }

  private static String getLabel(Cursor cursor) {
    return cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_LABEL));
  }

  @Nullable
  private static String getSublabel(Cursor cursor) {
    return cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_SUBLABEL));
  }
}
