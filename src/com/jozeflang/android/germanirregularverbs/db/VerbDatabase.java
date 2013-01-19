package com.jozeflang.android.germanirregularverbs.db;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** 
 * A singleton helper for working with word database <br />
 *  <br />
 * Database Schema:
 * <pre>
 * create table german (
 * 	id int primary key autoincrement,
 *  present varchar(100) not null,
 *  perfect varchar(100) not null,
 *  preterite varchar(100) not null
 * );
 * <pre>
 * 
 * @author Jozef Lang (developer@jozeflang.com) 
 */
enum VerbDatabase {
    INSTANCE;
 
    public final String COLUMN_ID = "id";
    public final String COLUMN_PRESENT = "present";
    public final String COLUMN_PERFECT= "perfect";
    public final String COLUMN_PRETERITE = "preterite";
    
    private final String DATABASE_NAME = "words";
    private final int DATABASE_VERSION = 2;
	private final String TABLE_NAME = "german";
	private final String TABLE_CREATE_SCRIPT =
			"create table " + TABLE_NAME + " ( "
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_PRESENT + " varchar(100) not null,"
			+ COLUMN_PERFECT + " varchar(100) not null,"
			+ COLUMN_PRETERITE + " varchar(100) not null"
			+");";
	
	/**
	 * A helper for word database
	 */
	private VerbDatabaseHelper dbHelper = null;
	
	/**
	 * Sets a context to be used with database.<br />
	 * An exception {@link IllegalStateException} is thrown when invoked and context has already been set.<br />
	 * Use {@link VerbDatabase#close()} to close context
	 * @param context
	 * @throws IllegalStateException 
	 */
	public void open(Context context) {
		if (this.dbHelper != null)
			throw new IllegalArgumentException("Helper has already been set");
		dbHelper = new VerbDatabaseHelper(context);
	}
	
	/**
	 * Closes context 
	 */
	public void close() {
		if (dbHelper != null)
			dbHelper.close();
	}
	
	/**
	 * Returns the number of verbs in database 
	 * @return
	 */
	public long getVerbCount() {
		 return DatabaseUtils.queryNumEntries(getHandler(), TABLE_NAME);
	}
	
	/**
	 * Returns verb from database by id.
	 * @param id
	 * @return An array of strings in following pattern:
	 * 	<li>
	 * 		<ol>Present</ol>
	 * 		<ol>Perfect</ol>
	 * 		<ol>Preterite</ol>
	 *  </li>
	 */
	public String[] getVerb(final int id) {
		String[] verb = new String[3];
		
		Cursor c = getHandler().query(TABLE_NAME, new String[] {COLUMN_PRESENT, COLUMN_PERFECT, COLUMN_PRETERITE}, COLUMN_ID + " = ?", new String[] {String.valueOf(id)}, null, null, null);
		c.moveToFirst();
		verb[0] = c.getString(0);
		verb[1] = c.getString(1);
		verb[2] = c.getString(2);
				
		return verb;
	}
	
	/**
	 * Returns handler to database. 
	 * @return
	 * @throws SQLException
	 */
	private SQLiteDatabase getHandler() throws SQLException {
		if (dbHelper == null)
			throw new IllegalStateException("A context has not been set");
		return dbHelper.getReadableDatabase();
	}
	
	/**
	 * Helper class for managing creation and upgrade of word database
	 * @author Jozef Lang (developer@jozeflang.com)
	 *
	 */
	private class VerbDatabaseHelper extends SQLiteOpenHelper {

		public VerbDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_CREATE_SCRIPT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP IF TABLE EXISTS "+ TABLE_NAME);
			onCreate(db);
		}
		
	}
}
