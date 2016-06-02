package au.com.museumvictoria.fieldguide.vic.fork.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;
import au.com.museumvictoria.fieldguide.vic.fork.util.ImageResizer;
import au.com.museumvictoria.fieldguide.vic.fork.util.Utilities;

public class SpeciesListCursorAdapter extends CursorAdapter implements SectionIndexer {
  private final AlphabetIndexer indexer;

  public SpeciesListCursorAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);

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

    labelView.setText(getLabel(cursor));
    String sublabel = getSublabel(cursor);
    if (sublabel == null) {
      sublabelView.setVisibility(View.GONE);
    } else {
      sublabelView.setVisibility(View.VISIBLE);
      sublabelView.setText(sublabel);
    }

    String iconPath = Utilities.SPECIES_IMAGES_THUMBNAILS_PATH
        + cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_THUMBNAIL));

    // TODO: Fix?
    iconView.setImageBitmap(ImageResizer.decodeSampledBitmapFromFile(
        Utilities.getFullExternalDataPath(context, iconPath), 150, 150));
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

  private static String getLabel(Cursor cursor) {
    return cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_LABEL));
  }

  @Nullable
  private static String getSublabel(Cursor cursor) {
    return cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_SUBLABEL));
  }
}
