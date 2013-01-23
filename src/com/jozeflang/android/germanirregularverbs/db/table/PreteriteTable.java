package com.jozeflang.android.germanirregularverbs.db.table;

/**
 * Preterite table definition
 * @author Jozef Lang (developer@jozeflang.com) 
 * 
 * Table definition
 * <pre>
 * CREATE TABLE preterite (
 * 	id integer primary key autoincrement,
 *  verb_id integer not null,
 *  preterite varchar(100) not null,
 *  FOREIGN KEY(verb_id) REFERENCES(id)
 * );
 * <pre>
 */
public class PreteriteTable {

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_VERB_ID = "verb_id";
	public static final String COLUMN_PRETERITE = "preterite";
	
	public static final String TABLE_NAME = "preterite";
	public static final String TABLE_CREATE_SCRIPT =
			"create table " + TABLE_NAME + " ( "
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_VERB_ID + " integer not null,"
			+ COLUMN_PRETERITE + " varchar(100) not null, " 
			+ "foreign key("+ COLUMN_VERB_ID + ") references " + VerbTable.TABLE_NAME + "(" + VerbTable.COLUMN_ID + ")" 
			+");";
	
	private PreteriteTable() {
	}
	
}
