package com.example.protography.ui.Fragments.Search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import com.example.protography.BuildConfig;
import com.example.protography.MainActivity;
import com.example.protography.R;
import com.example.protography.ui.Models.Image;
import com.example.protography.ui.Models.MarkerItem;
import com.example.protography.ui.ViewModels.MapsViewModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment {

    private static final String TAG = "MapsFragment";
    private MapsViewModel mapsViewModel;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap map;
    private LatLng coordinateAttuali;
    private SharedPreferences sharedPreferences;
    private ClusterManager<MarkerItem> clusterManager;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            clusterManager = new ClusterManager<>(getContext(), map);
            map.setOnCameraIdleListener(clusterManager);
            int darkMode;

            darkMode = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            // Settaggio della dark mode, se il tema è salvato lo prende, altrimenti, controlla il tema del dispositivo
            if(darkMode == Configuration.UI_MODE_NIGHT_YES || sharedPreferences.getString("THEME","").equalsIgnoreCase("dark"))
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.map_dark_mode));

            checkLocationPermission();

            if (coordinateAttuali != null)
                map.moveCamera(CameraUpdateFactory.newLatLng(coordinateAttuali));

            mapsViewModel.getImages().observe(getViewLifecycleOwner(), images -> {
                clusterManager.clearItems();
                clusterManager.cluster();
                for (Image image : images) {
                    String[] latlong = image.getCoords().split(",");
                    double latitude = Double.parseDouble(latlong[0]);
                    double longitude = Double.parseDouble(latlong[1]);
                    MarkerItem marker = new MarkerItem(latitude,longitude, image.getImageTitle(), "");
                    clusterManager.addItem(marker);

                }
                clusterManager.cluster();
            });

            //logica cluster -> il cluster è visibile con almeno 5 segnalini vicini
            clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerItem>() {
                @Override
                public boolean onClusterItemClick(MarkerItem item) {
                    String latLng = item.getPosition().latitude + "," + item.getPosition().longitude;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("LATLNG", latLng);
                    editor.apply();

                    ModalBottomSheet bottomSheet = new ModalBottomSheet();

                    bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
                    return true;
                }
            });

            clusterManager.setOnClusterClickListener(cluster -> {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        cluster.getPosition(), (float) Math.floor(map
                                .getCameraPosition().zoom + 2)), 300,
                        null);
                return true;
            });

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mapsViewModel = new ViewModelProvider(requireActivity()).get(MapsViewModel.class);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SetSearchBar();
    }

    //ASK FOR USER'S LOCATION PERMISSION
    public boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
        else {
            coordinateAttuali = null;

            // Imposta le coordinate attuali se la versione sdk è >= 23
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (locationGPS != null) {
                    coordinateAttuali = new LatLng(locationGPS.getLatitude(), locationGPS.getLongitude());
                }
            }
            setMyLocationButton();
        }
        return true;
    }

    //CHECK ANSWER
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)
                        setMyLocationButton();
                }
            }
        }
    }

    //SET THE "MY LOCATION" BUTTON
    @SuppressLint("MissingPermission")
    private void setMyLocationButton(){
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setPadding(0, 205, 0, 0);
    }

    //SET THE SEARCH BAR FOR THE MAP
    private void SetSearchBar(){
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);

            if (!Places.isInitialized())
                Places.initialize(requireContext(), BuildConfig.API_KEY);

            // Initialize the AutocompleteSupportFragment.
            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                    getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

            //filer the type of results
            assert autocompleteFragment != null;
            autocompleteFragment.setTypeFilter(TypeFilter.CITIES);

            // Specify the types of place data to return.
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));

            // Set up a PlaceSelectionListener to handle the response.
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(place.getLatLng().latitude,place.getLatLng().longitude),12.0f));
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.d(TAG, "setOnPlaceSelectedListener - An error occurred: " + status);
                }
            });
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}
