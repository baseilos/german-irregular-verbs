package com.jozeflang.android.germanirregularverbs.db;

import com.jozeflang.android.germanirregularverbs.validator.AnswerType;

/** 
 * A word data transfer object (DTO object).
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class VerbDTO {
	
	 private final String translation;
	 private final String present;
	 private final String perfect;
	 private final String preterite;
	 private final AnswerType answerType;
	 
	 private VerbDTO(String translation, String present, String perfect, String preterite, AnswerType answerType) {
		 this.translation = translation;
		 this.present = present;
		 this.perfect = perfect;
		 this.preterite = preterite;
		 this.answerType = answerType;
	 }
	 
	 public String getTranslation() {
		 return translation;
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
	
	public AnswerType getAnswerType() {
		return answerType;
	}

	static VerbDTO of(String translation, String present, String perfect, String preterite, AnswerType answerType) {
		 return new VerbDTO(translation, present, perfect, preterite, answerType);
	 }
	
}
