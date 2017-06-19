package fr.neamar.kiss.result;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Shader;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.R;
import fr.neamar.kiss.UiTweaks;
import fr.neamar.kiss.adapter.RecordAdapter;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.ContactsPojo;
import fr.neamar.kiss.searcher.QueryInterface;

public class ContactsResult extends ResultView {
    private final ContactsPojo contactPojo;
    private final QueryInterface queryInterface;

    public ContactsResult(QueryInterface queryInterface, ContactsPojo contactPojo, Result result) {
        super();
        this.pojo = this.contactPojo = contactPojo;
        this.result = result;
        this.queryInterface = queryInterface;
    }

    @Override
    public View display(Context context, int position, View v) {
        if (v == null)
            v = inflate(context);

        // Contact name
        TextView contactName = (TextView) v.findViewById(R.id.result_text);
        contactName.setText(enrichText(contactPojo.displayName, context));

        // Contact phone
        TextView contactPhone = (TextView) v.findViewById(R.id.result_subtext);
        contactPhone.setText(contactPojo.phone);

        // Contact photo
        ImageView contactIcon = (ImageView) v.findViewById(R.id.result_icon);
        contactIcon.setImageDrawable(this.getDrawable(context));

        LinearLayout buttonContainer = (LinearLayout) v.findViewById(R.id.result_extras);
        buttonContainer.removeAllViews();

        PackageManager pm = context.getPackageManager();
        if(pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            int primaryColor = Color.parseColor(UiTweaks.getPrimaryColor(context));

            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.appSelectableItemBackground, typedValue, true);

            if(!contactPojo.homeNumber) {
                // Message action
                ImageButton messageButton = new ImageButton(context);
                messageButton.setImageResource(R.drawable.ic_message);
                messageButton.setColorFilter(primaryColor);
                messageButton.setBackgroundResource(typedValue.resourceId);
                messageButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchMessaging(v.getContext());
                    }
                });
                buttonContainer.addView(messageButton);
            }

            // Phone action
            ImageButton phoneButton = new ImageButton(context);
            phoneButton.setImageResource(R.drawable.ic_phone);
            phoneButton.setColorFilter(primaryColor);
            phoneButton.setBackgroundResource(typedValue.resourceId);
            phoneButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchCall(v.getContext());
                }
            });
            buttonContainer.addView(phoneButton);
        }

        return v;
    }

    @Override
    protected PopupMenu buildPopupMenu(Context context, final RecordAdapter parent, View parentView) {
        return inflatePopupMenu(R.menu.menu_item_contact, context, parentView);
    }

    @Override
    protected Boolean popupMenuClickHandler(Context context, RecordAdapter parent, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_contact_copy_phone:
                copyPhone(context, contactPojo);
                return true;
        }

        return super.popupMenuClickHandler(context, parent, item);
    }

    @SuppressWarnings("deprecation")
    private void copyPhone(Context context, ContactsPojo contactPojo) {
        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(
                "Phone number for " + contactPojo.displayName,
                contactPojo.phone);
        clipboard.setPrimaryClip(clip);
    }

    @SuppressWarnings("deprecation")
    public Drawable getBasicDrawable(Context context) {
        if (contactPojo.icon != null) {
            InputStream inputStream = null;
            try {
                inputStream = context.getContentResolver().openInputStream(contactPojo.icon);
                return Drawable.createFromStream(inputStream, null);
            } catch (FileNotFoundException ignored) {
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        // Default icon
        return context.getResources().getDrawable(R.drawable.ic_contact);
    }
    
    @Override
    public Drawable getDrawable(Context context) {
        final int width  = 48;
        final int height = 48;
        
        // Convert drawable to bitmap (will also scale the image)
        Drawable drawable = this.getBasicDrawable(context);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        
        // Draw circular center of bitmap (based on http://stackoverflow.com/a/18642747/277882)
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawCircle(width / 2, height / 2, width < height ? width / 2 : height / 2, paint);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    @Override
    public void doLaunch(Context context, View v) {
        Intent viewContact = new Intent(Intent.ACTION_VIEW);

        viewContact.setData(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                String.valueOf(contactPojo.lookupKey)));
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            viewContact.setSourceBounds(v.getClipBounds());
        }

        viewContact.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        viewContact.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(viewContact);
    }

    private void launchMessaging(final Context context) {
        String url = "sms:" + Uri.encode(contactPojo.phone);
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recordLaunch(context);
                queryInterface.launchOccurred(-1, ContactsResult.this);
            }
        }, KissApplication.TOUCH_DELAY);

    }

    private void launchCall(final Context context) {
        String url = "tel:" + Uri.encode(contactPojo.phone);
        Intent i = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recordLaunch(context);
                queryInterface.launchOccurred(-1, ContactsResult.this);
            }
        }, KissApplication.TOUCH_DELAY);

    }
}
