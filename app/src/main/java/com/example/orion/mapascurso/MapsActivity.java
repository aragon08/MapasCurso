package com.example.orion.mapascurso;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, LocationListener, Response.Listener<String>, Response.ErrorListener {

    private GoogleMap mMap;
    private Marker marca;
    private double latActual, lngActual;
    private LocationManager adminLoc;
    private boolean banGPS = false, banRED = false;
    private double latMarca,lngMarca;
    private RequestQueue qSolicitudes;
    private Marker[] marcas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        qSolicitudes= Volley.newRequestQueue(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        getPosicionActual();

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mapa, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itmVistaNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.itmVistaSatelite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.itmVistaHibrida:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.itmVistaRelieve:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (marca != null) {
            marca.remove();
        }
        marca = mMap.addMarker(new MarkerOptions().position(latLng).title("Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
        latMarca=marca.getPosition().latitude;
        lngMarca=marca.getPosition().longitude;
        getLugaresCercanos();
    }

    private void getLugaresCercanos(){
        String URL= "http://demo.places.nlp.nokia.com/places/v1/discover/explore?at="+latMarca+","+lngMarca+"&app_id=DemoAppId01082013GAL&app_code=AJKnXv84fjrb0KIHawS0Tg&tf=plain&pretty=true";
        StringRequest reqUbicacion= new StringRequest(Request.Method.GET,URL,this,this);
        qSolicitudes.add(reqUbicacion);
    }

    private void getPosicionActual() {
        adminLoc = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        banGPS = adminLoc.isProviderEnabled(LocationManager.GPS_PROVIDER);
        banRED = adminLoc.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (banGPS) {
            try {
                adminLoc.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);
                Location ubicacion = adminLoc.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (ubicacion!=null){
                    latActual = ubicacion.getLatitude();
                    lngActual = ubicacion.getLongitude();
                }

            } catch (SecurityException se) {
            }
        }
    }
    @Override
    public void onLocationChanged (Location location){

    }

    @Override
    public void onStatusChanged (String provider,int status, Bundle extras){

    }

    @Override
    public void onProviderEnabled (String provider){

    }

    @Override
    public void onProviderDisabled (String provider){

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        try{
            JSONObject lugar;
            Double latLugar,lngLugar;

            JSONObject objJSON= new JSONObject(response);
            JSONObject jsonResults= objJSON.getJSONObject("results");
            JSONArray arrItems=jsonResults.getJSONArray("items");

            marcas= new Marker[arrItems.length()];
            for (int i=0;i<arrItems.length();i++){
                lugar=arrItems.getJSONObject(i);
                latLugar=lugar.getJSONArray("position").getDouble(0);
                lngLugar=lugar.getJSONArray("position").getDouble(1);
                marcas[i]= mMap.addMarker(new MarkerOptions().position(new LatLng(latLugar,lngLugar)).title(lugar.getString("title")));
            }
        }catch(Exception e){}

    }
}
