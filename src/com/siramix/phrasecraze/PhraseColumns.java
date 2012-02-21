package com.siramix.phrasecraze;

import android.provider.BaseColumns;

public class PhraseColumns implements BaseColumns {

  public static final String TABLE_NAME = "phrases";

  public static final String PHRASE = "phrase";

  public static final String DIFFICULTY = "difficulty";

  public static final String PLAY_DATE = "play_date";

  public static final String PACK_ID = "pack_id";

  public static final String[] COLUMNS = {_ID, PHRASE, DIFFICULTY, PLAY_DATE, PACK_ID};

  public static final String TABLE_CREATE = "CREATE TABLE "
      + TABLE_NAME + "( " + 
      _ID + " INTEGER PRIMARY KEY, " +
      PHRASE + " TEXT, " + 
      DIFFICULTY + " INTEGER, " +
      PLAY_DATE + " INTEGER, " +
      PACK_ID + " INTEGER );";
}
