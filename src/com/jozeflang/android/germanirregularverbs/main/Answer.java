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
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Answer))
			return false;
		Answer oAnswer = (Answer) o;
		return this.auxVerb.equals(oAnswer.auxVerb) && this.verb.equals(oAnswer.verb);
	}
	
	@Override
	public int hashCode() {
		int prime = 17;
		int result = 1;
		result = (result*prime) + (auxVerb == null ? 0 : auxVerb.hashCode());
		result = (result*prime) + (verb == null ? 0 : verb.hashCode());
		return result;
	}
	
}
