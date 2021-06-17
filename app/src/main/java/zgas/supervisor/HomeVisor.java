package zgas.supervisor;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import zgas.supervisor.providers.AuthProvider;
import zgas.supervisor.providers.DriverProvider;
import zgas.supervisor.providers.GeofireProvider;

public class HomeVisor extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;


    private LatLng mCurrentLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_visor);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");



        mCurrentLatLng = new LatLng(20.6340165, -103.3536772);

        getActiveDrivers();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(mCurrentLatLng)
                        .zoom(15f)
                        .build()
        ));
    }



    private List<Marker> mDriversMarkers = new ArrayList<>();

    private void getActiveDrivers() {
        mGeofireProvider.getActiveDrivers(mCurrentLatLng, 10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // AÑADIREMOS LOS MARCADORES DE LOS CONDUCTORES QUE SE CONECTEN EN LA APLICACION

                for (Marker marker: mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }
                }

                try {
                    LatLng driverLatLng = new LatLng(location.latitude, location.longitude);


                    DriverProvider driverProvider = new DriverProvider();

                    driverProvider.getDriver(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.exists())
                            {
                                String nombre = snapshot.child("nombre").getValue().toString();
                                String apellido = snapshot.child("apellido").getValue().toString();
                                String telefono = snapshot.child("telefono").getValue().toString();

                                Marker marker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title(nombre + " " + apellido + "\n" + telefono).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                                marker.setTag(key);
                                mDriversMarkers.add(marker);
                            }
                            else
                            {
                                Marker marker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Conductor disponible: " + key).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                                marker.setTag(key);
                                mDriversMarkers.add(marker);
                            }




                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Marker marker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Conductor disponible: " + key).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                            marker.setTag(key);
                            mDriversMarkers.add(marker);
                        }
                    });





                }
                catch (Exception e)
                {
                    LatLng driverLatLng = new LatLng(location.latitude, location.longitude);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Conductor disponible" + key).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                    marker.setTag(key);
                    mDriversMarkers.add(marker);
                }


            }

            @Override
            public void onKeyExited(String key) {
                for (Marker marker: mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.remove();
                            mDriversMarkers.remove(marker);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // ACTUALIZAR LA POSICION DE CADA CONDUCTOR
                for (Marker marker: mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}