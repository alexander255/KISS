package fr.neamar.kiss.result;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.R;
import fr.neamar.kiss.adapter.RecordAdapter;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.AppPojo;
import fr.neamar.kiss.utils.SpaceTokenizer;

public class AppResult extends ResultView {
    private final AppPojo appPojo;

    private final ComponentName className;

    private Drawable icon = null;

    public AppResult(AppPojo appPojo, Result result) {
        super();
        this.pojo = this.appPojo = appPojo;
        this.result = result;

        className = new ComponentName(appPojo.packageName, appPojo.activityName);
    }

    @Override
    public View display(final Context context, int position, View v) {
        if (v == null) {
            v = inflate(context);
        }

        this.displayText(context, v);
        this.displayButtons(context, v);

        TextView tagsView = (TextView) v.findViewById(R.id.result_subtext);
        //Hide tags view if tags are empty or if user has selected to hide them and the query doesnt match tags
        if (appPojo.displayTags.isEmpty() ||
                ((!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("tags-visible", true)) && (appPojo.displayTags.equals(appPojo.tags)))) {
            tagsView.setVisibility(View.GONE);
        }
        else {
            tagsView.setVisibility(View.VISIBLE);
        }

        final ImageView appIcon = (ImageView) v.findViewById(R.id.result_icon);

        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("icons-hide", false)) {
            // Display icon directy for first icons, and also for phones above lollipop
            // (fix a weird recycling issue with ListView on Marshmallow,
            // where the recycling occurs synchronously, before the handler)
            if (position < 15 || Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                appIcon.setImageDrawable(this.getDrawable(context));
            } else {
                // Do actions on a message queue to avoid performance issues on main thread
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        appIcon.setImageDrawable(getDrawable(context));
                    }
                });
            }
        }
        else {
            appIcon.setVisibility(View.INVISIBLE);
        }
        return v;
    }

    @Override
    public Drawable getDrawable(Context context) {
        
        if (icon == null) {
             icon = KissApplication.getIconsHandler(context).getDrawableIconForPackage(className, this.appPojo.userHandle);
        }
                
        return icon;
        
    }
}
