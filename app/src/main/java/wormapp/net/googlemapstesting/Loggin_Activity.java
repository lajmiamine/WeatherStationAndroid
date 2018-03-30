package wormapp.net.googlemapstesting;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;


public class Loggin_Activity extends ActionBarActivity {

    EditText login, passwd;
    public Button btn;

    InputStream is = null;
    JSONObject jObj = null;
    String IdProp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        login=(EditText) findViewById(R.id.login);
        passwd=(EditText) findViewById(R.id.passwd);

        btn=(Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new task().execute();
            }
        });
    }

    class task extends AsyncTask<String, String, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(Loggin_Activity.this);
        protected void onPreExecute() {
            progressDialog.setMessage("Fetching");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    task.this.cancel(true);
                }
            });
        }

        protected Void doInBackground(String... params){

            Thread t = new Thread() {

                public void run() {
                    Looper.prepare(); //For Preparing Message Pool for the child Thread
                    HttpClient client = new DefaultHttpClient();
                    HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
                    HttpResponse response;
                    JSONObject json = new JSONObject();

                    try {
                        HttpPost post = new HttpPost("http://41.227.194.224:81/Service1.svc/GetIdProp");
                        json.put("login",login.getText().toString());
                        json.put("passwd",passwd.getText().toString());
                        StringEntity se = new StringEntity( json.toString());
                        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                        post.setEntity(se);
                        response = client.execute(post);

                    /*Checking response */
                        if(response!=null){
                            InputStream in = response.getEntity().getContent(); //Get the data in the entity
                            String is = convertStreamToString(in);
                            progressDialog.dismiss();
                            Log.d("result", is);


                            try {
                                jObj = new JSONObject(is);
                            } catch (JSONException e) {
                                Log.e("JSON Parser", "Error parsing data " + e.toString());
                            }

                            IdProp = jObj.getString("GetIdPropResult");


                            Intent intent = new Intent(Loggin_Activity.this, Projects_Activity.class);
                            Bundle b = new Bundle();
                            b.putString("IdProp", IdProp);
                            intent.putExtras(b);
                            startActivity(intent);
                            finish();
                        }

                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    Looper.loop(); //Loop in the message queue
                }
            };

            t.start();
            return null;
        }

        protected void onPostExecute(Void v) {
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
