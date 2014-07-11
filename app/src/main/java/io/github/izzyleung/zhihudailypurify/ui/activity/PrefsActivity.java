package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import io.github.izzyleung.zhihudailypurify.ui.fragment.PrefsFragment;

public class PrefsActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new PrefsFragment())
                .commit();
    }
}