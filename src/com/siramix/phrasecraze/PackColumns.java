package com.siramix.phrasecraze;

import android.provider.BaseColumns;

public class PackColumns implements BaseColumns {

  public static final String TABLE_NAME = "packs";

  public static final String NAME = "name";

  public static final String FILE_NAME = "file_name";

  public static final String VERSION = "version";

  public static final String[] COLUMNS = {_ID, NAME, FILE_NAME, VERSION};

  public static final String TABLE_CREATE = "CREATE TABLE " 
      + TABLE_NAME + "( " + 
          _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
          NAME + " TEXT, " + 
          FILE_NAME + " TEXT, " + 
          VERSION + " INTEGER );";
}
