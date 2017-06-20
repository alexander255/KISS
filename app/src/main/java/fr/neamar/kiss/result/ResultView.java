package fr.neamar.kiss.result;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.MenuRes;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.andreinc.aleph.AlephFormatter;
import static net.andreinc.aleph.AlephFormatter.template;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.R;
import fr.neamar.kiss.UiTweaks;
import fr.neamar.kiss.adapter.RecordAdapter;
import fr.neamar.kiss.api.provider.ButtonAction;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.db.DBHelper;
import fr.neamar.kiss.pojo.AppPojo;
import fr.neamar.kiss.pojo.ContactsPojo;
import fr.neamar.kiss.pojo.PhonePojo;
import fr.neamar.kiss.pojo.Pojo;
import fr.neamar.kiss.pojo.SearchPojo;
import fr.neamar.kiss.pojo.SettingsPojo;
import fr.neamar.kiss.pojo.ShortcutsPojo;
import fr.neamar.kiss.pojo.TogglesPojo;
import fr.neamar.kiss.searcher.QueryInterface;

public abstract class ResultView {
    /**
     * Current information pojo
     */
    Result result = null;
    Pojo pojo = null;

    public static ResultView fromResult(QueryInterface parent, Result result) {
        if (result.pojo instanceof AppPojo)
            return new AppResult((AppPojo) result.pojo, result);
        else if (result.pojo instanceof ContactsPojo)
            return new ContactsResult(parent, (ContactsPojo) result.pojo, result);
        else if (result.pojo instanceof SearchPojo)
            return new SearchResult((SearchPojo) result.pojo, result);
        else if (result.pojo instanceof SettingsPojo)
            return new SettingsResult((SettingsPojo) result.pojo, result);
        else if (result.pojo instanceof TogglesPojo)
            return new TogglesResult((TogglesPojo) result.pojo, result);
        else if (result.pojo instanceof PhonePojo)
            return new PhoneResult((PhonePojo) result.pojo, result);
        else if (result.pojo instanceof ShortcutsPojo)
            return new ShortcutsResult((ShortcutsPojo) result.pojo, result);


        throw new RuntimeException("Unable to create a result from POJO");
    }
	
	
	/**
	 * Render the text of this result based on its UI description
	 */
	protected void displayText(Context context, View v) {
		// Put text rendering templates into templating engine
		AlephFormatter text    = template(this.result.userInterface.textTemplate);
		AlephFormatter subtext = template(this.result.userInterface.subtextTemplate);
		
		// Apply template substitutions
		for(Map.Entry<String, String> entry : this.result.templateParameters.entrySet()) {
			text    = text.arg(entry.getKey(), escapeHtml(entry.getValue()));
			subtext = subtext.arg(entry.getKey(), escapeHtml(entry.getValue()));
		}
		
		// *Enrich* the final text and display it
		TextView textView = (TextView) v.findViewById(R.id.result_text);
		textView.setText(enrichText(text.fmt(), context));
		
		TextView subtextView = (TextView) v.findViewById(R.id.result_subtext);
		subtextView.setText(enrichText(subtext.fmt(), context));
		
		// Hide subtext view if it is empty
		subtextView.setVisibility(subtextView.getText().length() > 0 ? View.VISIBLE : View.GONE);
	}
	
	
	protected void displayButtons(Context context, View v) {
		// Clear previous contents of button container (in case it was recycled)
		LinearLayout buttonContainer = (LinearLayout) v.findViewById(R.id.result_extras);
		buttonContainer.removeAllViews();
		
		if(this.result.userInterface.buttonActions.length < 1) {
			return;
		}
		
		// Obtain primary color value
		@ColorInt
		int primaryColor = Color.parseColor(UiTweaks.getPrimaryColor(context));
		
		// Calculate darker and lighter version of the color value for styling toggle buttons
		float[] primaryColorHSV = new float[3];
		Color.colorToHSV(primaryColor, primaryColorHSV);
		
		@ColorInt
		int primaryColorLight = Color.HSVToColor(new float[] {
				primaryColorHSV[0], primaryColorHSV[1], primaryColorHSV[2] + 0.2f
		});
		@ColorInt
		int primaryColorDark  = Color.HSVToColor(new float[] {
				primaryColorHSV[0], primaryColorHSV[1], primaryColorHSV[2] - 0.2f
		});
		
		// Create awful thing that tells the system how to render the different states of the
		// toggle button's thumb
		ColorStateList thumbColors = new ColorStateList(new int[][]{
				{ android.R.attr.state_checked, android.R.attr.state_pressed },
				{ android.R.attr.state_pressed },
				{ android.R.attr.state_checked },
				{-android.R.attr.state_enabled },
				{}
		}, new int[] {
				primaryColor,
				primaryColorLight,
				primaryColor,
				primaryColorDark,
				primaryColorLight
		});
		
		// Get theme background appearance
		TypedValue background = new TypedValue();
		context.getTheme().resolveAttribute(R.attr.appSelectableItemBackground, background, true);
		
		for(final ButtonAction buttonAction : this.result.userInterface.buttonActions) {
			switch(buttonAction.type) {
				case IMAGE_BUTTON:
					ImageButton imageButton = new ImageButton(context);
					imageButton.setImageBitmap(buttonAction.icon);
					imageButton.setColorFilter(primaryColor);
					imageButton.setBackgroundResource(background.resourceId);
					buttonContainer.addView(imageButton);
					
					imageButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								ResultView.this.result.callbacks.onButtonAction(buttonAction.action, 0);
							} catch(RemoteException e) {
								// Ignore errors if remote provider went away
								Log.w("ResultView", "Could not dispatch result view button action to provider");
								e.printStackTrace();
							}
						}
					});
					break;
				
				case TOGGLE_BUTTON:
					Switch toggleButton = new Switch(context);
					toggleButton.setEnabled(false);
					try {
						Method setThumbTintList = Switch.class.getMethod("setThumbTintList", ColorStateList.class);
						setThumbTintList.invoke(toggleButton, thumbColors);
					} catch(Exception e) {
						// Only available in Android 6+
						//TODO: Replace this by version check once we finally target Marshmallow
						e.printStackTrace();
					}
					buttonContainer.addView(toggleButton);
					
					//TODO: Query button state from provider
					
					toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							try {
								ResultView.this.result.callbacks.onButtonAction(buttonAction.action, isChecked ? 1 : 0);
							} catch(RemoteException e) {
								// Ignore errors if remote provider went away
								Log.w("ResultView", "Could not dispatch result view button action to provider");
								e.printStackTrace();
							}
						}
					});
					
					break;
			}
		}
	}
	
	
	/**
	 * `android.text.Html.escapeHtml` polyfill for Ice Cream Sandwich (4.0.3 / 15)
	 *
	 * Source code copied from the Android Nougat AOSP source code (Apache License 2.0).
	 */
	private static String escapeHtml(CharSequence text) {
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			return Html.escapeHtml(text);
		}
		
		StringBuilder out = new StringBuilder();
		
		int start = 0;
		int end   = text.length();
		
		for (int i = start; i < end; i++) {
			char c = text.charAt(i);
			
			if (c == '<') {
				out.append("&lt;");
			} else if (c == '>') {
				out.append("&gt;");
			} else if (c == '&') {
				out.append("&amp;");
			} else if (c >= 0xD800 && c <= 0xDFFF) {
				if (c < 0xDC00 && i + 1 < end) {
					char d = text.charAt(i + 1);
					if (d >= 0xDC00 && d <= 0xDFFF) {
						i++;
						int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
						out.append("&#").append(codepoint).append(";");
					}
				}
			} else if (c > 0x7E || c < ' ') {
				out.append("&#").append((int) c).append(";");
			} else if (c == ' ') {
				while (i + 1 < end && text.charAt(i + 1) == ' ') {
					out.append("&nbsp;");
					i++;
				}
				
				out.append(' ');
			} else {
				out.append(c);
			}
		}
		
		return out.toString();
	}
    
    /**
     * How to display this record ?
     *
     * @param context     android context
     * @param convertView a view to be recycled
     * @return a view to display as item
     */
    public abstract View display(Context context, int position, View convertView);

    /**
     * How to display the popup menu
     *
     * @return a PopupMenu object
     */
    public PopupMenu getPopupMenu(final Context context, final RecordAdapter parent, View parentView) {
        PopupMenu menu = buildPopupMenu(context, parent, parentView);

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                return popupMenuClickHandler(context, parent, item);
            }
        });

        return menu;
    }

	/**
	 * Default popup menu implementation, can be overridden by children class to display a more specific menu
	 *
	 * @return an inflated, listener-free PopupMenu
	 */
	PopupMenu buildPopupMenu(Context context, final RecordAdapter parent, View parentView) {
		PopupMenu menu = this.inflatePopupMenu(R.menu.menu_item_default, context, parentView);
		
		// Do not display the "Remove"-entry if the item cannot be removed from the provider
		if((this.result.userInterface.flags & UserInterface.Flags.REMOVABLE) == 0) {
			menu.getMenu().removeItem(R.id.item_remove);
		}
		
		// Do not display the "Add to Favourites"-entry if the item may not be added to
		// that list
		if((this.result.userInterface.flags & UserInterface.Flags.FAVOURABLE) == 0) {
			menu.getMenu().removeItem(R.id.item_favorites_add);
		}
		
		// Add custom menu actions
		for(MenuAction item : this.result.userInterface.menuActions) {
			// Note that we abuse the created menu item's group ID here for storing the action
			// number of the clicked menu item
			menu.getMenu().add(item.action, 0, 0, item.title);
		}
		
		return menu;
	}

    protected PopupMenu inflatePopupMenu(@MenuRes int menuId, Context context, View parentView) {
        PopupMenu menu = new PopupMenu(context, parentView);
        menu.getMenuInflater().inflate(menuId, menu.getMenu());

        // If app already pinned, do not display the "add to favorite" option
        // otherwise don't show the "remove favorite button"
        String favApps = PreferenceManager.getDefaultSharedPreferences(context).
                getString("favorite-apps-list", "");
        if (favApps.contains(this.pojo.id + ";")) {
            menu.getMenu().removeItem(R.id.item_favorites_add);
        } else {
            menu.getMenu().removeItem(R.id.item_favorites_remove);
        }

        return menu;
    }

    /**
     * Handler for popup menu action.
     * Default implementation only handle remove from history action.
     *
     * @return Works in the same way as onOptionsItemSelected, return true if the action has been handled, false otherwise
     */
    Boolean popupMenuClickHandler(Context context, RecordAdapter parent, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_remove:
                removeItem(context, parent);
                return true;
            case R.id.item_favorites_add:
                launchAddToFavorites(context, pojo);
                break;
            case R.id.item_favorites_remove:
                launchRemoveFromFavorites(context, pojo);
                break;
			default:
				try {
					this.result.callbacks.onMenuAction(item.getGroupId());
				} catch(RemoteException e) {
					// Ignore errors if remote provider went away
					Log.w("ResultView", "Could not dispatch result view menu action to provider");
					e.printStackTrace();
				}
				return true;
        }

        //Update Search to reflect favorite add, if the "exclude favorites" option is active
        ((MainActivity) context).updateRecords();

        return false;
    }

    private void launchAddToFavorites(Context context, Pojo app) {
        String msg = context.getResources().getString(R.string.toast_favorites_added);
        KissApplication.getDataHandler(context).addToFavorites(context, app.id);
        Toast.makeText(context, String.format(msg, app.name), Toast.LENGTH_SHORT).show();

        ((MainActivity) context).displayFavorites();
    }

    private void launchRemoveFromFavorites(Context context, Pojo app) {
        String msg = context.getResources().getString(R.string.toast_favorites_removed);
        KissApplication.getDataHandler(context).removeFromFavorites(context, app.id);
        Toast.makeText(context, String.format(msg, app.name), Toast.LENGTH_SHORT).show();

        ((MainActivity) context).displayFavorites();
    }

    /**
     * Remove the current result from the list
     *
     * @param context android context
     * @param parent  adapter on which to remove the item
     */
    private void removeItem(Context context, RecordAdapter parent) {
        Toast.makeText(context, R.string.removed_item, Toast.LENGTH_SHORT).show();
        parent.removeResultView(this);
    }

    public final void launch(Context context, View v) {
        Log.i("log", "Launching " + pojo.id);

        recordLaunch(context);

        // Launch
        doLaunch(context, v);
    }

	/**
	 * How to launch this record ? Most probably, will fire an intent.
	 *
	 * @param context android context
	 * @param v The Android view that has caused this action
	 */
	protected void doLaunch(Context context, View v) {
		try {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				this.result.callbacks.onLaunch(v.getClipBounds());
			} else {
				this.result.callbacks.onLaunch(null);
			}
		} catch(RemoteException e) {
			// Ignore errors if remote provider went away
			Log.w("ResultView", "Could not dispatch result view launch command to provider");
			e.printStackTrace();
		}
	}

    /**
     * How to launch this record "quickly" ? Most probably, same as doLaunch().
     * Override to define another behavior.
     *
     * @param context android context
     */
    public void fastLaunch(Context context, View v) {
        this.launch(context, v);
    }

    /**
     * Return the icon for this Result, or null if non existing.
     *
     * @param context android context
     */
    public Drawable getDrawable(Context context) {
        return null;
    }

    /**
     * Helper function to get a view
     *
     * @param context android context
     * @param id      id to inflate
     * @return the view specified by the id
     */
    View inflate(Context context) {
        LayoutInflater vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return vi.inflate(R.layout.result, null);
    }

    /**
     * Enrich text for display. Put text requiring highlighting between {}
     *
     * @param text to highlight
     * @return text displayable on a textview
     */
    Spanned enrichText(String text, Context context) {
        return Html.fromHtml(text.replaceAll("\\{", "<font color=" + UiTweaks.getPrimaryColor(context) + ">").replaceAll("\\}", "</font>"));
    }

    /**
     * Put this item in application history
     *
     * @param context android context
     */
    void recordLaunch(Context context) {
        // Save in history
        KissApplication.getDataHandler(context).addToHistory(pojo.id);
    }

    public void deleteRecord(Context context) {
        DBHelper.removeFromHistory(context, pojo.id);
    }

    /*
     * Get fill color from theme
     *
     */
    public int getThemeFillColor(Context context) {
        int[] attrs = new int[]{R.attr.resultColor /* index 0 */};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.WHITE);
        ta.recycle();
        return color;
    }
}
