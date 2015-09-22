package org.matrix.androidsdk;

import android.net.Uri;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.matrix.androidsdk.rest.model.login.Credentials;

import java.util.ArrayList;


/**
 * Represents how to connect to a specific Homeserver, may include credentials to use.
 */
public class HomeserverConnectionConfig {
    private Uri mHsUri;
    private byte[][] mAllowedFingerprints;
    private Credentials mCredentials;
    private boolean mPin;

    /**
     * @param hsUri The URI to use to connect to the homeserver
     */
    public HomeserverConnectionConfig(Uri hsUri) {
        this(hsUri, null, null, false);
    }

    /**
     * @param hsUri The URI to use to connect to the homeserver
     * @param credentials The credentials to use, if needed. Can be null.
     */
    public HomeserverConnectionConfig(Uri hsUri,Credentials credentials) {
        this(hsUri, credentials, null, false);
    }

    /**
     * @param hsUri The URI to use to connect to the homeserver
     * @param credentials The credentials to use, if needed. Can be null.
     * @param allowedFingerprints If using SSL, allow server certs that match these fingerprints.
     * @param pin If true only allow certs matching given fingerprints, otherwise fallback to
     *            standard X509 checks.
     */
    public HomeserverConnectionConfig(Uri hsUri, Credentials credentials, byte[][] allowedFingerprints, boolean pin) {
        if (hsUri == null || (!"http".equals(hsUri.getScheme()) && !"https".equals(hsUri.getScheme())) ) {
            throw new RuntimeException("Invalid home server URI: "+hsUri);
        }

        this.mHsUri = hsUri;
        this.mAllowedFingerprints = allowedFingerprints;
        this.mPin = pin;
        this.mCredentials = credentials;
    }

    public Uri getHomeserverUri() { return mHsUri; }
    public byte[][] getAllowedFingerprints() { return mAllowedFingerprints; }

    public Credentials getCredentials() { return mCredentials; }
    public void setCredentials(Credentials credentials) { this.mCredentials = credentials; }

    public boolean getPinned() {
        return mPin;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("home_server_url", mHsUri.toString());
        json.put("pin", mPin);
        if (mCredentials != null) json.put("credentials", mCredentials.toJson());
        if (mAllowedFingerprints != null) {
            ArrayList<String> fingerprints = new ArrayList<String>(mAllowedFingerprints.length);

            for (byte[] fingerprint : mAllowedFingerprints) {
                fingerprints.add(Base64.encodeToString(fingerprint, Base64.DEFAULT));
            }

            json.put("fingerprints", fingerprints);
        }

        return json;
    }

    public static HomeserverConnectionConfig fromJson(JSONObject obj) throws JSONException {
        JSONArray fingerprintArray = obj.optJSONArray("fingerprints");
        byte[][] fingerprints = null;
        if (fingerprintArray != null) {
            fingerprints = new byte[fingerprintArray.length()][];

            for (int i = 0; i < fingerprintArray.length(); i++) {
                fingerprints[i] = Base64.decode(fingerprintArray.getString(i), Base64.DEFAULT);
            }
        }

        JSONObject credentialsObj = obj.optJSONObject("credentials");
        Credentials creds = credentialsObj != null ? Credentials.fromJson(credentialsObj) : null;

        return new HomeserverConnectionConfig(
                Uri.parse(obj.getString("home_server_url")),
                creds,
                fingerprints,
                obj.optBoolean("pin", false)
        );
    }
}