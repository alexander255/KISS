package fr.neamar.kiss.dataprovider;

import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.regex.Pattern;

import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.loader.LoadPhonePojos;
import fr.neamar.kiss.pojo.PhonePojo;
import fr.neamar.kiss.pojo.Pojo;

public class PhoneProvider extends Provider<PhonePojo> {
    public static final String PHONE_SCHEME = "phone://";
    private boolean deviceIsPhone = false;

    @Override
    public void reload() {
        this.initialize(new LoadPhonePojos(this));

        PackageManager pm = this.getPackageManager();
        deviceIsPhone = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public ArrayList<Result> getResults(String query) {
        ArrayList<Result> results = new ArrayList<>();

        // Append an item only if query looks like a phone number and device has phone capabilities
        if (deviceIsPhone && query.matches("^([0-9+ .-]{2,}|[*#]{1,3}[0-9]{1,3}[*a-zA-Z0-9]*#)$")) {
            results.add(getResult(query));
        }

        return results;
    }


    public Result findById(String id) {
        return getResult(id.replaceFirst(Pattern.quote(PHONE_SCHEME), ""));
    }

    private Result getResult(String phoneNumber) {
        PhonePojo pojo = new PhonePojo();
        pojo.id = PHONE_SCHEME + phoneNumber;
        pojo.phone = phoneNumber;
        pojo.relevance = 20;
        pojo.name = phoneNumber;
        return new Result(pojo);
    }
}
