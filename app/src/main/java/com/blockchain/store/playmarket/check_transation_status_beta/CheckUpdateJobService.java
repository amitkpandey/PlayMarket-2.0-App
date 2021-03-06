package com.blockchain.store.playmarket.check_transation_status_beta;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.blockchain.store.playmarket.BuildConfig;
import com.blockchain.store.playmarket.api.RestApi;
import com.blockchain.store.playmarket.data.entities.App;
import com.blockchain.store.playmarket.data.entities.UpdateNotification;
import com.blockchain.store.playmarket.notification.NotificationManager;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.blockchain.store.playmarket.utilities.Constants.DOWNLOAD_NEW_VERSION_WITHOUT_PROMPT;

/**
 * Created by Igor.Sakovich on 17.10.2018.
 */

public class CheckUpdateJobService extends JobService {
    private static final String TAG = "CheckUpdateJobService";

    @Override
    public boolean onStartJob(JobParameters params) {
        RestApi.getServerApi().getAppsByPackage(getBaseContext().getPackageName())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> onGetAppsReady(result, params),
                        throwable -> onGetAppsFailed(throwable, params));
        return true;
    }

    private void onGetAppsReady(ArrayList<App> result, JobParameters params) {
        Log.d(TAG, "onGetAppsReady() called with: result = [" + result + "], params = [" + params + "]");
        if (!result.isEmpty()) {
            App app = result.get(0);
            Log.d(TAG, "onGetAppsReady: server's app version " + app.version);
            if (Integer.parseInt(app.version) > BuildConfig.VERSION_CODE) {
                onNewVersionAvailable(app);
            }

        }
        jobFinished(params, false);
    }

    private void onGetAppsFailed(Throwable throwable, JobParameters params) {
        Log.d(TAG, "onGetAppsFailed() called with: throwable = [" + throwable + "], params = [" + params + "]");
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }

    private void onNewVersionAvailable(App app) {
        Log.d(TAG, "onNewVersionAvailable() called with: app = [" + app + "]");
        UpdateNotification updateNotification = new UpdateNotification(app);
        NotificationManager.getManager().registerNewNotification(updateNotification);
        if (Hawk.get(DOWNLOAD_NEW_VERSION_WITHOUT_PROMPT, false)) {

        } else {

        }

    }
}
