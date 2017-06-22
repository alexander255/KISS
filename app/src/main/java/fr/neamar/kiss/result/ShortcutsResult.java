package fr.neamar.kiss.result;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.List;

import fr.neamar.kiss.DataHandler;
import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.R;
import fr.neamar.kiss.adapter.RecordAdapter;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.ShortcutsPojo;

public class ShortcutsResult extends ResultView {
    private final ShortcutsPojo shortcutPojo;

    public ShortcutsResult(ShortcutsPojo shortcutPojo, Result result) {
        super();
        this.pojo = this.shortcutPojo = shortcutPojo;
        this.result = result;
    }

    @Override
    public View display(final Context context, int position, View view) {
        if (view == null)
	        view = inflate(context);

        this.displayText(context, view);
        this.displayButtons(context, view);
        this.displayStaticIcon(context, view);

        final ImageView shortcutIcon = (ImageView) view.findViewById(R.id.result_icon);
        final ImageView appIcon = (ImageView) view.findViewById(R.id.result_subicon);

        // Retrieve package icon for this shortcut
        final PackageManager packageManager = context.getPackageManager();
        Drawable appDrawable = null;
        try {
            Intent intent = Intent.parseUri(shortcutPojo.intentUri, 0);
            List<ResolveInfo> packages = packageManager.queryIntentActivities(intent, 0);
            if(packages.size() > 0) {
                ResolveInfo mainPackage = packages.get(0);
                String packageName = mainPackage.activityInfo.applicationInfo.packageName;
                String activityName = mainPackage.activityInfo.name;
                ComponentName className =  new ComponentName(packageName, activityName);
                appDrawable = context.getPackageManager().getActivityIcon(className);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return view;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return view;
        }

        if (shortcutPojo.icon != null) {
            BitmapDrawable drawable = new BitmapDrawable(context.getResources(), shortcutPojo.icon);
            shortcutIcon.setImageDrawable(drawable);
            appIcon.setImageDrawable(appDrawable);
        }

        return view;
    }

    public Drawable getDrawable(Context context) {
        return new BitmapDrawable(context.getResources(), shortcutPojo.icon);
    }
}
