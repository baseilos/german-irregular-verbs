package com.jozeflang.android.germanirregularverbs.db.table;

/**
 * Verb table definition
 * @author Jozef Lang (developer@jozeflang.com) 
 * 
 * Table definition
 * <pre>
 * create table verb (
 * 	id integer primary key autoincrement,
 *  present varchar(100) not null
 * );
 * <pre>
 */
public class VerbTable {

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_PRESENT = "present";
    public static final String COLUMN_ACTIVE = "active";
	
	public static final String TABLE_NAME = "verb";
	public static final String TABLE_CREATE_SCRIPT =
			"create table " + TABLE_NAME + " ( "
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_PRESENT + " varchar(100) not null,"
            + COLUMN_ACTIVE + " integer default 1"
			+");";
	
	private VerbTable() {
	}
	
}
