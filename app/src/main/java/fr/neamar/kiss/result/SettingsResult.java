package fr.neamar.kiss.result;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.SettingsPojo;

public class SettingsResult extends ResultView {
    private final SettingsPojo settingPojo;

    public SettingsResult(SettingsPojo settingPojo, Result result) {
        super();
        this.pojo = this.settingPojo = settingPojo;
        this.result = result;
    }

    @Override
    public View display(Context context, int position, View v) {
        if (v == null)
            v = inflate(context);

        this.displayText(context, v);

        ImageView settingIcon = (ImageView) v.findViewById(R.id.result_icon);
        Drawable drawable = getDrawable(context);
        if(drawable != null) {
            settingIcon.setImageDrawable(drawable);
        } else {
            settingIcon.setImageResource(android.R.drawable.ic_menu_manage);
        }
        settingIcon.setColorFilter(getThemeFillColor(context), Mode.SRC_IN);

        return v;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Drawable getDrawable(Context context) {
        if (settingPojo.icon != -1) {
            return context.getResources().getDrawable(settingPojo.icon);
        }

        return null;
    }
}
