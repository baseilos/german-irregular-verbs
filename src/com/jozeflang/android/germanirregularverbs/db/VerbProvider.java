package com.jozeflang.android.germanirregularverbs.db;


import android.content.Context;

import com.jozeflang.android.germanirregularverbs.util.Utils;
import com.jozeflang.android.germanirregularverbs.validator.AnswerType;

/** 
 * A word provider.
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class VerbProvider {
	
	/**
	 * Inits a new Verb provider
	 * @param context
	 */
	public VerbProvider(Context context) {
		VerbDatabase.INSTANCE.open(context);
	}
	
	/**
	 * Closes Verb provider
	 */
	public void closeProvider() {
		VerbDatabase.INSTANCE.close();
	}
	
	/**
	 * Returns next verb from database.<br />
	 * The next verb is chosen by pseudo-random number.
	 * @return
	 */
	public VerbDTO getNextVerb() {
		String[] verb = VerbDatabase.INSTANCE.getVerb(getNextVerbId());
		return VerbDTO.of(verb[0], verb[1], verb[2], verb[3], getNextAnswerType());
	}
	
	/**
	 * Returns verb count
	 * @return
	 */
	public long getVerbCount() {
		return VerbDatabase.INSTANCE.getVerbCount();
	}
	
	
	private int getNextVerbId() {
		return Utils.getRandom(1, Utils.longToInt(getVerbCount()));
	}
	
	private AnswerType getNextAnswerType() {
		int answerId = Utils.getRandom(0, AnswerType.values().length - 1);
		return AnswerType.values()[answerId];
	}
	
}
