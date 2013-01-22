package com.jozeflang.android.germanirregularverbs.validator;

import com.jozeflang.android.germanirregularverbs.main.R;

/** 
 * Answer types
 * @author Jozef Lang (developer@jozeflang.com)
 */
public enum AnswerType {
	PERFECT(R.string.answerType_perfect),
	PRETERITE(R.string.answerType_preterite);
	
	private final int stringId;
	
	private AnswerType(int stringId) {
		this.stringId = stringId;
	}
	
	public int getStringId() {
		return stringId;
	}
}
