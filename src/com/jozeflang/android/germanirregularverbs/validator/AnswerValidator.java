package com.jozeflang.android.germanirregularverbs.validator;

import com.jozeflang.android.germanirregularverbs.db.VerbDTO;
import com.jozeflang.android.germanirregularverbs.main.data.Answer;
import com.jozeflang.android.germanirregularverbs.util.Utils;

/** 
 * Answer validator
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class AnswerValidator {

	private AnswerValidator() {
		
	}
	
	public static boolean validate(VerbDTO verb, final Answer answer, AnswerType answerType) {
		switch (answerType) {
		case PERFECT:
			return validatePerfect(verb, answer);
		case PRETERITE:
			return validatePreterite(verb, answer);
		default:
			return false;
		}
	}
	
	private static boolean validatePerfect(VerbDTO verb, final Answer answer) {
		for (VerbDTO.Perfect p : verb.getPerfects()) {
			if (Utils.escapeGermanCharacters(p.getAuxVerb()).equalsIgnoreCase(answer.getAuxVerb()) 
					&& Utils.escapeGermanCharacters(p.getPerfect()).equalsIgnoreCase(answer.getVerb()))
				return true;
		}
		return false;
	}
	
	private static boolean validatePreterite(VerbDTO verb, final Answer answer) {
		for (VerbDTO.Preterite p : verb.getPreterites()) {
			if (Utils.escapeGermanCharacters(p.getPreterite()).equalsIgnoreCase(answer.getVerb()))
				return true;
		}
		return false;
	}
	
}
