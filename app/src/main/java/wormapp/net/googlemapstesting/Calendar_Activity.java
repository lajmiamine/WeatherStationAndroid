package wormapp.net.googlemapstesting;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Calendar_Activity extends ActionBarActivity {

    private CalendarView calendar;

    String param;

    ArrayList<Calendar_Event> arrayList = new ArrayList<Calendar_Event>();
    ArrayList<Calendar_Event> arrayListByDay = new ArrayList<>();
    ListView listView;
    int selectedItemId;

    int[] currentDate;
    private String previousParam;
    private String param2;
    private String zoom;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent myIntent = getIntent();
        param = myIntent.getStringExtra("IdProject");
        previousParam = myIntent.getStringExtra("previousParam");
        param2 = myIntent.getStringExtra("IdProp");
        zoom = myIntent.getStringExtra("Zoom");

        setContentView(R.layout.activity_calendar);
        currentDate = getCurrentDate();

        new gettingCalendar().execute();
        initializeCalendar();

        listView = (ListView) findViewById(R.id.listView);
        String[] values = new String[] { "Selectionner un jour" };
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Calendar_Activity.this,android.R.layout.simple_list_item_1, values);
        listView.setAdapter(adapter);

    }

    public void onBackPressed() {
        Intent intent = new Intent(Calendar_Activity.this, Maps_Activity.class);
        Bundle b = new Bundle();
        b.putString("IdProject", previousParam);
        b.putString("IdProp", param2);
        b.putString("Zoom", zoom);
        intent.putExtras(b);
        startActivity(intent);
        finish();
        return;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initializeCalendar() {
        calendar = (CalendarView) findViewById(R.id.calendar);
        calendar.setShowWeekNumber(false);
        calendar.setFirstDayOfWeek(2);
        calendar.setSelectedWeekBackgroundColor(getResources().getColor(R.color.green));
        calendar.setUnfocusedMonthDateColor(getResources().getColor(R.color.transparent));
        calendar.setWeekSeparatorLineColor(getResources().getColor(R.color.transparent));
        calendar.setSelectedDateVerticalBar(R.color.darkgreen);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                arrayListByDay = new ArrayList<Calendar_Event>();
                for (int i=0;i<arrayList.size();i++){
                    if (arrayList.get(i).isTheDay(year,month+1,day) == true) {
                        arrayListByDay.add(arrayList.get(i));
                    }
                }
                if (arrayListByDay.size()!= 0) {
                    final UsersAdapter adapter = new UsersAdapter(Calendar_Activity.this, arrayListByDay);
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedItemId = Integer.parseInt(parent.getItemAtPosition(position).toString());
                            Log.d("selectedItemId",String.valueOf(selectedItemId));
                            for (int i=0;i<arrayListByDay.size();i++){
                                if (arrayListByDay.get(i).IdCalendrier == selectedItemId){
                                    Date date = new Date();
                                    if(arrayListByDay.get(i).getDate().before(date)){
                                        Toast.makeText(getApplicationContext(), "Vous ne pouvez pas modifier cette évennement!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        popUp(selectedItemId);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    });
                }
                else {
                    ArrayList<String> arrayListNoEvents = new ArrayList<String>();
                    String[] values = new String[] { "No events" };
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(Calendar_Activity.this,android.R.layout.simple_list_item_1, values);
                    listView.setAdapter(adapter);
                }

            }
        });
    }

    public class UsersAdapter extends ArrayAdapter<Calendar_Event> {
        public UsersAdapter(Context context, ArrayList<Calendar_Event> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Calendar_Event event = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
            }

            TextView Date = (TextView) convertView.findViewById(R.id.Date);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);

            Date.setText(event.Working_date());


            switch(event.IdCalendrier%4){
                case 0 : imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.evaa));
                    break;
                case 1 : imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.evbb));
                    break;
                case 2 : imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.evcc));
                    break;
                case 3 : imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.evdd));
                    break;
            }
            return convertView;
        }
    }

    public int[] getCurrentDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss");
        Date date = new Date();
        String currentDate = dateFormat.format(date);

        int[] cDate= new int[5];
        String[] s =currentDate.split("\\.");
        cDate[0]=Integer.parseInt(s[0]);
        cDate[1]=Integer.parseInt(s[1]);
        cDate[2]=Integer.parseInt(s[2]);
        String[] s1 = s[3].split("\\:");
        cDate[3] = Integer.parseInt(s1[0]);
        cDate[4]=Integer.parseInt((s1[1]));

        return cDate;
    }


    /*****************************Getting calendar****************************/

    class gettingCalendar extends AsyncTask<String, String, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(Calendar_Activity.this);
        protected void onPreExecute() {
            progressDialog.setMessage("Fetching");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    gettingCalendar.this.cancel(true);
                }
            });
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
                        HttpPost post = new HttpPost("http://41.227.194.224:81/Service1.svc/GetCalendarByStation");
                        json.put("IdStation",param);
                        //json.put("IdStation",140);
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

                            JSONObject jObj=null;
                            try {
                                jObj = new JSONObject(is);
                            } catch (JSONException e) {
                                Log.e("JSON Parser", "Error parsing data " + e.toString());
                            }

                            JSONArray array = jObj.getJSONArray("GetCalendarByStationResult");
                            JSONObject jo = null;

                            for (int i = 0; i<array.length();i++){
                                jo = array.getJSONObject(i);
                                arrayList.add(new Calendar_Event(jo));
                                Log.d("arraylist",String.valueOf(arrayList.get(i).IdCalendrier));
                            }
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

    class RemovingEvent extends AsyncTask<String, String, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(Calendar_Activity.this);
        protected void onPreExecute() {
            progressDialog.setMessage("Fetching");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    RemovingEvent.this.cancel(true);
                }
            });
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
                        HttpPost post = new HttpPost("http://41.227.194.224:81/Service1.svc/EventRomovor");
                        json.put("IdCalendrier",selectedItemId);
                        //json.put("IdStation",140);
                        StringEntity se = new StringEntity( json.toString());
                        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                        post.setEntity(se);
                        response = client.execute(post);
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
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss");
        return format.format(date).toString();
    }
    /************************************************************************/

    private void popUp(final int selectedItemId) {

        final Dialog dialog = new Dialog(Calendar_Activity.this);
        dialog.setContentView(R.layout.dialog_calendar);
        dialog.setTitle("Options");

        Button removeBtn, editBtn,cancel;

        removeBtn = (Button) dialog.findViewById(R.id.removeBtn);
        editBtn = (Button) dialog.findViewById(R.id.editBtn);
        cancel = (Button) dialog.findViewById(R.id.cancelBtn);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new AsyncTask<String, String, Void>() {

                    private ProgressDialog progressDialog = new ProgressDialog(Calendar_Activity.this);
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
                                    HttpPost post = new HttpPost("http://41.227.194.224:81/Service1.svc/EventRomovor");
                                    json.put("IdCalendrier",selectedItemId);
                                    //json.put("IdStation",140);
                                    StringEntity se = new StringEntity( json.toString());
                                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                                    post.setEntity(se);
                                    response = client.execute(post);
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
                Toast.makeText(getApplicationContext(), "Event supprimer", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Calendar_Activity.this, Calendar_Activity.class);
                Bundle b = new Bundle();
                b.putString("IdProject", param);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });

        final Dialog dialogModify = new Dialog(Calendar_Activity.this);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                Intent intent = new Intent(Calendar_Activity.this, Modify_Event.class);
                Bundle b = new Bundle();
                b.putString("IdProject", param);
                b.putString("IdCalendrier",String.valueOf(selectedItemId));
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }

    private void toDate(String hour, String minutes) {
        
    }

}
