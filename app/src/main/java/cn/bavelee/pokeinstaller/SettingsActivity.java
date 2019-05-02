package cn.bavelee.pokeinstaller;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import cn.bavelee.donatedialog.DonateToMe;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        findPreference("donate").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DonateToMe.show(SettingsActivity.this);
                return false;
            }
        });
    }
}
