package etchee.com.weightlifty.Activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import etchee.com.weightlifty.R;

/**
 * Preference screen
 * Created by rikutoechigoya on 2017/05/11.
 */

public class SettingsActivity extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


}
