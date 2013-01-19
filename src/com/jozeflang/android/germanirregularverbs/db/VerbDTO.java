package com.jozeflang.android.germanirregularverbs.db;

/** 
 * A word data transfer object (DTO object).
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class VerbDTO {

	 private final String present;
	 private final String perfect;
	 private final String preterite;
	 
	 private VerbDTO(String present, String perfect, String preterite) {
		 this.present = present;
		 this.perfect = perfect;
		 this.preterite = preterite;
	 }
	 
	 public String getPresent() {
		return present;
	}

	public String getPerfect() {
		return perfect;
	}

	public String getPreterite() {
		return preterite;
	}

	static VerbDTO of(String present, String perfect, String preterite) {
		 return new VerbDTO(present, perfect, preterite);
	 }
	
}
