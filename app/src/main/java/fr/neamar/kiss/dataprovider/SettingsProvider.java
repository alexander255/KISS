package fr.neamar.kiss.dataprovider;

import java.util.ArrayList;
import java.util.regex.Pattern;

import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.dataprovider.setting.DataItem;
import fr.neamar.kiss.dataprovider.setting.UIEndpoint;
import fr.neamar.kiss.loader.LoadSettingsPojos;
import fr.neamar.kiss.pojo.SettingsPojo;

public class SettingsProvider extends Provider<SettingsPojo> {
    private UIEndpoint uiEndpoint;
    private String settingName;
    
	@Override
	public void onCreate() {
		this.uiEndpoint = new UIEndpoint(this);
		
		super.onCreate();
	}

    @Override
    public void reload() {
        this.initialize(new LoadSettingsPojos(this));

        settingName = this.getString(R.string.settings_prefix).toLowerCase();
    }

    public ArrayList<Result> getResults(String query) {
        ArrayList<Result> results = new ArrayList<>();

        int relevance;
        String settingNameLowerCased;
        for (SettingsPojo setting : pojos) {
            relevance = 0;
            settingNameLowerCased = setting.nameNormalized;
            if (settingNameLowerCased.startsWith(query))
                relevance = 10;
            else if (settingNameLowerCased.contains(" " + query))
                relevance = 5;
            else if (settingName.startsWith(query)) {
                // Also display for a search on "settings" for instance
                relevance = 4;
            }

            if (relevance > 0) {
                setting.displayName = setting.name.replaceFirst(
                        "(?i)(" + Pattern.quote(query) + ")", "{$1}");
                setting.relevance = relevance;
                results.add(new DataItem(this.uiEndpoint, setting));
            }
        }

        return results;
    }

    public Result findById(String id) {
        for (SettingsPojo pojo : pojos) {
            if (pojo.id.equals(id)) {
                pojo.displayName = pojo.name;
                return new DataItem(this.uiEndpoint, pojo);
            }
        }

        return null;
    }
}
