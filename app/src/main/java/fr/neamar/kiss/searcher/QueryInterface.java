package fr.neamar.kiss.searcher;

import android.view.View;

import fr.neamar.kiss.ui.ResultView;

public interface QueryInterface {
	void launchOccurred(ResultView result, View reason);
}
