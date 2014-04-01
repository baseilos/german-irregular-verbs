package com.jozeflang.android.germanirregularverbs.main;

import android.app.Application;
import android.graphics.Typeface;
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

    private Typeface fontAwesome;
	private VerbProvider verbProvider;
	private Question activeQuestion;
	
	@Override
	public void onCreate() {
		super.onCreate();
        fontAwesome = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
		verbProvider = new VerbProvider(this);
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

    public void updateVerb(VerbDTO verb) {
        int rowsUpdated = verbProvider.updateVerb(verb);
        logger.fine(String.format("Verb %s updated. Updated %d records", verb.getPresent(), rowsUpdated));

    }

    public void setVerbsActivness(boolean isActive) {
        verbProvider.setVerbsActivness(isActive);
    }

    public void invertVerbActivness() {
        verbProvider.invertVerbsActivness();
    }

    public long getVerbCount(boolean activeOnly) {
        return verbProvider.getVerbCount(activeOnly);
    }

    public Typeface getFontAwesome() {
        return fontAwesome;
    }
}
