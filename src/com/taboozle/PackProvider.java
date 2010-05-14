package com.taboozle;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.taboozle.R;

/**
 * Provides access to a database of cards. Each card has a title and the words
 * the user cannot say when trying to describe the word.
 */
public class PackProvider extends ContentProvider
{

  /**
   * Tag for logging output
   */
  private static final String TAG = "PackProvider";


  /**
   * Query and URI matching constants
   */
  private static final int CARDS = 1;
  private static final int CARD_ID = 2;
  private static HashMap<String, String> sCardsProjectionMap;
  private static final UriMatcher sUriMatcher;

  /**
   * This class helps open, create, and upgrade the database file.
   */
  private static class DatabaseHelper extends SQLiteOpenHelper
  {
    
    private static Context curContext;
    /**
     * Default constructor for the database helper
     * 
     * @param context
     *          - database context object
     */
    DatabaseHelper( Context context )
    {
      super( context, GameData.DATABASE_NAME, null, 
             GameData.DATABASE_VERSION );
      curContext = context;
    }

    @Override
    public void onCreate( SQLiteDatabase db )
    {
      db.execSQL( "CREATE TABLE " + GameData.CARD_TABLE_NAME + " (" + 
                  GameData.Cards._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                  GameData.Cards.PACK_NAME + " TEXT," +
                  GameData.Cards.TITLE + " TEXT," + 
                  GameData.Cards.BAD_WORDS + " TEXT," + 
                  GameData.Cards.CATEGORIES + " TEXT);" );
      
      InputStream starterXML =
        curContext.getResources().openRawResource(R.raw.starter);
      DocumentBuilderFactory docBuilderFactory = 
        DocumentBuilderFactory.newInstance();
      try
      {
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(starterXML);
        NodeList cardNodes = doc.getElementsByTagName( "card" );
        for(int i = 0; i < cardNodes.getLength(); i++)
        {        
          NodeList titleWhiteAndBads = cardNodes.item( i ).getChildNodes();
          Node titleNode = null;
          Node badsNode = null;
          Node categoriesNode = null;
          Node packNameNode = null;
          for( int j = 0; j < titleWhiteAndBads.getLength(); j++ )
          {
            String candidateName = titleWhiteAndBads.item( j ).getNodeName(); 
            if( candidateName.equals( "title" ) )
            {
              titleNode = titleWhiteAndBads.item( j );
            }
            else if( candidateName.equals( "pack-name" ) )
            {
              packNameNode = titleWhiteAndBads.item( j );
            }
            else if( candidateName.equals( "bad-words" ) )
            {
              badsNode = titleWhiteAndBads.item( j );
            }
            else if( candidateName.equals( "categories" ) )
            {
              categoriesNode = titleWhiteAndBads.item( j );
            }
            else
            {
              continue; // We found some #text
            }
          }
          String title = titleNode.getFirstChild().getNodeValue();
          String packName = packNameNode.getFirstChild().getNodeValue();
          String categories = categoriesNode.getFirstChild().getNodeValue();
          String badWords = "";
          NodeList bads = badsNode.getChildNodes();
          for( int j = 0; j < bads.getLength(); j++ )
          {
            String candidateName = bads.item( j ).getNodeName();
            if( candidateName.equals( "word" ) )
            {
              badWords += bads.item( j ).getFirstChild().getNodeValue() + ",";
            }
          }
          // hack because I have a comma at the end
          badWords = badWords.substring( 0, badWords.length() - 1 );
          db.execSQL( "INSERT INTO " + GameData.CARD_TABLE_NAME + " (" + 
                      GameData.Cards.PACK_NAME + "," + GameData.Cards.TITLE  + ", " + 
                      GameData.Cards.BAD_WORDS + ", " + GameData.Cards.CATEGORIES + 
                      ") VALUES (\"" + 
                      packName + "\",\"" +
                      title + "\",\"" +
                      badWords + "\",\"" + 
                      categories + "\");" );
        }
      }
      catch( ParserConfigurationException e )
      {
        e.printStackTrace();
      }
      catch( SAXException e )
      {
        e.printStackTrace();
      }
      catch( IOException e )
      {
        e.printStackTrace();
      }                     
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
    {
      Log.w( TAG, "Upgrading database from version " + oldVersion + " to "
                  + newVersion + ", which will destroy all old data" );
      db.execSQL( "DROP TABLE IF EXISTS cards;" );
      onCreate( db );
    }

  } // End of DatabaseHelper

  private DatabaseHelper mOpenHelper;

  @Override
  public boolean onCreate()
  {
    mOpenHelper = new DatabaseHelper( getContext() );
    return true;
  }

