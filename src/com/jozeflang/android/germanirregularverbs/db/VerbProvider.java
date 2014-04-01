package com.jozeflang.android.germanirregularverbs.db;


import android.content.Context;
import com.jozeflang.android.germanirregularverbs.main.data.Question;
import com.jozeflang.android.germanirregularverbs.util.Utils;
import com.jozeflang.android.germanirregularverbs.validator.AnswerType;

import java.util.List;

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
		return VerbDatabase.INSTANCE.getNthVerb(getNextVerbPosition(), true);
	}
	
	public AnswerType getAnswerType() {
		int answerId = Utils.getRandom(0, AnswerType.values().length - 1);
		return AnswerType.values()[answerId];
	}
	
	/**
	 * A convenient method for generating new question<br />
	 * Method calls {@link VerbProvider#getAnswerType()} and {@link VerbProvider#getNextVerb()} 
	 * and constructs new Question object from retrieved values. 
	 * @return
	 */
	public Question getQuestion() {
		return new Question(getAnswerType(), getNextVerb());
	}
	
	/**
	 * Returns verb count
     * @param onlyActive
	 * @return
	 */
	public long getVerbCount(boolean onlyActive) {
		return VerbDatabase.INSTANCE.getVerbCount(onlyActive);
	}

    /**
     * Returns a list of all verbs
     * @param onlyAcive
     * @param filter
     * @return
     */
    public List<VerbDTO> getAllVerbs(boolean onlyAcive, String filter) {
        return VerbDatabase.INSTANCE.getAllVerbs(onlyAcive, filter);
    }

    /**
     * Updates verb
     * @param verb
     * @return
     */
    public int updateVerb(VerbDTO verb) {
        return VerbDatabase.INSTANCE.updateVerb(verb);
    }

    public void setVerbsActivness(boolean isActive) {
        VerbDatabase.INSTANCE.setVerbsActivness(isActive);
    }

    public void invertVerbsActivness() {
        VerbDatabase.INSTANCE.invertVerbActivness();
    }
	
	private int getNextVerbId() {
		return Utils.getRandom(1, Utils.longToInt(getVerbCount(true)));
	}

    private int getNextVerbPosition() {
        return Utils.getRandom(0, Utils.longToInt(getVerbCount(true)-1));
    }
}
