package fr.neamar.kiss.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import java.util.ArrayList;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.searcher.QueryInterface;
import fr.neamar.kiss.ui.ResultView;

public class RecordAdapter extends ArrayAdapter<Result> {
	private final QueryInterface parent;
	
	public RecordAdapter(Context context, QueryInterface parent, int textViewResourceId,
	                     ArrayList<Result> results) {
		super(context, textViewResourceId, results);
		
		this.parent = parent;
	}
	
	@NonNull
	@Override
	public View getView(int position, View view, @NonNull ViewGroup parentView) {
		if(view != null) {
			((ResultView) view).setResult(this.getItem(position));
			return view;
		} else {
			return ResultView.create(this.getContext(), this.parent, this.getItem(position));
		}
	}
	
	public void onLongClick(final ResultView resultView, final View reason) {
		PopupMenu menu = resultView.getPopupMenu(this, resultView);
		
		//check if menu contains elements and if yes show it
		if(menu.getMenu().size() > 0) {
			menu.show();
		}
	}
	
	public void onClick(final ResultView resultView, final View reason) {
		try {
			resultView.launch(resultView);
		} catch(ArrayIndexOutOfBoundsException ignored) {
			return;
		}
		
		// Record the launch after some period,
		// * to ensure the animation runs smoothly
		// * to avoid a flickering -- launchOccurred will refresh the list
		// Thus TOUCH_DELAY * 3
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				parent.launchOccurred(resultView, reason);
			}
		}, KissApplication.TOUCH_DELAY * 3);
		
	}
	
	public void removeResultView(ResultView resultView) {
		this.remove(resultView.getResult());
		resultView.deleteRecord();
		notifyDataSetChanged();
	}
}
