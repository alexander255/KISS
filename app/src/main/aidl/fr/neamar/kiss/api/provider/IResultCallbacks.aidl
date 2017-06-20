package fr.neamar.kiss.api.provider;

import android.graphics.Rect;

import fr.neamar.kiss.api.provider.Result;

interface IResultCallbacks {
	oneway void onMenuAction(int action);
	oneway void onLaunch(in Rect sourceBounds);
}
