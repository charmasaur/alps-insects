package au.com.museumvictoria.fieldguide.vic.fork.provider;

import android.content.Context;

public final class DataProviderFactory {
  private static DataProvider dataProvider;

  public synchronized static DataProvider getDataProvider(Context context) {
    if (dataProvider == null) {
      dataProvider = new AssetsDataProvider(context.getResources().getAssets());
    }
    return dataProvider;
  }
}