  @Override
  public Cursor query( Uri uri, String[] projection, String selection,
                       String[] selectionArgs, String sortOrder )
  {
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    qb.setTables( GameData.CARD_TABLE_NAME );

    switch ( sUriMatcher.match( uri ) )
    {
      case CARDS:
        qb.setProjectionMap( sCardsProjectionMap );
        break;

      case CARD_ID:
        qb.setProjectionMap( sCardsProjectionMap );
        qb.appendWhere( GameData.Cards._ID + "=" + uri.getPathSegments().get( 1 ) );
        break;

      default:
        throw new IllegalArgumentException( "Unknown URI: " + uri );
    }

    // Get the database and run the query
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    Cursor c = qb.query( db, projection, selection, selectionArgs, null, null,
                         null );

    // Tell the cursor what uri to watch, so it knows when its source data
    // changes
    c.setNotificationUri( getContext().getContentResolver(), uri );
    return c;
  }

  @Override
  public String getType( Uri uri )
  {
    switch ( sUriMatcher.match( uri ) )
    {
      case CARDS:
        return GameData.Cards.CONTENT_TYPE;
      case CARD_ID:
        return GameData.Cards.CONTENT_ITEM_TYPE;
      default:
        throw new IllegalArgumentException( "Unknown URI: " + uri );
    }
  }

  @Override
  public Uri insert( Uri uri, ContentValues initialValues )
  {
    // Validate the requested uri
    if( sUriMatcher.match( uri ) != CARDS )
    {
      throw new IllegalArgumentException( "Unknown URI " + uri );
    }

    ContentValues values;
    if( initialValues != null )
    {
      values = new ContentValues( initialValues );
    }
    else
    {
      values = new ContentValues();
    }

    if( values.containsKey( GameData.Cards.TITLE ) == false )
    {
      values.put( GameData.Cards.TITLE, "No Title Given" );
    }

    if( values.containsKey( GameData.Cards.BAD_WORDS ) == false )
    {
      values.put( GameData.Cards.BAD_WORDS, "No Bad Words Given" );
    }

    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    long rowId = db.insert( GameData.CARD_TABLE_NAME, "", values );
    if( rowId > 0 )
    {
      Uri cardUri = ContentUris.withAppendedId( GameData.Cards.CONTENT_URI, rowId );
      this.getContext().getContentResolver().notifyChange( cardUri, null );
      return cardUri;
    }

    throw new SQLException( "Failed to insert row into " + uri );
  }

  @Override
  public int delete( Uri uri, String where, String[] whereArgs )
  {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int count;
    switch ( sUriMatcher.match( uri ) )
    {
      case CARDS:
        count = db.delete( GameData.CARD_TABLE_NAME, where, whereArgs );
        break;

      case CARD_ID:
        String noteId = uri.getPathSegments().get( 1 );
        count = db
                  .delete( GameData.CARD_TABLE_NAME,
                           GameData.Cards._ID
                               + "="
                               + noteId
                               + ( !TextUtils.isEmpty( where ) ? " AND ("
                                                                 + where + ')'
                                                              : "" ), whereArgs );
        break;

      default:
        throw new IllegalArgumentException( "Unknown URI " + uri );
    }

    getContext().getContentResolver().notifyChange( uri, null );
    return count;
  }

  @Override
  public int update( Uri uri, ContentValues values, String where,
                     String[] whereArgs )
  {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int count;
    switch ( sUriMatcher.match( uri ) )
    {
      case CARDS:
        count = db.update( GameData.CARD_TABLE_NAME, values, where, whereArgs );
        break;

      case CARD_ID:
        String noteId = uri.getPathSegments().get( 1 );
        count = db
                  .update( GameData.CARD_TABLE_NAME, values,
                           GameData.Cards._ID
                               + "="
                               + noteId
                               + ( !TextUtils.isEmpty( where ) ? " AND ("
                                                                 + where + ')'
                                                              : "" ), whereArgs );
        break;

      default:
        throw new IllegalArgumentException( "Unknown URI " + uri );
    }

    getContext().getContentResolver().notifyChange( uri, null );
    return count;
  }

  static
  {
    sUriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
    sUriMatcher.addURI( GameData.AUTHORITY, "cards", CARDS );
    sUriMatcher.addURI( GameData.AUTHORITY, "cards/#", CARD_ID );

    sCardsProjectionMap = new HashMap<String, String>();
    sCardsProjectionMap.put( GameData.Cards._ID, GameData.Cards._ID );
    sCardsProjectionMap.put( GameData.Cards.TITLE, GameData.Cards.TITLE );
    sCardsProjectionMap.put( GameData.Cards.BAD_WORDS, 
                             GameData.Cards.BAD_WORDS );
  }
}