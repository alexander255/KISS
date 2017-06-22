package fr.neamar.kiss.result;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import fr.neamar.kiss.R;
import fr.neamar.kiss.adapter.RecordAdapter;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.PhonePojo;

public class PhoneResult extends ResultView {
    private final PhonePojo phonePojo;

    public PhoneResult(PhonePojo phonePojo, Result result) {
        super();
        this.pojo = this.phonePojo = phonePojo;
        this.result = result;
    }

    @Override
    public View display(Context context, int position, View view) {
        if (view == null)
	        view = inflate(context);

        this.displayText(context, view);
        this.displayButtons(context, view);
        this.displayStaticIcon(context, view);

        return view;
    }

    @Override
    public Drawable getDrawable(Context context) {
        //noinspection deprecation: getDrawable(int, Theme) requires SDK 21+
        return context.getResources().getDrawable(android.R.drawable.ic_menu_call);
    }
}
