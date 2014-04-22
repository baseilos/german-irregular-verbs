package com.jozeflang.android.germanirregularverbs.main;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Settings activity
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
