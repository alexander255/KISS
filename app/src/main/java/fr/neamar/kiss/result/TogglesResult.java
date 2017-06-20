package fr.neamar.kiss.result;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.TogglesPojo;
import fr.neamar.kiss.toggles.TogglesHandler;

public class TogglesResult extends ResultView {
    private final TogglesPojo togglePojo;
    private Switch toggleButton;

    /**
     * Handler for all toggle-related queries
     */
    private TogglesHandler togglesHandler = null;

    public TogglesResult(TogglesPojo togglePojo, Result result) {
        super();
        this.pojo = this.togglePojo = togglePojo;
        this.result = result;
    }

    @SuppressWarnings({"ResourceType", "deprecation"})
    @Override
    public View display(Context context, int position, View v) {
        // On first run, initialize handler
        if (togglesHandler == null)
            togglesHandler = new TogglesHandler(context);

        if (v == null)
            v = inflate(context);

        this.displayText(context, v);

        ImageView toggleIcon = (ImageView) v.findViewById(R.id.result_icon);
        toggleIcon.setImageDrawable(context.getResources().getDrawable(togglePojo.icon));
        toggleIcon.setColorFilter(getThemeFillColor(context), Mode.SRC_IN);

        LinearLayout buttonContainer = (LinearLayout) v.findViewById(R.id.result_extras);
        buttonContainer.removeAllViews();

        // Use the handler to check or un-check button
        this.toggleButton = new Switch(context);
        buttonContainer.addView(this.toggleButton);

        //set listener to null to avoid calling the listener of the older toggle item
        //(due to recycling)
        toggleButton.setOnCheckedChangeListener(null);

        Boolean state = togglesHandler.getState(togglePojo);
        if (state != null)
            toggleButton.setChecked(togglesHandler.getState(togglePojo));
        else
            toggleButton.setEnabled(false);

        toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!togglesHandler.getState(togglePojo).equals(toggleButton.isChecked())) {

                    // record launch manually
                    recordLaunch(buttonView.getContext());

                    togglesHandler.setState(togglePojo, toggleButton.isChecked());

                    toggleButton.setEnabled(false);
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            super.onPostExecute(result);
                            toggleButton.setEnabled(true);
                        }

                    }.execute();
                }
            }
        });
        return v;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Drawable getDrawable(Context context) {
        return context.getResources().getDrawable(togglePojo.icon);
    }

    @Override
    public void doLaunch(Context context, View v) {
        if (this.toggleButton != null) {
            // Use the handler to check or un-check button
            if (this.toggleButton.isEnabled()) {
                this.toggleButton.performClick();
            }
        } else {
            //in case it is pinned on kissbar
            if (togglesHandler == null) {
                togglesHandler = new TogglesHandler(context);
            }

            //get message based on current state of toggle
            String msg = context.getResources().getString(togglesHandler.getState(togglePojo) ? R.string.toggles_off : R.string.toggles_on);

            //toggle state
            togglesHandler.setState(togglePojo, !togglesHandler.getState(togglePojo));

            //show toast to inform user what the state is
            Toast.makeText(context, String.format(msg, " " + this.pojo.displayName), Toast.LENGTH_SHORT).show();

        }
    }

}
