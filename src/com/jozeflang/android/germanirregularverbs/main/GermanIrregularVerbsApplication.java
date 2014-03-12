package com.jozeflang.android.germanirregularverbs.main;

import android.app.Application;
import com.jozeflang.android.germanirregularverbs.db.VerbDTO;
import com.jozeflang.android.germanirregularverbs.db.VerbProvider;
import com.jozeflang.android.germanirregularverbs.main.data.Question;

import java.util.List;
import java.util.logging.Logger;

/**
 * Application class
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class GermanIrregularVerbsApplication extends Application {

	private final Logger logger = Logger.getLogger(GermanIrregularVerbsApplication.class.getName());
	
	private VerbProvider verbProvider;
	private Question activeQuestion;
	
	@Override
	public void onCreate() {
		super.onCreate();
		verbProvider = new VerbProvider(this);
		
		// Generate first question
		getQuestion(true);
	}

	@Override
	public void onTerminate() {
		verbProvider.closeProvider();
	}
	
	public Question getQuestion() {
		return activeQuestion;
	}
	
	public Question getQuestion(boolean newQuestion) {
		if (newQuestion) {
			logger.info(String.format("Generating new question"));
			activeQuestion = verbProvider.getQuestion();
		}
		return activeQuestion;
	}

    public List<VerbDTO> getVerbs(boolean onlyActive) {
        return getVerbs(onlyActive, "");
    }

    public List<VerbDTO> getVerbs(boolean onlyActive, String filter) {
        return verbProvider.getAllVerbs(onlyActive, filter);
    }
	
}
