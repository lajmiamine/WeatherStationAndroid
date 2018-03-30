package wormapp.net.googlemapstesting;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

public class Maps_Activity extends FragmentActivity implements OnMapReadyCallback {

    Button cancelBtn, infoBtn;
    String param;
    public static JSONObject jObj=null;
    int a;
    String variable;
    String addon;
    float zoom=0;
    static LatLng targetLatLng = null;


    JSONObject array;
    JSONArray arrayExploitation;
    JSONArray arrayParcelles;
    JSONArray arraySecteur;
    JSONArray arrayNoeuds;
    JSONArray arrayElectrovannes;

    List<Exploitation> arrayList = new ArrayList<>();
    List<Parcelle> arrayListParcelles = new ArrayList<>();
    List<Secteur> arrayListSecteur = new ArrayList<>();
    List<Noeud> arrayListNoeuds = new ArrayList<>();
    List<Electovanne> arrayListElectrovannes = new ArrayList<>();

    final List<Marker> markerList = new ArrayList<>();
    final List<Marker> markerListNoeuds = new ArrayList<>();
    final List<Marker> markerListElectrovannes = new ArrayList<>();
    private String param2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent myIntent = getIntent();
        try {
            param = myIntent.getStringExtra("IdProject");
        }
        catch (Exception e)
        {

        }
        param2 = myIntent.getStringExtra("IdProp");
        try {
            zoom = Float.parseFloat(myIntent.getStringExtra("Zoom"));
        }
        catch (Exception e)
        {

        }

