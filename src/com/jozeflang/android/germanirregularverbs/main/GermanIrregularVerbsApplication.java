package com.jozeflang.android.germanirregularverbs.main;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.jozeflang.android.germanirregularverbs.db.VerbDTO;
import com.jozeflang.android.germanirregularverbs.db.VerbProvider;
import com.jozeflang.android.germanirregularverbs.main.data.Question;
import com.jozeflang.android.germanirregularverbs.validator.AnswerType;

import java.util.List;
import java.util.logging.Logger;

/**
 * Application class
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class GermanIrregularVerbsApplication extends Application {

	private final Logger logger = Logger.getLogger(GermanIrregularVerbsApplication.class.getName());

    private Tracker analyticsTracker;
    private Typeface fontAwesome;
	private VerbProvider verbProvider;
	private Question activeQuestion;
    private SharedPreferences preferences;
	
	@Override
	public void onCreate() {
		super.onCreate();
        fontAwesome = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
		verbProvider = new VerbProvider(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}

	@Override
	public void onTerminate() {
        super.onTerminate();
		verbProvider.closeProvider();
	}
	
	public Question getQuestion() {




        return activeQuestion;
	}
	
	public Question getQuestion(boolean newQuestion) {
		if (newQuestion) {
			logger.info(String.format("Generating new question"));
			activeQuestion = getNextQuestion();
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

    public void sendAnalyticsHit(String screenName) {
        HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder();
        // https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters
        builder.set("&cd", screenName);
        getDefaultTracker().send(builder.build());
    }

    synchronized private Tracker getDefaultTracker() {
        if (analyticsTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analyticsTracker = analytics.newTracker("UA-68998946-1");
        }
        return analyticsTracker;
    }

    private Question getNextQuestion() {
        String verbFrom = preferences.getString("pref_generateVerbForm", "BOTH");
        if (TextUtils.equals("BOTH", verbFrom)) {
            return verbProvider.getQuestion();
        } else {
            return verbProvider.getQuestion(AnswerType.valueOf(verbFrom));
        }
    }
}
