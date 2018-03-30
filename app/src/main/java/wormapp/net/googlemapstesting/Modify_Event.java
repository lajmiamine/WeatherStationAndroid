package wormapp.net.googlemapstesting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;


public class Modify_Event extends ActionBarActivity {

    private EditText dateDebutHH;
    private EditText dateDebutMM;
    private EditText duree;
    private Button saveBtn;
    private String param;
    private String param2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent myIntent = getIntent();
        param = myIntent.getStringExtra("IdProject");
        param2 = myIntent.getStringExtra("IdCalendrier");

        setContentView(R.layout.activity_modify__event);

        dateDebutHH = (EditText) findViewById(R.id.hh);
        dateDebutMM = (EditText) findViewById(R.id.mm);
        duree = (EditText) findViewById(R.id.duree);
        saveBtn = (Button) findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hour = dateDebutHH.getText().toString();
                String minutes = dateDebutMM.getText().toString();
                final String dre = duree.getText().toString();
                //toDate(hour, minutes);

                try {
                    new AsyncTask<String, String, Void>() {

                        private ProgressDialog progressDialog = new ProgressDialog(Modify_Event.this);

                        protected void onPreExecute() {
                            progressDialog.setMessage("Fetching");
                            progressDialog.show();
                        }

                        protected Void doInBackground(String... params) {

                            Thread t = new Thread() {

                                public void run() {
                                    Looper.prepare(); //For Preparing Message Pool for the child Thread
                                    HttpClient client = new DefaultHttpClient();
                                    HttpConnectionParams.setConnectionTimeout(client.getParams(), 10001); //Timeout Limit
                                    HttpResponse response;
                                    JSONObject json = new JSONObject();

                                    try {
                                        HttpPost post = new HttpPost("http://41.227.194.224:81/Service1.svc/EventModifier");
                                        json.put("IdCalendrier", param2);
                                        json.put("Duree", dre);
                                        StringEntity se = new StringEntity(json.toString());
                                        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                                        post.setEntity(se);
                                        response = client.execute(post);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    Looper.loop();
                                }
                            };

                            t.start();

                            return null;
                        }

                        protected void onPostExecute(Void v) {
                        }

                    }.execute();
                    Toast.makeText(getApplicationContext(), "Event modifier", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    Log.d("Error","Error");
                }

                Intent intent = new Intent(Modify_Event.this, Calendar_Activity.class);
                Bundle b = new Bundle();
                b.putString("IdProject", param);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });

    }

    private void toDate(String hour, String minutes) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_modify__event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
