package com.jozeflang.android.germanirregularverbs.main;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Settings activity
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class SettingsActivity extends PreferenceActivity {

    private GermanIrregularVerbsApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (GermanIrregularVerbsApplication) getApplication();
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        application.sendAnalyticsHit(SettingsActivity.class.getSimpleName());
    }
}
