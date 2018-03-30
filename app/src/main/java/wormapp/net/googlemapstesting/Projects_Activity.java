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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class Projects_Activity extends ActionBarActivity {

    String param;
    String param2;
    ListView lv;

    InputStream is = null;
    JSONObject jObj2 = null;
    List<Projet> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        lv= (ListView) findViewById(R.id.listView);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Projects_Activity.this, Maps_Activity.class);
                Bundle b = new Bundle();
                param2 = String.valueOf(arrayList.get(position).IdProjet);
                new AsyncTask<String, String, Void>() {

                    private ProgressDialog progressDialog = new ProgressDialog(Projects_Activity.this);
                    protected void onPreExecute() {
                        progressDialog.setMessage("Fetching");
                        progressDialog.show();
                    }

                    protected Void doInBackground(String... params){

                        Thread t = new Thread() {

                            public void run() {
                                Looper.prepare(); //For Preparing Message Pool for the child Thread
                                HttpClient client = new DefaultHttpClient();
                                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10001); //Timeout Limit
                                HttpResponse response;
                                JSONObject json = new JSONObject();

                                try {
                                    HttpPost post = new HttpPost("http://41.227.194.224:81/Service1.svc/ExploitationsGetter");
                                    json.put("IdProjet",param2);
                                    //json.put("IdProjet",30);
                                    StringEntity se = new StringEntity( json.toString());
                                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                                    post.setEntity(se);
                                    response = client.execute(post);

                    /*Checking response */
                                    if(response!=null){
                                        InputStream in = response.getEntity().getContent(); //Get the data in the entity
                                        String is = convertStreamToString(in);
                                        progressDialog.dismiss();
                                        //Log.d("result", is);

                                        try {
                                            Maps_Activity.jObj = new JSONObject(is);
                                        } catch (JSONException e) {
                                            Log.e("JSON Parser", "Error parsing data " + e.toString());
                                        }
                                    }

                                } catch(Exception e) {
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
                b.putString("IdProp", param);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });

        Intent myIntent = getIntent();
        param = myIntent.getStringExtra("IdProp");

        new task().execute();
    }

    public void onBackPressed() {
        finish();
    }

    class task extends AsyncTask<String, String, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(Projects_Activity.this);
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
                        HttpPost post = new HttpPost("http://41.227.194.224:81/Service1.svc/GetProjects");
                        json.put("IdProp",param);
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
                                jObj2 = new JSONObject(is);
                            } catch (JSONException e) {
                                Log.e("JSON Parser", "Error parsing data " + e.toString());
                            }

                            JSONArray array = jObj2.getJSONArray("GetProjectsResult");
                            JSONObject jo = null;

                            for (int i = 0; i<array.length();i++){
                                jo = array.getJSONObject(i);
                                arrayList.add(new Projet(jo));
                            }

                            Log.d("array", arrayList.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                            ArrayAdapter<Projet> arrayAdapter = new ArrayAdapter<Projet>(Projects_Activity.this,android.R.layout.simple_list_item_1,arrayList);
                            lv.setAdapter(arrayAdapter);
                                }
                            });
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

    public class Projet {
        String DateAjoutExploitation;
        String DescriptionProjet;
        int IdProjet;
        String NomProjet;
        int FK_IdProprietaire;

        Projet(JSONObject jsonObject){
            try {
                this.IdProjet = jsonObject.getInt("idProjet");
                this.NomProjet = jsonObject.getString("NomProjet");
                this.DescriptionProjet = jsonObject.getString("DescriptionProjet");
                this.DateAjoutExploitation = jsonObject.getString("DateAjoutExploitation");
                this.FK_IdProprietaire = jsonObject.getInt("FK_IdProprietaire");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public String toString(){
            return "Projet: "+NomProjet;
        }
    }
}
