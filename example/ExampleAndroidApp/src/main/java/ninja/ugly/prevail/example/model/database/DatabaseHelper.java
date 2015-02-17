package ninja.ugly.prevail.example.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ninja.ugly.prevail.example.model.domain.TodoItem;

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;

class DatabaseHelper extends SQLiteOpenHelper {

  public static final String DATABASE_NAME = "database.sqlite";
  private static final int DATABASE_VERSION = 1;

  private static DatabaseHelper sSingleton;

  static {
    cupboard().register(TodoItem.class);
  }

  private DatabaseHelper(final Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  /**
   * Returns singleton instance of database connection.
   *
   * @param context Application context.
   * @return Singleton instance of database connection.
   */
  public static synchronized DatabaseHelper get(final Context context) {
    checkNotNull(context);

    if (sSingleton == null) {
      sSingleton = new DatabaseHelper(context);
    }

    return sSingleton;
  }

  @Override
  public final void onCreate(final SQLiteDatabase db) {
    cupboard().withDatabase(db).createTables();
  }

  @Override
  public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    cupboard().withDatabase(db).upgradeTables();
  }

}
