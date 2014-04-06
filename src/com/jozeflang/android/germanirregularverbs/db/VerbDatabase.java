package com.jozeflang.android.germanirregularverbs.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.jozeflang.android.germanirregularverbs.db.table.PerfectTable;
import com.jozeflang.android.germanirregularverbs.db.table.PreteriteTable;
import com.jozeflang.android.germanirregularverbs.db.table.TranslationTable;
import com.jozeflang.android.germanirregularverbs.db.table.VerbTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 
 * A singleton helper for working with word database
 * @author Jozef Lang (developer@jozeflang.com) 
 */
enum VerbDatabase {
    INSTANCE;

    private final String DATABASE_NAME = "verbs";
    private final int DATABASE_VERSION = 2;
	
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
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper = null;
		}
	}
	
	/**
	 * Returns the number of verbs in database
     * @param onlyActive
	 * @return
	 */
	public long getVerbCount(boolean onlyActive) {
        String onlyActiveCondition = null;
        if (onlyActive) {
            onlyActiveCondition = String.format("%s = 1", VerbTable.COLUMN_ACTIVE);
        }
        Cursor c = getHandler().query(VerbTable.TABLE_NAME, new String[] {"count(1)"}, onlyActiveCondition, null, null, null, null);
        long verbCount = -1;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            verbCount = c.getLong(0);
            c.moveToNext();
        }
        if (verbCount == -1) {
            // Something went wrong, -1 is not a valid verb count
            throw new IllegalStateException("Illegal verb count: " + verbCount);
        }
        return verbCount;
	}
	
	/**
	 * Returns verb from database by id.
	 * @param id
	 * @return 
	 */
	public VerbDTO getVerb(final int id) {
		VerbDTO verb = getVerbFromDb(id);
		getTranslations(verb);
		getPerfects(verb);
		getPreterites(verb);
		return verb;
	}

    public VerbDTO getNthVerb(int n, boolean activeOnly) {
        return getVerb(getNthVerbIdFromDb(n, activeOnly));
    }

    /**
     * Returns a list of all verbs stored in database
     * @param onlyActive
     * @param filter
     * @return
     */
    public List<VerbDTO> getAllVerbs(boolean onlyActive, String filter) {
        List<VerbDTO> allVerbs = getAllVerbsFromDb(onlyActive, filter);
        for (VerbDTO verb : allVerbs) {
            getTranslations(verb);
            getPerfects(verb);
            getPreterites(verb);
        }
        return allVerbs;
    }

    public int updateVerb(VerbDTO verb) {
        String whereCondition = String.format("%s = %d", VerbTable.COLUMN_ID, verb.getId());
        return getHandler(false).update(VerbTable.TABLE_NAME, VerbTable.createContentValues(verb), whereCondition, null);
    }

    public void setVerbsActivness(boolean isActive) {
        getHandler(false).execSQL(String.format("UPDATE %s SET %s=%d", VerbTable.TABLE_NAME, VerbTable.COLUMN_ACTIVE, isActive ? 1 : 0));
    }

    public void invertVerbActivness() {
        getHandler(false).execSQL(String.format("UPDATE %s SET %s=CASE WHEN %s = 0 THEN 1 ELSE 0 END", VerbTable.TABLE_NAME, VerbTable.COLUMN_ACTIVE, VerbTable.COLUMN_ACTIVE));
    }


    private List<VerbDTO> getAllVerbsFromDb(boolean onlyActive, String filter) {
        List<VerbDTO> verbList = new ArrayList<VerbDTO>();
        String onlyActiveCondition = null;
        if (onlyActive) {
            onlyActiveCondition = String.format("%s = %d", VerbTable.COLUMN_ACTIVE, onlyActive ? 1 : 0);
        }
        String filterCondition = null;
        if (filter != null && !TextUtils.isEmpty(filter)) {
            filterCondition = String.format("%s LIKE '%s%%'", VerbTable.COLUMN_PRESENT, filter);
        }

        // Construct condition
        String condition = "";
        if (onlyActiveCondition != null) {
            condition = onlyActiveCondition;
        }
        if (!TextUtils.isEmpty(condition)) {
            condition += " AND ";
        }
        if (filterCondition != null) {
            condition += filterCondition;
        }

        Cursor c = getHandler().query(VerbTable.TABLE_NAME, new String[] {VerbTable.COLUMN_ID, VerbTable.COLUMN_PRESENT, VerbTable.COLUMN_ACTIVE}, condition, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            verbList.add(VerbDTO.of(c.getInt(0), c.getString(1), c.getInt(2)));
            c.moveToNext();
        }
        c.close();
        return verbList;
    }
	
	/**
	 * Returns basic VerbDTO object from data stored in database
	 * @param id
	 * @return
	 */
	private VerbDTO getVerbFromDb(final int id) {
		Cursor c = getHandler().query(VerbTable.TABLE_NAME, new String[] {VerbTable.COLUMN_ID, VerbTable.COLUMN_PRESENT, VerbTable.COLUMN_ACTIVE}, VerbTable.COLUMN_ID + " = ?", new String[] {String.valueOf(id)}, null, null, null);
		c.moveToFirst();
		int verbId = c.getInt(0);
		String verbPresent = c.getString(1);
        int verbActive = c.getInt(2);
		c.close();
		return VerbDTO.of(verbId, verbPresent, verbActive);
	}

    /**
     * Returns n-th verb from the database.
     * @param n
     * @param activeOnly
     * @return
     */
    private int getNthVerbIdFromDb(final int n, boolean activeOnly) {
        String limitStr = String.format("%d,1", n);
        Cursor c = null;
        if (activeOnly) {
            c = getHandler().query(VerbTable.TABLE_NAME, new String[] {VerbTable.COLUMN_ID}, VerbTable.COLUMN_ACTIVE + " = 1", null, null, null, null, limitStr);
        } else {
            c = getHandler().query(VerbTable.TABLE_NAME, new String[] {VerbTable.COLUMN_ID}, null, null, null, null, null, limitStr);
        }
        int verbId = -1;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            verbId = c.getInt(0);
            c.moveToNext();
        }
        c.close();
        return verbId;
    }
	
	/**
	 * Returns translations from data stored in database
	 * @param verb
	 * @return
	 */
	private VerbDTO getTranslations(VerbDTO verb) {
		Cursor c = getHandler().query(TranslationTable.TABLE_NAME, new String[] {TranslationTable.COLUMN_TRANSLATION}, TranslationTable.COLUMN_VERB_ID + " = ?", new String[] {String.valueOf(verb.getId())}, null, null, null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			verb.addTranslation(c.getString(0));
			c.moveToNext();
		}
		c.close();
		return verb;
	}
	
	/**
	 * Returns perfects from data stored in database
	 * @param verb
	 * @return
	 */
	private VerbDTO getPerfects(VerbDTO verb) {
		Cursor c = getHandler().query(PerfectTable.TABLE_NAME, new String[] {PerfectTable.COLUMN_AUX_VERB, PerfectTable.COLUMN_PERFECT}, PerfectTable.COLUMN_VERB_ID + " = ?", new String[] {String.valueOf(verb.getId())}, null, null, null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			verb.addPerfect(c.getString(0), c.getString(1));
			c.moveToNext();
		}
		c.close();
		return verb;
	}
	
	/**
	 * Returns preterites from data stored in database
	 * @param verb
	 * @return
	 */
	private VerbDTO getPreterites(VerbDTO verb) {
		Cursor c = getHandler().query(PreteriteTable.TABLE_NAME, new String[] {PreteriteTable.COLUMN_PRETERITE}, PreteriteTable.COLUMN_VERB_ID + " = ?", new String[] {String.valueOf(verb.getId())}, null, null, null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			verb.addPreterite(c.getString(0));
			c.moveToNext();
		}
		c.close();
		return verb;
	}
	
	/**
	 * Returns readonly handler to database 
	 * @return
	 * @throws
	 */
	private SQLiteDatabase getHandler() throws SQLException {
		return getHandler(true);
	}
	
	/**
	 * Returns handler to database. 
	 * @param readonly
	 * @return
	 * @throws SQLException
	 */
	private SQLiteDatabase getHandler(final boolean readonly) throws SQLException {
		if (dbHelper == null)
			throw new IllegalStateException("A context has not been set");
		return readonly ? dbHelper.getReadableDatabase() : dbHelper.getWritableDatabase();
	}
	
	/**
	 * Helper class for managing creation and upgrade of word database
	 * @author Jozef Lang (developer@jozeflang.com)
	 *
	 */
	private class VerbDatabaseHelper extends SQLiteOpenHelper {

		private VerbDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			generateTables(db);
			importScript(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			dropTables(db);
			onCreate(db);
		}
		
		private void generateTables(SQLiteDatabase db) {
			for (String tableCreateQuery : Arrays.asList(VerbTable.TABLE_CREATE_SCRIPT, PreteriteTable.TABLE_CREATE_SCRIPT, PerfectTable.TABLE_CREATE_SCRIPT, TranslationTable.TABLE_CREATE_SCRIPT))
				db.execSQL(tableCreateQuery);
		}
		
		private void dropTables(SQLiteDatabase db) {
			for (String tableName : Arrays.asList(VerbTable.TABLE_NAME, PreteriteTable.TABLE_NAME, PerfectTable.TABLE_NAME, TranslationTable.TABLE_NAME))
				db.execSQL(String.format("DROP TABLE IF EXISTS " + tableName));
		}
		
		private void importScript(SQLiteDatabase db) {
            db.execSQL("INSERT INTO verb (id, present, active) VALUES(1, 'backen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(1, 'bake');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(1, 'backte');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(1, 'buk');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(1, 'hat', 'gebacken');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(2, 'befehlen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(2, 'command');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(2, 'befahl');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(2, 'hat', 'befohlen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(3, 'befleißen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(3, 'take');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(3, 'befliß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(3, 'hat', 'beflissen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(4, 'beginnen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(4, 'begin');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(4, 'begann');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(4, 'hat', 'begonnen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(5, 'beißen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(5, 'bite');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(5, 'biß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(5, 'hat', 'gebissen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(6, 'bergen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(6, 'rescue');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(6, 'barg');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(6, 'hat', 'geborgen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(7, 'bersten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(7, 'burst');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(7, 'barst');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(7, 'hat', 'geborsten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(8, 'bescheißen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(8, 'cheat');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(8, 'beschiß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(8, 'hat', 'beschissen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(9, 'bewegen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(9, 'induce');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(9, 'bewog');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(9, 'hat', 'bewogen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(10, 'biegen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(10, 'bend');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(10, 'bog');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(10, 'hat', 'gebogen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(11, 'bieten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(11, 'offer');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(11, 'bot');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(11, 'hat', 'geboten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(12, 'binden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(12, 'bind');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(12, 'band');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(12, 'hat', 'gebunden');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(13, 'bitten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(13, 'request');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(13, 'bat');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(13, 'hat', 'gebeten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(14, 'blasen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(14, 'blow');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(14, 'blies');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(14, 'hat', 'geblasen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(15, 'bleiben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(15, 'remain');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(15, 'blieb');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(15, 'ist', 'geblieben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(16, 'bleichen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(16, 'fade');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(16, 'bleichte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(16, 'hat', 'geblichen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(17, 'braten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(17, 'roast');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(17, 'briet');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(17, 'hat', 'gebraten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(18, 'brechen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(18, 'break');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(18, 'brach');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(18, 'hat', 'gebrochen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(19, 'brennen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(19, 'burn');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(19, 'brannte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(19, 'hat', 'gebrannt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(20, 'bringen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(20, 'bring');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(20, 'brachte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(20, 'hat', 'gebracht');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(21, 'denken', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(21, 'think');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(21, 'dachte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(21, 'hat', 'gedacht');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(22, 'dingen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(22, 'hire');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(22, 'dingte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(22, 'hat', 'gedungen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(23, 'dreschen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(23, 'thresh');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(23, 'drasch');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(23, 'hat', 'gedroschen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(24, 'dringen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(24, 'penetrate');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(24, 'drang');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(24, 'hat', 'gedrungen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(25, 'dünken', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(25, 'seem');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(25, 'dünkte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(25, 'hat', 'gedünkt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(26, 'dürfen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(26, 'be');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(26, 'durfte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(26, 'hat', 'gedurft');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(27, 'empfehlen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(27, 'recommend');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(27, 'empfahl');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(27, 'hat', 'empfohlen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(28, 'erbleichen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(28, 'pale');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(28, 'erbleichte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(28, 'hat', 'erblichen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(29, 'erkiesen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(29, 'choose');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(29, 'erkor');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(29, 'hat', 'erkoren');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(30, 'erlöschen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(30, 'die');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(30, 'erlosch');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(30, 'hat', 'erloschen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(31, 'erschrecken', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(31, 'frighten');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(31, 'erschrak');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(31, 'hat', 'erschrocken');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(32, 'essen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(32, 'eat');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(32, 'aß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(32, 'hat', 'gegessen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(33, 'fahren', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(33, 'drive');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(33, 'fuhr');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(33, 'ist', 'gefahren');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(34, 'fallen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(34, 'fall');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(34, 'fiel');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(34, 'ist', 'gefallen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(35, 'fangen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(35, 'catch');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(35, 'fing');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(35, 'hat', 'gefangen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(36, 'fechten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(36, 'fence');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(36, 'focht');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(36, 'hat', 'gefochten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(37, 'finden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(37, 'find');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(37, 'fand');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(37, 'hat', 'gefunden');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(38, 'flechten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(38, 'plait');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(38, 'flocht');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(38, 'hat', 'geflochten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(39, 'fliegen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(39, 'fly');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(39, 'flog');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(39, 'ist', 'geflogen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(40, 'fliehen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(40, 'flee');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(40, 'floh');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(40, 'hat', 'geflohen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(41, 'fließen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(41, 'flow');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(41, 'floß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(41, 'hat', 'geflossen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(42, 'fragen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(42, 'ask');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(42, 'fragte');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(42, 'frug');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(42, 'hat', 'gefragt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(43, 'fressen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(43, 'snarf');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(43, 'fraß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(43, 'hat', 'gefressen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(44, 'frieren', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(44, 'freeze');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(44, 'fror');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(44, 'hat', 'gefroren');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(45, 'gären', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(45, 'ferment');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(45, 'gor');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(45, 'hat', 'gegoren');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(46, 'gebären', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(46, 'give');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(46, 'gebar');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(46, 'hat', 'geboren');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(47, 'geben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(47, 'give');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(47, 'gab');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(47, 'hat', 'gegeben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(48, 'gedeihen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(48, 'thrive');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(48, 'gedieh');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(48, 'hat', 'gediehen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(49, 'gehen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(49, 'go');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(49, 'ging');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(49, 'ist', 'gegangen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(50, 'gelingen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(50, 'succeed');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(50, 'gelang');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(50, 'hat', 'gelungen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(51, 'gelten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(51, 'be');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(51, 'galt');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(51, 'hat', 'gegolten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(52, 'genesen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(52, 'convalesce');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(52, 'genas');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(52, 'hat', 'genesen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(53, 'genießen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(53, 'enjoy');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(53, 'genoß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(53, 'hat', 'genossen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(54, 'geraten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(54, 'turn');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(54, 'geriet');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(54, 'hat', 'geraten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(55, 'geschehen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(55, 'happen');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(55, 'geschah');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(55, 'ist', 'geschehen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(56, 'gewinnen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(56, 'win');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(56, 'gewann');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(56, 'hat', 'gewonnen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(57, 'gießen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(57, 'pour');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(57, 'goß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(57, 'hat', 'gegossen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(58, 'gleichen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(58, 'compare');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(58, 'glich');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(58, 'hat', 'geglichen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(59, 'gleißen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(59, 'gleam');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(59, 'gleißte');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(59, 'gliß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(59, 'hat', 'geglissen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(60, 'gleiten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(60, 'glide');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(60, 'glitt');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(60, 'hat', 'geglitten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(61, 'glimmen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(61, 'glow');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(61, 'glomm');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(61, 'hat', 'geglommen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(62, 'graben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(62, 'dig');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(62, 'grub');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(62, 'hat', 'gegraben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(63, 'greifen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(63, 'seize');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(63, 'griff');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(63, 'hat', 'gegriffen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(64, 'haben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(64, 'have');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(64, 'hatte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(64, 'hat', 'gehabt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(65, 'halten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(65, 'hold');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(65, 'hielt');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(65, 'hat', 'gehalten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(66, 'hängen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(66, 'hang');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(66, 'hing');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(66, 'hat', 'gehangen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(67, 'hauen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(67, 'hew');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(67, 'haute');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(67, 'hat', 'gehauen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(68, 'heben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(68, 'lift');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(68, 'hob');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(68, 'hat', 'gehoben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(69, 'heißen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(69, 'be');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(69, 'hieß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(69, 'hat', 'geheißen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(70, 'helfen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(70, 'help');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(70, 'half');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(70, 'hat', 'geholfen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(71, 'kennen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(71, 'know');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(71, 'kannte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(71, 'hat', 'gekannt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(72, 'klimmen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(72, 'climb');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(72, 'klomm');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(72, 'hat', 'geklommen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(73, 'klingen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(73, 'sound');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(73, 'klang');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(73, 'hat', 'geklungen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(74, 'kneifen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(74, 'pinch');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(74, 'kniff');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(74, 'hat', 'gekniffen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(75, 'kommen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(75, 'come');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(75, 'kam');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(75, 'ist', 'gekommen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(76, 'können', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(76, 'be');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(76, 'konnte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(76, 'hat', 'gekonnt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(77, 'kreischen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(77, 'shriek');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(77, 'kreischte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(77, 'hat', 'gekreischt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(78, 'kriechen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(78, 'crawl');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(78, 'kroch');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(78, 'hat', 'gekrochen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(79, 'küren', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(79, 'choose');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(79, 'kürte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(79, 'hat', 'gekürt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(80, 'laden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(80, 'load');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(80, 'lud');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(80, 'hat', 'geladen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(81, 'lassen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(81, 'let');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(81, 'ließ');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(81, 'hat', 'gelassen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(82, 'laufen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(82, 'run');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(82, 'lief');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(82, 'ist', 'gelaufen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(83, 'leiden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(83, 'suffer');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(83, 'litt');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(83, 'hat', 'gelitten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(84, 'leihen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(84, 'lend');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(84, 'lieh');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(84, 'hat', 'geliehen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(85, 'lesen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(85, 'read');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(85, 'las');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(85, 'hat', 'gelesen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(86, 'liegen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(86, 'lie');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(86, 'lag');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(86, 'ist', 'gelegen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(87, 'löschen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(87, 'go');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(87, 'losch');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(87, 'hat', 'geloschen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(88, 'lügen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(88, 'tell');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(88, 'log');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(88, 'hat', 'gelogen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(89, 'mahlen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(89, 'grind');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(89, 'mahlte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(89, 'hat', 'gemahlen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(90, 'meiden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(90, 'avoid');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(90, 'mied');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(90, 'hat', 'gemieden');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(91, 'melken', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(91, 'milk');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(91, 'melkte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(91, 'hat', 'gemelkt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(92, 'messen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(92, 'measure');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(92, 'maß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(92, 'hat', 'gemessen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(93, 'mißlingen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(93, 'fail');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(93, 'mißlang');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(93, 'hat', 'mißlungen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(94, 'mögen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(94, 'like');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(94, 'mochte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(94, 'hat', 'gemocht');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(95, 'müssen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(95, 'must');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(95, 'mußte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(95, 'hat', 'gemußt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(96, 'nehmen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(96, 'take');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(96, 'nahm');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(96, 'hat', 'genommen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(97, 'nennen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(97, 'name');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(97, 'nannte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(97, 'hat', 'genannt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(98, 'pfeifen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(98, 'whistle');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(98, 'pfiff');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(98, 'hat', 'gepfiffen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(99, 'pflegen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(99, 'care');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(99, 'pflegte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(99, 'hat', 'gepflegt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(100, 'preisen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(100, 'praise');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(100, 'pries');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(100, 'hat', 'gepriesen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(101, 'quellen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(101, 'well');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(101, 'quoll');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(101, 'hat', 'gequollen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(102, 'raten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(102, 'guess');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(102, 'riet');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(102, 'hat', 'geraten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(103, 'reiben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(103, 'rub');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(103, 'rieb');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(103, 'hat', 'gerieben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(104, 'reißen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(104, 'tear');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(104, 'riß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(104, 'hat', 'gerissen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(105, 'reiten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(105, 'ride');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(105, 'ritt');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(105, 'hat', 'geritten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(106, 'rennen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(106, 'run');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(106, 'rannte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(106, 'ist', 'gerannt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(107, 'riechen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(107, 'smell');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(107, 'roch');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(107, 'hat', 'gerochen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(108, 'ringen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(108, 'wrestle');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(108, 'rang');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(108, 'ist', 'gerungen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(109, 'rinnen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(109, 'trickle');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(109, 'rann');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(109, 'hat', 'geronnen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(110, 'rufen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(110, 'call');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(110, 'rief');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(110, 'hat', 'gerufen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(111, 'salzen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(111, 'salt');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(111, 'salzte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(111, 'hat', 'gesalzen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(112, 'saufen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(112, 'booze');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(112, 'soff');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(112, 'hat', 'gesoffen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(113, 'saugen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(113, 'suck');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(113, 'sog');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(113, 'hat', 'gesogen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(114, 'schaffen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(114, 'create');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(114, 'schuf');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(114, 'hat', 'geschaffen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(115, 'schallen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(115, 'resound');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(115, 'schallte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(115, 'hat', 'geschollen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(116, 'scheiden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(116, 'separate');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(116, 'schied');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(116, 'hat', 'geschieden');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(117, 'scheinen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(117, 'shine');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(117, 'schien');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(117, 'hat', 'geschienen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(118, 'scheißen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(118, 'shit');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(118, 'schiß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(118, 'hat', 'geschissen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(119, 'schelten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(119, 'scold');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(119, 'schalt');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(119, 'hat', 'gescholten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(120, 'scheren', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(120, 'shear');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(120, 'schor');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(120, 'hat', 'geschoren');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(121, 'schieben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(121, 'push');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(121, 'schob');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(121, 'hat', 'geschoben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(122, 'schießen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(122, 'shoot');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(122, 'schoß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(122, 'hat', 'geschossen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(123, 'schinden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(123, 'fleece');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(123, 'schindete');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(123, 'hat', 'geschunden');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(124, 'schlafen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(124, 'sleep');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(124, 'schlief');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(124, 'ist', 'geschlafen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(125, 'schlagen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(125, 'strike');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(125, 'schlug');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(125, 'hat', 'geschlagen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(126, 'schleichen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(126, 'creep');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(126, 'schlich');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(126, 'hat', 'geschlichen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(127, 'schleifen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(127, 'sharpen');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(127, 'schliff');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(127, 'hat', 'geschliffen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(128, 'schleißen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(128, 'pluck');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(128, 'schliß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(128, 'hat', 'geschlissen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(129, 'schließen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(129, 'close');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(129, 'schloß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(129, 'hat', 'geschlossen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(130, 'schlingen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(130, 'wind;');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(130, 'schlang');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(130, 'hat', 'geschlungen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(131, 'schmeißen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(131, 'chuck');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(131, 'schmiß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(131, 'hat', 'geschmissen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(132, 'schmelzen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(132, 'melt');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(132, 'schmolz');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(132, 'hat', 'geschmolzen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(133, 'schnauben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(133, 'snort');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(133, 'schnaubte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(133, 'hat', 'geschnaubt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(134, 'schneiden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(134, 'cut');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(134, 'schnitt');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(134, 'hat', 'geschnitten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(135, 'schreiben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(135, 'write');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(135, 'schrieb');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(135, 'hat', 'geschrieben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(136, 'schreien', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(136, 'scream');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(136, 'schrie');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(136, 'hat', 'geschrien');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(137, 'schreiten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(137, 'stride');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(137, 'schritt');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(137, 'hat', 'geschritten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(138, 'schwären', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(138, 'fester');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(138, 'schwärte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(138, 'hat', 'geschwärt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(139, 'schweigen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(139, 'clam');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(139, 'schwieg');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(139, 'hat', 'geschwiegen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(140, 'schwellen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(140, 'swell');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(140, 'schwoll');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(140, 'hat', 'geschwollen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(141, 'schwimmen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(141, 'swim');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(141, 'schwamm');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(141, 'ist', 'geschwommen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(142, 'schwinden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(142, 'disappear');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(142, 'schwand');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(142, 'hat', 'geschwunden');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(143, 'schwingen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(143, 'swing');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(143, 'schwang');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(143, 'hat', 'geschwungen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(144, 'schwören', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(144, 'swear');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(144, 'schwur');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(144, 'hat', 'geschworen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(145, 'sehen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(145, 'see');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(145, 'sah');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(145, 'hat', 'gesehen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(146, 'sein', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(146, 'be');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(146, 'war');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(146, 'hat', 'gewesen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(147, 'senden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(147, 'send');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(147, 'sandte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(147, 'hat', 'gesandt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(148, 'sieden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(148, 'boil');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(148, 'siedete');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(148, 'hat', 'gesiedet');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(149, 'singen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(149, 'sing');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(149, 'sang');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(149, 'hat', 'gesungen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(150, 'sinken', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(150, 'sink');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(150, 'sank');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(150, 'ist', 'gesunken');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(151, 'sinnen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(151, 'ponder');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(151, 'sann');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(151, 'hat', 'gesonnen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(152, 'sitzen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(152, 'sit');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(152, 'saß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(152, 'ist', 'gesessen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(153, 'sollen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(153, 'should');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(153, 'sollte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(153, 'hat', 'gesollt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(154, 'speien', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(154, 'spit');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(154, 'spie');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(154, 'hat', 'gespien');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(155, 'spinnen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(155, 'spin');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(155, 'spann');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(155, 'hat', 'gesponnen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(156, 'sprechen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(156, 'speak');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(156, 'sprach');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(156, 'hat', 'gesprochen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(157, 'sprießen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(157, 'spring');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(157, 'sproß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(157, 'hat', 'gesprossen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(158, 'springen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(158, 'jump');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(158, 'sprang');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(158, 'ist', 'gesprungen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(159, 'stechen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(159, 'prick');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(159, 'stach');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(159, 'hat', 'gestochen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(160, 'stecken', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(160, 'be');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(160, 'stak');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(160, 'steckte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(160, 'hat', 'gesteckt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(161, 'stehen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(161, 'stand');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(161, 'stand');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(161, 'ist', 'gestanden');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(162, 'stehlen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(162, 'steal');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(162, 'stahl');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(162, 'hat', 'gestohlen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(163, 'steigen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(163, 'climb');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(163, 'stieg');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(163, 'ist', 'gestiegen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(164, 'sterben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(164, 'die');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(164, 'starb');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(164, 'ist', 'gestorben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(165, 'stieben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(165, 'fly');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(165, 'stob');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(165, 'hat', 'gestoben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(166, 'stinken', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(166, 'stink');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(166, 'stank');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(166, 'hat', 'gestunken');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(167, 'stoßen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(167, 'shove');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(167, 'stieß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(167, 'hat', 'gestossen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(168, 'streichen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(168, 'stroke');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(168, 'strich');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(168, 'hat', 'gestrichen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(169, 'streiten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(169, 'argue');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(169, 'stritt');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(169, 'hat', 'gestritten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(170, 'tragen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(170, 'carry');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(170, 'trug');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(170, 'hat', 'getragen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(171, 'treffen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(171, 'meet');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(171, 'traf');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(171, 'hat', 'getroffen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(172, 'treiben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(172, 'drive');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(172, 'trieb');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(172, 'hat', 'getrieben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(173, 'treten', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(173, 'step');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(173, 'trat');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(173, 'hat', 'getreten');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(174, 'triefen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(174, 'drip');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(174, 'troff');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(174, 'hat', 'getroffen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(175, 'trinken', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(175, 'drink');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(175, 'trank');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(175, 'hat', 'getrunken');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(176, 'trügen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(176, 'deceive');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(176, 'trog');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(176, 'hat', 'getrogen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(177, 'tun', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(177, 'do');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(177, 'tat');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(177, 'hat', 'getan');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(178, 'verderben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(178, 'spoil');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(178, 'verdarb');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(178, 'hat', 'verdorben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(179, 'verdrießen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(179, 'peeve');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(179, 'verdroß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(179, 'hat', 'verdrossen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(180, 'vergessen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(180, 'forget');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(180, 'vergaß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(180, 'hat', 'vergessen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(181, 'verlieren', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(181, 'lose');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(181, 'verlor');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(181, 'hat', 'verloren');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(182, 'verschleißen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(182, 'wear');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(182, 'verschliß');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(182, 'hat', 'verschlissen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(183, 'wachsen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(183, 'grow');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(183, 'wuchs');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(183, 'ist', 'gewachsen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(184, 'wägen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(184, 'weigh');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(184, 'wog');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(184, 'hat', 'gewogen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(185, 'waschen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(185, 'wash');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(185, 'wusch');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(185, 'ist', 'gewaschen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(186, 'weben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(186, 'weave');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(186, 'webte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(186, 'hat', 'gewebt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(187, 'weichen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(187, 'yield');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(187, 'wich');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(187, 'hat', 'gewichen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(188, 'weisen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(188, 'how');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(188, 'wies');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(188, 'hat', 'gewiesen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(189, 'wenden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(189, 'turn');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(189, 'wandte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(189, 'hat', 'gewandt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(190, 'werben', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(190, 'win');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(190, 'warb');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(190, 'hat', 'geworben');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(191, 'werden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(191, 'become');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(191, 'wurde');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(191, 'hat', '(ge)worden');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(192, 'werfen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(192, 'throw');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(192, 'warf');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(192, 'hat', 'geworfen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(193, 'wiegen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(193, 'weigh');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(193, 'wog');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(193, 'hat', 'gewogen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(194, 'winden', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(194, 'wind');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(194, 'wand');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(194, 'hat', 'gewunden');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(195, 'winken', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(195, 'wave');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(195, 'winkte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(195, 'hat', 'gewinkt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(196, 'wissen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(196, 'know');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(196, 'wußte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(196, 'hat', 'gewußt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(197, 'wollen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(197, 'want');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(197, 'wollte');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(197, 'hat', 'gewollt');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(198, 'wringen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(198, 'wring');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(198, 'wrang');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(198, 'hat', 'gewrungen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(199, 'zeihen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(199, 'accuse');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(199, 'zieh');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(199, 'hat', 'geziehen');");

            db.execSQL("INSERT INTO verb (id, present, active) VALUES(200, 'ziehen', 1);");
            db.execSQL("INSERT INTO translation (verb_id, translation) VALUES(200, 'pull');");
            db.execSQL("INSERT INTO preterite (verb_id, preterite) VALUES(200, 'zog');");
            db.execSQL("INSERT INTO perfect (verb_id, aux_verb, perfect) VALUES(200, 'ist', 'gezogen');");
        }
		
	}
}
