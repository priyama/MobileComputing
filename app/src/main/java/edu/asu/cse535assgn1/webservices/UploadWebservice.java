package edu.asu.cse535assgn1.webservices;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import edu.asu.cse535assgn1.database.DatabaseManager;

/**
 * Created by Jithin Roy on 2/24/16.
 */
public class UploadWebservice {

    private static String TAG = "UploadWebservice";
    private String uploadFilePath;

    public UploadWebservice(String filePath) {
        this.uploadFilePath = filePath;
    }

    public void startUpload() {
        Log.i(TAG, "Start db upload");
        UploadTask task = new UploadTask();
        task.execute(DatabaseManager.sharedInstance().databasePath());
    }

    public boolean isRunning()  {
        return false;
    }

    private class UploadTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {


            int count = params.length;
            Log.i(TAG, "Length = " + count + "name = " + params[0]);

            HttpsURLConnection connection = null;
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }
            } };

            try {
                SSLContext sc = SSLContext.getInstance("TLS");

                sc.init(null, trustAllCerts, new java.security.SecureRandom());

                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }


            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024;

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            InputStream input = null;
            DataOutputStream output = null;

            try {
                URL url = new URL("https://impact.asu.edu/Appenstance/UploadToServerGPS.php");
                connection = (HttpsURLConnection) url.openConnection();
                connection.setUseCaches(false);
                connection.setDoOutput(true);

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setRequestProperty("Content-Type", "multipart/form-data");



                output = new DataOutputStream(connection.getOutputStream());

                output.writeBytes(twoHyphens + boundary + lineEnd);
                output.writeBytes("Content-Disposition: form-data; name=\"" +
                        "JithinTest" + "\";filename=\"" +
                        "JithinTest" + "\"" + lineEnd);
                output.writeBytes(lineEnd);

                Log.i(TAG, "DB file " + params[0]);
                FileInputStream fileInputStream = new FileInputStream(new File(params[0]));
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                System.out.println("File length " + bytesAvailable + "");

                while (bytesRead > 0) {
                    try {
                        output.write(buffer);
                        Log.i(TAG, "Bytes read!!");
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary + twoHyphens
                        + lineEnd);
                output.flush();
                output.close();

                fileInputStream.close();

                // Responses from the server (code and message)
                int serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();
                System.out.println("Server Response Code " + " " + serverResponseCode);
                System.out.println("Server Response Message "+ serverResponseMessage);

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }


}
