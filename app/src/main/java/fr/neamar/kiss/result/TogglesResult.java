package fr.neamar.kiss.result;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
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
        this.displayButtons(context, v);

        ImageView toggleIcon = (ImageView) v.findViewById(R.id.result_icon);
        toggleIcon.setImageDrawable(context.getResources().getDrawable(togglePojo.icon));
        toggleIcon.setColorFilter(getThemeFillColor(context), Mode.SRC_IN);

        LinearLayout buttonContainer = (LinearLayout) v.findViewById(R.id.result_extras);

        // Use the handler to check or un-check button
        this.toggleButton = (Switch) buttonContainer.getChildAt(0);

        Boolean state = togglesHandler.getState(togglePojo);
        if (state != null) {
            toggleButton.setChecked(togglesHandler.getState(togglePojo));
            toggleButton.setEnabled(true);
        }
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
