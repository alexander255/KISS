package fr.neamar.kiss.api.provider;

import android.graphics.Rect;

import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.api.provider.ResultControllerConnection;

interface IResultCallbacks {
	oneway void onMenuAction(in ResultControllerConnection controller, int action);
	oneway void onButtonAction(in ResultControllerConnection controller, int action, int newState);
	oneway void onLaunch(in ResultControllerConnection controller, in Rect sourceBounds);
	oneway void onCreate(in ResultControllerConnection controller);
	oneway void onDestroy(in ResultControllerConnection controller);
}
