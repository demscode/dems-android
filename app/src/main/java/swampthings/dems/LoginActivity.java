package swampthings.dems;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;


/**
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work with your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class LoginActivity extends Activity implements
        ConnectionCallbacks, OnConnectionFailedListener, View.OnClickListener {

    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 0;
    private boolean intentInProgress;
    private boolean signInClicked;
    private ConnectionResult connectionResult;
    protected String patientAPIURL = "http://demsweb.herokuapp.com/api/patient/";


    private void resolveSignInErrors() {
        if (connectionResult.hasResolution()) {
            try {
                intentInProgress = true;
                startIntentSenderForResult(connectionResult.getResolution().getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult
                intentInProgress = false;
                googleApiClient.connect();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // We've resolved any connection errors.  googleApiClient can be used to
        // access Google APIs on behalf of the user.
        signInClicked = false;
        Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
        String email = Plus.AccountApi.getAccountName(googleApiClient);

        String id = currentPerson.getId();
        String name = currentPerson.getDisplayName();
        JSONObject profile = new JSONObject();

        try {

            profile.put("id", id);
            profile.put("name", name);
            profile.put("email", email);

        } catch (Exception e) {
            //error
        }

        new CheckAccountExists().execute(profile);

    }


    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!intentInProgress) {
            // Store the ConnectionResult so that we can use it later when the user clicks
            // 'sign-in'.
            connectionResult = result;

            if (signInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInErrors();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if(responseCode != RESULT_OK) {
                signInClicked = false;
            }
            intentInProgress = false;
        }

        if (!googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button && !googleApiClient.isConnecting()) {
            signInClicked = true;
            resolveSignInErrors();
        }
    }

    protected class CheckAccountExists extends AsyncTask<JSONObject, Integer, String> {

        @Override
        protected String doInBackground(JSONObject... params) {
            String id = "";
            try {
                id = params[0].getString("id");
            } catch (Exception e) {
                //id not defined
            }

            if (CheckPatientExists(id)) {
                return id;
            } else {
                CreateNewAccount(params[0]);
            }

            return id;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);

        }


        private boolean CheckPatientExists(String id) {
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
            HttpGet request = new HttpGet(patientAPIURL + id);
            HttpResponse response;
            boolean exists = false;

            try {
                response = httpClient.execute(request);


                if (response.getStatusLine().getStatusCode() == 200) {
                    exists = true;
                }


            } catch (Exception e) {
                exists = false;
            } finally {
                httpClient.close();
            }

            return exists;
        }

        private boolean CreateNewAccount(JSONObject profile) {
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
            HttpPost request = new HttpPost(patientAPIURL);
            HttpResponse response;
            boolean success = false;

            try {

                StringEntity stringEntity = new StringEntity(profile.toString());

                request.setEntity(stringEntity);
                request.setHeader("Accept", "application/json");
                request.setHeader("Content-type", "application/json");

                response = httpClient.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    success = true;
                }



            } catch (Exception e) {
                success =  false;
            } finally {
                httpClient.close();
            }

            return success;
        }
    }
}



