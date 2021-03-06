package com.jozeflang.android.germanirregularverbs.db.table;

/**
 * Perfect table definition
 * @author Jozef Lang (developer@jozeflang.com) 
 * 
 * Table definition
 * <pre>
 * CREATE TABLE perfect (
 * 	id integer primary key autoincrement,
 *  verb_id integer not null,
 *  aux_verb varchar(100) not null,
 *  perfect varchar(100) not null,
 *  FOREIGN KEY(verb_id) REFERENCES(id)
 * );
 * <pre>
 */
public class PerfectTable {

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_VERB_ID = "verb_id";
	public static final String COLUMN_AUX_VERB = "aux_verb";
	public static final String COLUMN_PERFECT = "perfect";
	
	public static final String TABLE_NAME = "perfect";
	public static final String TABLE_CREATE_SCRIPT =
			"create table " + TABLE_NAME + " ( "
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_VERB_ID + " integer not null,"
			+ COLUMN_AUX_VERB + " varchar(100) not null, " 
			+ COLUMN_PERFECT + " varchar(100) not null, " 
			+ "foreign key("+ COLUMN_VERB_ID + ") references " + VerbTable.TABLE_NAME + "(" + VerbTable.COLUMN_ID + ")"
			+");";
	
	private PerfectTable() {
		
	}
	
}
