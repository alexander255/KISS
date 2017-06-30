package fr.neamar.kiss.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.andreinc.aleph.AlephFormatter;
import static net.andreinc.aleph.AlephFormatter.template;

import java.lang.reflect.Method;
import java.util.Map;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.R;
import fr.neamar.kiss.UiTweaks;
import fr.neamar.kiss.adapter.RecordAdapter;
import fr.neamar.kiss.api.provider.ButtonAction;
import fr.neamar.kiss.api.provider.IResultController;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.api.provider.ResultControllerConnection;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.db.DBHelper;
import fr.neamar.kiss.searcher.QueryInterface;


public class ResultView extends RelativeLayout {
	/**
	 * Result item that is currently being rendered
	 */
	private Result result = null;
	
	/**
	 * Callback interface for the remote provider to influence rendering of the current result item
	 */
	private ResultControllerConnection controller = null;
	
	private final static class ResultController extends IResultController.Stub {
		/// Result view that may be controlled by this result controller
		/// (if `null` the controller will do nothing)
		private ResultView resultView;
		
		/// Handler used for processing requests on the main thread
		/// (they may be dispatched from any thread)
		private final Handler handler;
		
		ResultController(ResultView resultView) {
			this.resultView = resultView;
			this.handler    = new Handler(resultView.getContext().getMainLooper());
		}
		
		void detachFromResultView() {
			this.resultView = null;
		}
		
		@Override
		public void setIcon(final Bitmap icon, final boolean tintIcon) {
			this.handler.post(new Runnable() {
				@Override
				public void run() {
					if(resultView != null) {
						resultView.setImageContents(R.id.result_icon, icon, tintIcon);
					}
				}
			});
		}
		
		@Override
		public void setSubicon(final Bitmap icon, final boolean tintIcon) {
			this.handler.post(new Runnable() {
				@Override
				public void run() {
					if(resultView != null) {
						resultView.setImageContents(R.id.result_subicon, icon, tintIcon);
					}
				}
			});
		}
	}
	
	
	
	/**
	 * Standard Android View constructor – use `create` instead
	 * @internal
	 */
	public ResultView(Context context) {
		super(context);
	}
	
