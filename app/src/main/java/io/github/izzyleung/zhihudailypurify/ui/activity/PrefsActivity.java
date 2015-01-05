package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.Check;

@SuppressWarnings("deprecation")
public class PrefsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);
        findPreference("about").setOnPreferenceClickListener(this);

        if (!Check.isZhihuClientInstalled()) {
            ((PreferenceCategory) findPreference("settings_settings"))
                    .removePreference(findPreference("using_client?"));
        }

        if (!PreferenceManager.getDefaultSharedPreferences(PrefsActivity.this)
                .getBoolean("enable_accelerate_server?", false)) {
            ((PreferenceScreen) findPreference("preference_screen"))
                    .removePreference(findPreference("settings_network_settings"));
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("about")) {
            showApacheLicenseDialog();
            return true;
        }
        return false;
    }

    private void showApacheLicenseDialog() {
        final Dialog apacheLicenseDialog = new Dialog(PrefsActivity.this);
        apacheLicenseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        apacheLicenseDialog.setCancelable(true);
        apacheLicenseDialog.setContentView(R.layout.dialog_apache_license);

        TextView textView = (TextView) apacheLicenseDialog.findViewById(R.id.dialog_text);

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.licences_header)).append("\n");

        String[] basedOnProjects = getResources().getStringArray(R.array.apache_licensed_projects);

        for (String str : basedOnProjects) {
            sb.append("• ").append(str).append("\n");
        }

        sb.append("\n").append(getString(R.string.licenses_subheader));
        sb.append("\n\n").append(getString(R.string.apache_license));
        textView.setText(sb.toString());

        Button closeDialogButton = (Button) apacheLicenseDialog.findViewById(R.id.close_dialog_button);

        closeDialogButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                apacheLicenseDialog.dismiss();
            }
        });

        closeDialogButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                apacheLicenseDialog.dismiss();
                Toast.makeText(PrefsActivity.this,
                        PrefsActivity.this.getString(R.string.accelerate_server_unlock),
                        Toast.LENGTH_SHORT).show();
                PreferenceManager.getDefaultSharedPreferences(PrefsActivity.this)
                        .edit().putBoolean("enable_accelerate_server?", true).apply();
                return true;
            }
        });

        apacheLicenseDialog.show();
    }
}