        new gettingExploitations().execute();
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onBackPressed() {
        targetLatLng = null;
        Intent intent = new Intent(Maps_Activity.this, Projects_Activity.class);
        Bundle b = new Bundle();
        b.putString("IdProp", param2);
        intent.putExtras(b);
        startActivity(intent);
        finish();
        return;
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        float f = (float) 6.5; // initial zoom
        LatLng tunisia = new LatLng(33.886, 9.537);
        if (zoom != 0) {
            if (targetLatLng != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, zoom));
            }
            else
            {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(tunisia, zoom));
            }
        }
        else
        {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(tunisia, f));
        }

        /******************Préparation des exploitation*******************/

        //final List<Marker> markerList = new ArrayList<>();
        final List<Polygon> polygonList = new ArrayList<>();

        for (int i=0; i<arrayList.size();i++){


            double lat=0, lng=0;
            String[] s =arrayList.get(i).AdresseExploitation.split(",");
            PolygonOptions polygonOptions = new PolygonOptions();
            final Polygon polygon;
            for (int j=0; j< s.length;j+=2){
                lat=lat+Double.parseDouble(s[j]);
                lng=lng+Double.parseDouble(s[j+1]);
                polygonOptions.add(new LatLng(Double.parseDouble(s[j]),Double.parseDouble(s[j+1])));
            }

            lat=lat/(s.length/2);
            lng=lng/(s.length/2);

            final Marker mk = map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .anchor(0.5f, 1.0f)
                    .position(new LatLng(lat, lng)));
            mk.setTitle(arrayList.get(i).NomExploitation);
            String st = arrayList.get(i).DescriptionExploitation + ", nombre de neouds: "+arrayList.get(i).NombreNoeuds;
            mk.setSnippet(st);
            markerList.add(mk);

            polygon = map.addPolygon(polygonOptions.strokeColor(Color.BLUE));
            polygonList.add(polygon);

        }

        /******************Préparation des parcelles*******************/

        final List<Polygon> polygonListParcelles = new ArrayList<>();
        boolean test=true;
        for (int i=0; i<arrayListParcelles.size();i++){

            String[] s =arrayListParcelles.get(i).AdresseParcelle.split(",");
            PolygonOptions polygonOptionsParcelles = new PolygonOptions();
            final Polygon polygon;
            for (int j=0; j< s.length;j+=2){
                polygonOptionsParcelles.add(new LatLng(Double.parseDouble(s[j]),Double.parseDouble(s[j+1])));
            }

            polygon = map.addPolygon(polygonOptionsParcelles.strokeColor(Color.BLACK));


            if (test == true) {
                polygon.setFillColor(Color.GREEN);
                test=false;
            }
            else polygon.setFillColor(Color.BLUE);


            polygonListParcelles.add(polygon);

        }

        /******************Préparation des secteurs*******************/

        final List<Polygon> polygonListSecteurs = new ArrayList<>();

        for (int i=0; i<arrayListSecteur.size();i++){

            String[] s =arrayListSecteur.get(i).AdresseSecteur.split(",");
            PolygonOptions polygonOptionsSecteurs = new PolygonOptions();
            final Polygon polygon;
            for (int j=0; j< s.length;j+=2){
                polygonOptionsSecteurs.add(new LatLng(Double.parseDouble(s[j]),Double.parseDouble(s[j+1])));
            }

            polygon = map.addPolygon(polygonOptionsSecteurs.strokeColor(Color.BLACK).fillColor(Color.BLUE));
            polygonListSecteurs.add(polygon);

        }

        /******************Préparation des Noeuds*******************/

        //final List<Marker> markerListNoeuds = new ArrayList<>();

        for (int i=0; i<arrayListNoeuds.size();i++){


            double lat=0, lng=0;
            Double NoeudLong =Double.parseDouble(arrayListNoeuds.get(i).Latitude);
            Double NoeudLat =Double.parseDouble(arrayListNoeuds.get(i).Longitude);

            Marker mk=null;

            switch (arrayListNoeuds.get(i).Type) {
                case 1:  mk = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .anchor(0.5f, 1.0f)
                        .position(new LatLng(NoeudLong, NoeudLat)));
                    mk.setTitle("Type:"+arrayListNoeuds.get(i).Type);
                    break;
                case 2:  mk = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .anchor(0.5f, 1.0f)
                        .position(new LatLng(NoeudLong, NoeudLat)));
                    mk.setTitle("Type:"+arrayListNoeuds.get(i).Type);
                    break;
                case 3:  mk = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        .anchor(0.5f, 1.0f)
                        .position(new LatLng(NoeudLong, NoeudLat)));
                    mk.setTitle("Type:"+arrayListNoeuds.get(i).Type);
                    break;
                case 4:  mk = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                        .anchor(0.5f, 1.0f)
                        .position(new LatLng(NoeudLong, NoeudLat)));
                    mk.setTitle("Type:"+arrayListNoeuds.get(i).Type);
                    break;
            }
            markerListNoeuds.add(mk);

        }

        /******************Préparation des Noeuds*******************/

        //final List<Marker> markerListElectrovannes = new ArrayList<>();

        for (int i=0; i<arrayListElectrovannes.size();i++){


            double lat=0, lng=0;
            Double NoeudLong =Double.parseDouble(arrayListElectrovannes.get(i).Latitude);
            Double NoeudLat =Double.parseDouble(arrayListElectrovannes.get(i).Longitude);

            final Marker mk = map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .anchor(0.5f, 1.0f)
                    .position(new LatLng(NoeudLong, NoeudLat)));
            mk.setTitle("IdElectrovanne:"+arrayListElectrovannes.get(i).IdElectrovanne);
            markerListElectrovannes.add(mk);

        }

        /******************Dessin de tous*******************/

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                for (int k=0; k<markerList.size();k++) {
                    markerList.get(k).setVisible(cameraPosition.zoom < 11.5);
                }

                for (int k=0; k<markerListNoeuds.size();k++) {
                    markerListNoeuds.get(k).setVisible(cameraPosition.zoom > 17);
                }

                for (int k=0; k<markerListElectrovannes.size();k++) {
                    markerListElectrovannes.get(k).setVisible(cameraPosition.zoom > 17);
                }

                for (int k=0; k<polygonList.size();k++) {
                    polygonList.get(k).setVisible(cameraPosition.zoom > 10);
                }

                for (int k=0; k<polygonListParcelles.size();k++) {
                    polygonListParcelles.get(k).setVisible(cameraPosition.zoom > 15);
                }

                for (int k=0; k<polygonListSecteurs.size();k++) {
                    polygonListSecteurs.get(k).setVisible(cameraPosition.zoom > 17);
                }

            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                zoom = map.getCameraPosition().zoom;

                targetLatLng = map.getCameraPosition().target;

                //Log.d("getId",marker.getId());
                //a = Integer.parseInt(marker.getId().split("m")[1])-2;
                a = Integer.parseInt(marker.getId().split("m")[1])-arrayList.size();
                Log.d("Type ",String.valueOf(a));

                try {
                    variable = String.valueOf(arrayListNoeuds.get(a).IdImplanter_noeud_secteur);
                }
                catch (Exception e){

                }

                String type=null;
                try {
                    type = marker.getTitle().split(":")[1];
                }
                catch (Exception e){

                }
                try {
                    if (type.equals("4")) {
                        popUp();
                    }
                    else if (type.equals("1")){
                        popUp2();
                    }
                    else {

                    }
                }
                catch (Exception e){

                }
                return false;
            }
        });
    }

    private void popUp() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle("Options");

        infoBtn = (Button) dialog.findViewById(R.id.infoBtn);
        cancelBtn = (Button) dialog.findViewById(R.id.cancelBtn);
        infoBtn.setText("Consulter calendrier");

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Maps_Activity.this, Calendar_Activity.class);
                Bundle b = new Bundle();
                b.putString("IdProject", String.valueOf(variable));
                b.putString("IdProp", param2);
                b.putString("Zoom", String.valueOf(zoom));
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }

    private void popUp2() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle("Options");

        infoBtn = (Button) dialog.findViewById(R.id.infoBtn);
        cancelBtn = (Button) dialog.findViewById(R.id.cancelBtn);
        infoBtn.setText("Voir graph");

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Maps_Activity.this, Chart_Activity.class);
                Bundle b = new Bundle();
                addon = String.valueOf(variable);
                new AsyncTask<String, String, Void>() {

                    private ProgressDialog progressDialog = new ProgressDialog(Maps_Activity.this);
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
                                    String URL = "http://41.227.194.224:81/Service1.svc/MesuresNoeudsGetter/";
                                    HttpGet get = new HttpGet(URL+addon);
                                    StringEntity se = new StringEntity( json.toString());
                                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                                    response = client.execute(get);

                        /*Checking response */
                                    if(response!=null){
                                        InputStream in = response.getEntity().getContent(); //Get the data in the entity
                                        String is = convertStreamToString(in);
                                        progressDialog.dismiss();

                                        try
                                        {
                                            Chart_Activity.jObj = new JSONObject(is);
                                        }
                                        catch (JSONException e)
                                        {
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
                b.putString("previousParam", param);
                b.putString("IdProp", param2);
                b.putString("Zoom", String.valueOf(zoom));
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }

    class gettingExploitations extends AsyncTask<String, String, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(Maps_Activity.this);
        protected void onPreExecute() {
            progressDialog.setMessage("Fetching");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    gettingExploitations.this.cancel(true);
                }
            });
        }

        protected Void doInBackground(String... params){
            return null;
        }

        protected void onPostExecute(Void v) {

            /*getting all data*/
            try {
                array = jObj.getJSONObject("ExploitationsGetterResult");
                Log.d("array all data",array.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /*****************************************/


            /*getting ExploitationsList */
            try {
                arrayExploitation = array.getJSONArray("Exploitations");
                Log.d("array ExploitationsList",arrayExploitation.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /*Filling arrayList by Exploitations*/
            JSONObject joE = null;
            for (int i = 0; i<arrayExploitation.length();i++){
                try {
                    joE = arrayExploitation.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                arrayList.add(new Exploitation(joE));
            }
            /*****************************************/


            /*getting ParcellesList */
            try {
                arrayParcelles = array.getJSONArray("Parcelles");
                Log.d("array ParcellesList",arrayParcelles.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /*Filling arrayList by Exploitations*/
            JSONObject joP = null;
            for (int i = 0; i<arrayParcelles.length();i++){
                try {
                    joP = arrayParcelles.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                arrayListParcelles.add(new Parcelle(joP));
            }
            /*****************************************/


            /*getting SecteursList */
            try {
                arraySecteur = array.getJSONArray("Secteurs");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /*Filling arrayList by Exploitations*/
            JSONObject joS = null;
            for (int i = 0; i<arraySecteur.length();i++){
                try {
                    joS = arraySecteur.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                arrayListSecteur.add(new Secteur(joS));
            }
            /*****************************************/


            /*getting SecteursList */
            try {
                arrayNoeuds = array.getJSONArray("Noeuds");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /*Filling arrayList by Exploitations*/
            JSONObject joN = null;
            for (int i = 0; i<arrayNoeuds.length();i++){
                try {
                    joN = arrayNoeuds.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                arrayListNoeuds.add(new Noeud(joN));
            }
            /*****************************************/
            progressDialog.dismiss();
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public class Exploitation{
        String AdresseExploitation;
        String DateAjoutExploitation;
        int IdExploitation;
        String DescriptionExploitation;
        int FK_IdProjet;
        String NomExploitation;
        int NombreNoeuds;

        Exploitation(JSONObject jsonObject){
            try {
                this.NomExploitation = jsonObject.getString("NomExploitation");
                this.IdExploitation = jsonObject.getInt("IdExploitation");
                this.AdresseExploitation = jsonObject.getString("AdresseExploitation");
                this.DescriptionExploitation = jsonObject.getString("DescriptionExploitation");
                this.DateAjoutExploitation = jsonObject.getString("DateAjoutExploitation");
                this.FK_IdProjet = jsonObject.getInt("FK_IdProjet");
                this.NombreNoeuds = jsonObject.getInt("NombreNoeuds");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public String toString(){
            return "Exploitation: "+IdExploitation;
        }
    }

    public class Parcelle{
        String AdresseParcelle;
        int IdParcelle;
        String DescriptionParcelle;
        int FK_IdExploitation;
        String NomParcelle;
        String CouleurParcelle;

        Parcelle(JSONObject jsonObject){
            try {
                this.NomParcelle = jsonObject.getString("NomParcelle");
                this.IdParcelle = jsonObject.getInt("IdParcelle");
                this.AdresseParcelle = jsonObject.getString("AdresseParcelle");
                this.DescriptionParcelle = jsonObject.getString("DescriptionExploitation");
                this.FK_IdExploitation = jsonObject.getInt("FK_IdExploitation");
                this.CouleurParcelle = jsonObject.getString("CouleurParcelle");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public String toString(){
            return "Parcelle: "+IdParcelle;
        }
    }

    public class Secteur{
        String AdresseSecteur;
        int IdSecteur;
        String DescriptionSecteur;
        int FK_IdParcelle;
        String NomSecteur;

        Secteur(JSONObject jsonObject){
            try {
                this.NomSecteur = jsonObject.getString("NomSecteur");
                this.IdSecteur = jsonObject.getInt("IdSecteur");
                this.AdresseSecteur = jsonObject.getString("AdresseSecteur");
                this.DescriptionSecteur = jsonObject.getString("DescriptionSecteur");
                this.FK_IdParcelle = jsonObject.getInt("FK_IdParcelle");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public String toString(){
            return "Secteur: "+IdSecteur;
        }
    }

    public class Noeud{
        int IdImplanter_noeud_secteur;
        int Type;
        String Latitude;
        String Longitude;
        int FK_Id_secteur;

        Noeud(JSONObject jsonObject){
            try {
                this.IdImplanter_noeud_secteur = jsonObject.getInt("IdImplanter_noeud_secteur");
                this.Type = jsonObject.getInt("Type");
                this.Latitude = jsonObject.getString("Latitude");
                this.Longitude = jsonObject.getString("Longitude");
                this.FK_Id_secteur = jsonObject.getInt("FK_Id_secteur");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public String toString(){
            return "Noeuds: "+IdImplanter_noeud_secteur;
        }
    }

    public class Electovanne{
        int IdElectrovanne;
        int FK_IdSecteur;
        int FK_IdImlpNoeudIrrigation;
        String Latitude;
        String Longitude;

        Electovanne(JSONObject jsonObject){
            try {
                this.IdElectrovanne = jsonObject.getInt("IdElectrovanne");
                this.FK_IdSecteur = jsonObject.getInt("FK_IdSecteur");
                this.Latitude = jsonObject.getString("Latitude");
                this.Longitude = jsonObject.getString("Longitude");
                this.FK_IdImlpNoeudIrrigation = jsonObject.getInt("FK_IdImlpNoeudIrrigation");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public String toString(){
            return "Electovannes: "+IdElectrovanne;
        }
    }
}