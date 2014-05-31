package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import io.github.izzyleung.zhihudailypurify.R;

@SuppressWarnings("deprecation")
public class PrefsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        //noinspection ConstantConditions
        findPreference("about").setOnPreferenceClickListener(this);

        PackageManager pm = getPackageManager();
        try {
            if (pm != null) {
                pm.getPackageInfo("com.zhihu.android", PackageManager.GET_ACTIVITIES);
            }
        } catch (PackageManager.NameNotFoundException e) {
            //noinspection ConstantConditions
            ((PreferenceCategory) findPreference("settings_settings")).removePreference(findPreference("using_client?"));
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        //noinspection ConstantConditions
        if (preference.getKey().equals("about")) {
            showApacheLicenseDialog();
            return true;
        }
        return false;
    }

    private void showApacheLicenseDialog() {
        final Dialog apacheLicenseDialog = new Dialog(this);
        apacheLicenseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        apacheLicenseDialog.setCancelable(true);
        apacheLicenseDialog.setContentView(R.layout.dialog_apache_license);

        TextView textView = (TextView) apacheLicenseDialog.findViewById(R.id.dialog_text);

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.licences_header));

        String[] basedOnProjects = getResources().getStringArray(R.array.apache_licensed_projects);

        for (String str : basedOnProjects) {
            sb.append("• ").append(str).append("\n");
        }

        sb.append("\n").append(getString(R.string.licenses_subheader));
        textView.setText(sb.toString());

        apacheLicenseDialog.findViewById(R.id.close_dialog_button)
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        apacheLicenseDialog.dismiss();
                    }
                });

        apacheLicenseDialog.show();
    }
}