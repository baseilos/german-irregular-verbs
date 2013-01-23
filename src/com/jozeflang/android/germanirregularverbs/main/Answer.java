package com.jozeflang.android.germanirregularverbs.main;

/** 
 * Answer object
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class Answer {
	
	private final String auxVerb;
	private final String verb;
	
	public Answer(String auxVerb, String verb) {
		this.auxVerb = auxVerb;
		this.verb = verb;
	}
	
	public String getAuxVerb() {
		return auxVerb;
	}
	
	public String getVerb() {
		return verb;
	}
	
}
