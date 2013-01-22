package com.jozeflang.android.germanirregularverbs.validator;

import com.jozeflang.android.germanirregularverbs.db.VerbDTO;

/** 
 * Answer validator
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class AnswerValidator {

	private AnswerValidator() {
		
	}
	
	public static boolean validate(VerbDTO verb, final String answer, AnswerType answerType) {
		switch (answerType) {
		case PERFECT:
			return validatePerfect(verb, answer);
		case PRETERITE:
			return validatePreterite(verb, answer);
		default:
			return false;
		}
	}
	
	private static boolean validatePerfect(VerbDTO verb, final String answer) {
		return verb.getPerfect().equalsIgnoreCase(answer);
	}
	
	private static boolean validatePreterite(VerbDTO verb, final String answer) {
		return verb.getPreterite().equalsIgnoreCase(answer);
	}
	
}
