package com.example.danis.pulpomatic;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    Button button;
    Button botonIniciar;
    public boolean botonGO = false;


    String serveKey = "AIzaSyDw3prMyflvWLpEekCyMjtmoY5PWaCD5lE";
    GoogleMap mGoogleMap;
    double lon;
    double lat;
    LatLng latlngActual;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Address calle;
    MarkerOptions miUbicacion;
    TextView direccion;
    TextView distancia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botonIniciar = (Button) findViewById(R.id.iniciar_button);
        button = (Button) findViewById(R.id.iniciar_button);

        direccion = (TextView) findViewById(R.id.tvDireccion);
        distancia = (TextView) findViewById(R.id.tvDistancia);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Obteniendo dirección actual
        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        lon = location.getLongitude();
        lat = location.getLatitude();
        latlngActual = new LatLng(lat, lon);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.clear();
        //Verificamos si es mayor a 6 (Marshmallow)
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlngActual, 15));
            }
        } else {
            //Si es menor, no requiere pedir permisos
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permiso aceptado!!
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    }
                } else {
                    //Permiso denegado!!
                    Toast.makeText(this, "Permiso denegado", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    public void onMapSearch(View view) {
        mGoogleMap.clear();
        EditText locationSearch = (EditText) findViewById(R.id.editText);
        Log.d("HOLA", locationSearch.getText().toString());
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!addressList.isEmpty()) {
                final Address address = addressList.get(0);

                final LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                GoogleDirection.withServerKey(serveKey)
                        .from(latlngActual)
                        .to(latLng)
                        .alternativeRoute(true)
                        .transitMode(TransportMode.WALKING)
                        .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                Log.d("Status", direction.getStatus());

                                if (direction.isOK()) {
                                    Route route = direction.getRouteList().get(0);

                                    mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(address.getAddressLine(0)));
                                    //mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                                    ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
                                    mGoogleMap.addPolyline(DirectionConverter.createPolyline(MainActivity.this, directionPositionList, 5, Color.RED));

                                    //Zoom entre los dos marcadores
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    builder.include(latlngActual);
                                    builder.include(latLng);

                                    LatLngBounds bounds = builder.build();
                                    int width = getResources().getDisplayMetrics().widthPixels;
                                    int height = getResources().getDisplayMetrics().heightPixels;
                                    int padding = (int) (width * 0.30);

                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                                    mGoogleMap.animateCamera(cameraUpdate);


                                    //PolyLine Recta
                                /*Polyline line = mGoogleMap.addPolyline(new PolylineOptions()
                                .add(latlngActual, latLng)
                                .width(5)
                                .color(Color.BLUE));*/


                                    double distanceBetween = SphericalUtil.computeDistanceBetween(latlngActual, latLng);
                                    int distanciaMetros = (int) Math.ceil(distanceBetween);

                                    if (distanciaMetros > 200) {
                                        direccion.setText("Estas muy lejos de tu objetivo");
                                    }
                                    if (distanciaMetros > 100 && distanciaMetros <= 200) {
                                        direccion.setText("Estás lejos del punto objetivo");

                                    }
                                    if (distanciaMetros > 50 && distanciaMetros <= 100) {
                                        direccion.setText("Estás próximo al punto objetivo");

                                    }
                                    if (distanciaMetros > 10 && distanciaMetros <= 50) {
                                        direccion.setText("Estás muy próximo al punto objetivo");

                                    }
                                    if (distanciaMetros < 10) {
                                        direccion.setText("Estás en el punto objetivo");
                                    }

                                    distancia.setText("Distancia: " + distanciaMetros + " M");
                                }
                            }

                            @Override
                            public void onDirectionFailure(Throwable t) {
                                Log.e("Error", t.toString());
                            }
                        });

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(locationSearch.getWindowToken(), 0);
            }else {
                Log.d("HOLA","AKIKEDO");
                Toast.makeText(this,"Oops! Se te olvidó ingresar la dirección",Toast.LENGTH_LONG).show();
                Snackbar.make(view, "Oops! Se te olvidó ingresar la dirección!", Snackbar.LENGTH_LONG).show();
            }
        }

    }


    public void onNavigationStart(View v) {

        checkLocationPermission();

        EditText locationSearch = (EditText) findViewById(R.id.editText);
        String location = locationSearch.getText().toString();

        if (botonGO != false) {

            button.setText("GO");
            botonGO = false;
            locationSearch.setText("");
            direccion.setText("Selecciona una dirección");
            distancia.setText("");
            onMapReady(mGoogleMap);
        } else {

            List<Address> addressList = null;

            if (location != null || !location.equals("")) {
                Geocoder geocoder = new Geocoder(this);
                try {
                    addressList = geocoder.getFromLocationName(location, 1);

                } catch (IOException e) {
                    e.printStackTrace();
                }


                if (!addressList.isEmpty()) {
                    final Address address = addressList.get(0);

                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?saddr=" + lat + "," + lon + "&daddr=" + address.getLatitude() + "," + address.getLongitude()));
                    startActivity(intent);
                    button.setText("Parar");
                    botonGO = true;
                }else{
                    Snackbar.make(v, "Tienes que buscar una dirección primero", Snackbar.LENGTH_LONG).show();
                }

            }
        }
    }
}





