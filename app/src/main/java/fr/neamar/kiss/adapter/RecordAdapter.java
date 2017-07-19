package fr.neamar.kiss.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.searcher.QueryInterface;
import fr.neamar.kiss.ui.ResultStateManager;
import fr.neamar.kiss.ui.ResultView;

public class RecordAdapter extends ArrayAdapter<Result> {
	private final QueryInterface parent;
	private ConcurrentHashMap<String, ResultStateManager> preloadedResults = new ConcurrentHashMap<>();
	
	public RecordAdapter(Context context, QueryInterface parent, int textViewResourceId,
	                     ArrayList<Result> results) {
		super(context, textViewResourceId, results);
		
		this.parent = parent;
	}
	
	public void preloadResults(Result[] results, int timeoutMilliseconds) {
		// Sanity check – we will block the current thread, so no handlers would be able to run there
		if(Looper.myLooper() == Looper.getMainLooper()) {
			throw new AssertionError("RecordAdapter.preloadResults may not be called from the main thread");
		}
		
		// Remove previous set of preloaded views
		this.preloadedResults.clear();
		
		final Lock        lock             = new ReentrantLock();
		final Condition   resultViewsReady = lock.newCondition();
		final Set<String> pendingResultIds = Collections.newSetFromMap(
				new ConcurrentHashMap<String, Boolean>()
		);
		
		// Create result view for each result
		for(final Result result : results) {
			ResultStateManager resultController = new ResultStateManager(result, false);
			pendingResultIds.add(result.id);
			
			resultController.addCallbacks(new ResultStateManager.IReadyCallbacks() {
				@Override
				public void onResultReady(ResultStateManager stateManager) {
					lock.lock();
					try {
						// Result view is ready – therefor not pending anymore
						if(!pendingResultIds.remove(stateManager.getResult().id)) {
							// Timeout has already expired and object removed
							return;
						}
						
						// Declare result view ready for consumption
						preloadedResults.put(stateManager.getResult().id, stateManager);
						
						// Fulfill condition once all result views have been preloaded
						if(pendingResultIds.isEmpty()) {
							resultViewsReady.signalAll();
						}
					} finally {
						lock.unlock();
					}
				}
				
				@Override
				public void onResultCancelled(ResultStateManager stateManager) {
					this.onResultReady(stateManager);
				}
			});
		}
		
		// Suspend current thread until our callback tells us there are no pending result views
		// or the timeout expired
		lock.lock();
		try {
			resultViewsReady.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);
		} catch(InterruptedException ignored) {
		} finally {
			// Log notice for any result views that didn't make it in time
			for(String resultId : pendingResultIds) {
				Log.w("RecordAdapter",
				      "ResultView did not become ready after " + timeoutMilliseconds + "ms: " + resultId);
			}
			pendingResultIds.clear();
			
			lock.unlock();
		}
	}
	
	@NonNull
	@Override
	public View getView(int position, View view, @NonNull ViewGroup parentView) {
		//noinspection ConstantConditions: `this.getItem(position)` may never be `null` in this context
		ResultStateManager stateManager = this.preloadedResults.get(this.getItem(position).id);
		if(stateManager != null) {
			stateManager.setDestroyOnDetach(true);
			
			if(view != null) {
				((ResultView) view).setStateManager(stateManager);
			} else {
				view = ResultView.create(this.getContext(), this.parent, stateManager);
			}
			
			return view;
		}
		
		if(view != null) {
			((ResultView) view).setStateManager(new ResultStateManager(this.getItem(position)));
			return view;
		} else {
			return ResultView.create(this.getContext(), this.parent, new ResultStateManager(this.getItem(position)));
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
