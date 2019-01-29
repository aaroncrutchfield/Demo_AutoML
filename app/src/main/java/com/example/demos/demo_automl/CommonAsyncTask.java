package com.example.demos.demo_automl;

import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;


import android.os.AsyncTask;
import android.view.View;


import java.io.IOException;

public abstract class CommonAsyncTask extends AsyncTask<Void, Void, Boolean> {
    final MainActivity activity;
    final com.google.api.services.tasks.Tasks client;


    CommonAsyncTask(MainActivity activity) {
        this.activity = activity;
        client = activity.service;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.numAsyncTasks++;
    }


    @Override
    protected final Boolean doInBackground(Void... ignored) {
        try {
            doInBackground();
            return true;
        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            activity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());
        } catch (UserRecoverableAuthIOException userRecoverableException) {
            activity.startActivityForResult(
                    userRecoverableException.getIntent(), MainActivity.REQUEST_AUTHORIZATION);
        } catch (IOException e) {
            Utils.logAndShow(activity, MainActivity.TAG, e);
        }
        return false;
    }


    @Override
    protected final void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (0 == --activity.numAsyncTasks) {
            progressBar.setVisibility(View.GONE);
        }
        if (success) {
            activity.refreshView();
        }
    }


    abstract protected void doInBackground() throws IOException;
}
