package wormapp.net.googlemapstesting;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Chart_Activity extends ActionBarActivity {

    InputStream is = null;
    static public JSONObject jObj = null;
    List<MesureNoeuds> arrayList = new ArrayList<>();
    private String param;
    private String previousParam;
    private String param2;
    private String zoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent myIntent = getIntent();
        try {
            param = myIntent.getStringExtra("addon");
        }
        catch (Exception e)
        {

        }
        previousParam = myIntent.getStringExtra("previousParam");
        param2 = myIntent.getStringExtra("IdProp");
        zoom = myIntent.getStringExtra("Zoom");

        new task().execute();
        setContentView(R.layout.activity_chart);
        //Painting();

        final ProgressDialog progressDialog = new ProgressDialog(Chart_Activity.this);
        progressDialog.setMessage("Fetching");
        progressDialog.show();
        Runnable mRunnable;
        Handler mHandler=new Handler();
        mRunnable=new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Painting();
            }
        };
        mHandler.postDelayed(mRunnable, 1000);

    }

    public void onBackPressed() {
        Intent intent = new Intent(Chart_Activity.this, Maps_Activity.class);
        Bundle b = new Bundle();
        b.putString("IdProject", previousParam);
        b.putString("IdProp", param2);
        b.putString("Zoom", zoom);
        intent.putExtras(b);
        startActivity(intent);
        finish();
        return;
    }

    void Painting() {
        LineChart chart = (LineChart) findViewById(R.id.chart);

        int count=24;
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add((i) + "");
        }

        /*********************Vals1*************************/
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        /*yVals.add(new Entry(11, 2));
        yVals.add(new Entry((float)11.5, 4));
        yVals.add(new Entry((float)11.1, 7));
        yVals.add(new Entry((float)10.8, 12));
        yVals.add(new Entry((float)11.0, 16));

        LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
        set1.setColor(Color.rgb(0, 0, 155));
        set1.setCircleColor(Color.RED);
        set1.setLineWidth(2f);
        set1.setCircleSize(4f);*/

        /*********************Vals2*************************/
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();

        for(int i=0;i<arrayList.size();i++){
            yVals2.add(new Entry(arrayList.get(i).TemperatureAir, i));
        }
        LineDataSet set2 = new LineDataSet(yVals2, "DataSet 2");
        set2.setColor(Color.rgb(0, 155, 0));
        set2.setCircleColor(Color.RED);
        set2.setLineWidth(2f);
        set2.setCircleSize(4f);

        /*********************Painting***********************/
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        //dataSets.add(set1);
        dataSets.add(set2);

        LineData data = new LineData(xVals, dataSets);
        chart.setData(data);
        chart.setDescription("Comparing Chart");
        chart.invalidate();
    }

    class task extends AsyncTask<String, String, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(Chart_Activity.this);
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
            return null;
        }

        protected void onPostExecute(Void v) {
            JSONArray array = null;
            try {
                array = jObj.getJSONArray("MesuresNoeudsGetterResult");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject jo = null;
            for (int i = 0; i<array.length();i++){
                try {
                    jo = array.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                arrayList.add(new MesureNoeuds(jo));
            }
            progressDialog.dismiss();
        }
    }

    static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public class MesureNoeuds {
        float TemperatureAir;

        MesureNoeuds(JSONObject jsonObject){
            try {
                this.TemperatureAir = (float)jsonObject.getDouble("TemperatureAir");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public String toString(){
            return "MesureNoeuds: "+TemperatureAir;
        }
    }
}