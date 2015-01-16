package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.ui.fragment.PrefsFragment;

public class PrefsActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction().replace(R.id.prefs_frame, new PrefsFragment()).commit();
    }

}