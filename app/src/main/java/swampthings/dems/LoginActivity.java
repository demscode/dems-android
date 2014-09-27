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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


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

    /* Overrided method which runs on activity creation
     * Creates view and sets up up Google+ API
     */
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

    /* Overrided method which runs on activity startup
     * Begins connection to Google+ and adds event listener
     */
    @Override
    protected void onStart() {
        super.onStart();
        //googleApiClient.connect();
        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }

    /* Overrided method which runs on activity termination
     * Disconnects Google+ API on activity end
     */
    @Override
    protected void onStop() {
        super.onStop();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    /* Method which runs on successful connection/login to Google+ API
     * Collects Google Account info and calls RESTful API calls
     */
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
            //add fields to JSONObject
            profile.put("gid", id);
            profile.put("name", name);
            profile.put("email", email);

        } catch (Exception e) {
            //error
        }

        // Execute RESTful calls as a background task (another thread)
        new CheckAccountExists().execute(profile);

    }


    /* Reconnect if connection is suspended
     */
    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    /* Try to recover from failed connection to Google+
     */
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

    /* Process result of connection attempt.
     */
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
        if (v.getId() == R.id.sign_in_button) {
        googleApiClient.connect();
            if(!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                signInClicked = true;
                resolveSignInErrors();
            }
        }
    }


    /* RESTful API calls background task
     * Runs in a separate thread than main activity
     */
    protected class CheckAccountExists extends AsyncTask<JSONObject, Integer, String> {

        // Executes doInBackground task first
        @Override
        protected String doInBackground(JSONObject... params) {
            String id = "";
            try {
                id = params[0].getString("gid");
            } catch (Exception e) {
                //id not defined
            }


            String patientID = CheckPatientExists(id);
            if (patientID != null) {
                return patientID;
            } else {
                patientID = CreateNewAccount(params[0]);
            }

            return patientID;
        }

        // Tasks result of doInBackground and executes after completion of task
        @Override
        protected void onPostExecute(String id) {
            super.onPostExecute(id);

            // Move onto the main activity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
			
            // Parse id of current user to main activity before starting
            intent.putExtra("id", id);
            startActivity(intent);
            LoginActivity.this.finish();

        }


        // Checks whether a patient is already registered in the app database
        // returns true if registered, false otherwise
        private String CheckPatientExists(String id) {
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
            HttpGet request = new HttpGet(patientAPIURL + "google/" + id);
            HttpResponse response;
            boolean exists = false;
            String objectId = null;
            StringBuilder builder = new StringBuilder();

            try {
                response = httpClient.execute(request);


                if (response.getStatusLine().getStatusCode() == 200) {
                    exists = true;

                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    String jsonString = builder.toString();

                    JSONObject json = new JSONObject(jsonString);
                    objectId = json.getString("id");
                }

            } catch (Exception e) {
                exists = false;
            } finally {
                httpClient.close();
            }

            if (exists) {
                return objectId;
            } else {
                return null;
            }
        }

        // Registers a patient into the app database by passing their Google account data
        // through RESTful API POST request
        private String CreateNewAccount(JSONObject profile) {
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
            HttpPost request = new HttpPost(patientAPIURL);
            HttpResponse response;
            boolean success = false;
            String objectId = null;
            StringBuilder builder = new StringBuilder();

            try {

                StringEntity stringEntity = new StringEntity(profile.toString());

                request.setEntity(stringEntity);
                request.setHeader("Accept", "application/json");
                request.setHeader("Content-type", "application/json");

                response = httpClient.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    success = true;

                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    String jsonString = builder.toString();

                    JSONObject json = new JSONObject(jsonString);
                    objectId = json.getString("id");
                }



            } catch (Exception e) {
                success =  false;
            } finally {
                httpClient.close();
            }

            if (success) {
                return objectId;
            } else {
                return null;
            }
        }
    }
}