	/**
	 * Standard Android View constructor – use `create` instead
	 * @internal
	 */
	public ResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * Standard Android View constructor – use `create` instead
	 * @internal
	 */
	public ResultView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	
	/**
	 * Create new `ResultView` using its XML layout
	 *
	 * @param context UI Context that will host this View
	 * @param parent
	 * @param result Result instance to initially render on this View
	 */
	public static ResultView create(Context context, QueryInterface parent, @Nullable Result result) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ResultView resultView = (ResultView) inflater.inflate(R.layout.result, null);
		resultView.setResult(result);
		return resultView;
	}
	
	
	@Override
	protected void onDetachedFromWindow() {
		this.setResult(null);
		
		super.onDetachedFromWindow();
	}
	
	
	/**
	 * @return The result object that this view will attempt to render
	 */
	public Result getResult() {
		return this.result;
	}
	
	/**
	 * Change the result object wrapped by this view
	 *
	 * This will also terminate any `IResultController` connections that may be currently active
	 * on this view.
	 *
	 * @param result The new result that should be rendered by this view
	 */
	public void setResult(@Nullable Result result) {
		// Do nothing if the result actually stays the same
		if(result == this.result) {
			return;
		}
		
		
		// Disarm controller instance and notify the connected provider that their result is
		// disappearing
		if(this.controller != null) {
			((ResultController) this.controller.controller).detachFromResultView();
			try {
				this.result.callbacks.onDestroy(this.controller);
			} catch(RemoteException e) {
				Log.w("ResultView", "Could not dispatch result view hide event to provider");
				e.printStackTrace();
			}
		}
		
		// Set new state
		this.controller = null;
		this.result     = result;
		
		if(this.result != null) {
			// Build view tree for new result
			this.displayText();
			this.displayButtons();
			this.displayStaticIcon();
			
			// Notify the provider of the new result that it is now going to be shown
			this.controller = new ResultControllerConnection(new ResultController(this));
			try {
				this.result.callbacks.onCreate(this.controller);
			} catch(RemoteException e) {
				Log.w("ResultView", "Could not dispatch result view show event to provider");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Render the text of this result based on its UI description
	 */
	protected void displayText() {
		// Put text rendering templates into templating engine
		AlephFormatter text    = template(this.result.userInterface.textTemplate);
		AlephFormatter subtext = template(this.result.userInterface.subtextTemplate);
		
		// Apply template substitutions
		for(Map.Entry<String, String> entry : this.result.templateParameters.entrySet()) {
			text    = text.arg(entry.getKey(), escapeHtml(entry.getValue()));
			subtext = subtext.arg(entry.getKey(), escapeHtml(entry.getValue()));
		}
		
		// *Enrich* the final text and display it
		TextView textView = (TextView) this.findViewById(R.id.result_text);
		textView.setText(enrichText(text.fmt()));
		
		TextView subtextView = (TextView) this.findViewById(R.id.result_subtext);
		subtextView.setText(enrichText(subtext.fmt()));
		
		// Hide subtext view if it is empty
		subtextView.setVisibility(subtextView.getText().length() > 0 ? View.VISIBLE : View.GONE);
	}
	
	protected void displayButtons() {
		// Clear previous contents of button container (in case it was recycled)
		LinearLayout buttonContainer = (LinearLayout) this.findViewById(R.id.result_extras);
		buttonContainer.removeAllViews();
		
		if(this.result.userInterface.buttonActions.length < 1) {
			return;
		}
		
		// Obtain primary color value
		@ColorInt
		int primaryColor = Color.parseColor(UiTweaks.getPrimaryColor(this.getContext()));
		
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
		this.getContext().getTheme().resolveAttribute(R.attr.appSelectableItemBackground, background, true);
		
		for(final ButtonAction buttonAction : this.result.userInterface.buttonActions) {
			switch(buttonAction.type) {
				case IMAGE_BUTTON:
					ImageButton imageButton = new ImageButton(this.getContext());
					imageButton.setImageBitmap(buttonAction.icon);
					imageButton.setColorFilter(primaryColor);
					imageButton.setBackgroundResource(background.resourceId);
					buttonContainer.addView(imageButton);
					
					imageButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								ResultView.this.result.callbacks.onButtonAction(controller, buttonAction.action, 0);
							} catch(RemoteException e) {
								// Ignore errors if remote provider went away
								Log.w("ResultView", "Could not dispatch result view button action to provider");
								e.printStackTrace();
							}
						}
					});
					break;
				
				case TOGGLE_BUTTON:
					Switch toggleButton = new Switch(this.getContext());
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
								ResultView.this.result.callbacks.onButtonAction(controller, buttonAction.action, isChecked ? 1 : 0);
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
	 * Display the icon statically associated with the user-interface of this result (if any)
	 */
	protected void displayStaticIcon() {
		this.setImageContents(R.id.result_icon,
				this.result.userInterface.staticIcon,
				(this.result.userInterface.flags & UserInterface.Flags.TINT_ICON) != 0
		);
		
		((ImageView) this.findViewById(R.id.result_subicon)).setImageBitmap(null);
	}
	
	
	private void setImageContents(@IdRes int id, @Nullable Bitmap bitmap, boolean tint) {
		ImageView imageView = (ImageView) this.findViewById(id);
		imageView.setImageBitmap(bitmap);
		
		if(tint) {
			imageView.setColorFilter(this.getThemeFillColor(), PorterDuff.Mode.SRC_IN);
		} else {
			imageView.clearColorFilter();
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
     * How to display the popup menu
     *
     * @return a PopupMenu object
     */
    public PopupMenu getPopupMenu(final RecordAdapter parent, View parentView) {
        PopupMenu menu = buildPopupMenu(parent, parentView);

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                return popupMenuClickHandler(parent, item);
            }
        });

        return menu;
    }

	/**
	 * Default popup menu implementation, can be overridden by children class to display a more specific menu
	 *
	 * @return an inflated, listener-free PopupMenu
	 */
	PopupMenu buildPopupMenu(final RecordAdapter parent, View parentView) {
		PopupMenu menu = this.inflatePopupMenu(R.menu.menu_item_default, parentView);
		
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

    protected PopupMenu inflatePopupMenu(@MenuRes int menuId, View parentView) {
        PopupMenu menu = new PopupMenu(this.getContext(), parentView);
        menu.getMenuInflater().inflate(menuId, menu.getMenu());

        // If app already pinned, do not display the "add to favorite" option
        // otherwise don't show the "remove favorite button"
        String favApps = PreferenceManager.getDefaultSharedPreferences(this.getContext()).
                getString("favorite-apps-list", "");
        if (favApps.contains(this.result.id + ";")) {
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
	Boolean popupMenuClickHandler(RecordAdapter parent, MenuItem item) {
		switch(item.getItemId()) {
			case R.id.item_remove:
				removeItem(parent);
				return true;
			case R.id.item_favorites_add:
				launchAddToFavorites();
				break;
			case R.id.item_favorites_remove:
				launchRemoveFromFavorites();
				break;
			default:
				try {
					this.result.callbacks.onMenuAction(this.controller, item.getGroupId());
				} catch(RemoteException e) {
					// Ignore errors if remote provider went away
					Log.w("ResultView", "Could not dispatch result view menu action to provider");
					e.printStackTrace();
				}
				return true;
		}
		
		//Update Search to reflect favorite add, if the "exclude favorites" option is active
		((MainActivity) this.getContext()).updateRecords();
		
		return false;
	}

    private void launchAddToFavorites() {
        String msg = this.getContext().getResources().getString(R.string.toast_favorites_added);
        KissApplication.getDataHandler(this.getContext()).addToFavorites(this.getContext(), this.result.id);
        Toast.makeText(this.getContext(), String.format(msg, this.result.name), Toast.LENGTH_SHORT).show();

        ((MainActivity) this.getContext()).displayFavorites();
    }

    private void launchRemoveFromFavorites() {
        String msg = this.getContext().getResources().getString(R.string.toast_favorites_removed);
        KissApplication.getDataHandler(this.getContext()).removeFromFavorites(this.getContext(), this.result.id);
        Toast.makeText(this.getContext(), String.format(msg, this.result.name), Toast.LENGTH_SHORT).show();

        ((MainActivity) this.getContext()).displayFavorites();
    }

    /**
     * Remove the current result from the list
     *
     * @param parent  adapter on which to remove the item
     */
    private void removeItem(RecordAdapter parent) {
        Toast.makeText(this.getContext(), R.string.removed_item, Toast.LENGTH_SHORT).show();
        parent.removeResultView(this);
    }

    public final void launch(View v) {
        Log.i("log", "Launching " + this.result.id);

        recordLaunch();

        // Launch
        doLaunch(v);
    }

	/**
	 * How to launch this record ? Most probably, will fire an intent.
	 *
	 * @param v The Android view that has caused this action
	 */
	protected void doLaunch(View v) {
		try {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				this.result.callbacks.onLaunch(this.controller, v.getClipBounds());
			} else {
				this.result.callbacks.onLaunch(this.controller, null);
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
     * @param v The view that has been interacted with to cause this event
     */
    public void fastLaunch(View v) {
        this.launch(v);
    }
	
	/**
     * Enrich text for display. Put text requiring highlighting between {}
     *
     * @param text to highlight
     * @return text displayable on a textview
     */
    Spanned enrichText(String text) {
        return Html.fromHtml(text.replaceAll("\\{", "<font color=" + UiTweaks.getPrimaryColor(this.getContext()) + ">").replaceAll("\\}", "</font>"));
    }

    /**
     * Put this item in application history
     */
    void recordLaunch() {
        // Save in history
        KissApplication.getDataHandler(this.getContext()).addToHistory(this.result.id);
    }

    public void deleteRecord() {
        DBHelper.removeFromHistory(this.getContext(), this.result.id);
    }

    /*
     * Get fill color from theme
     *
     */
    public int getThemeFillColor() {
        int[] attrs = new int[]{R.attr.resultColor /* index 0 */};
        TypedArray ta = this.getContext().obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.WHITE);
        ta.recycle();
        return color;
    }
}
