package com.example.protography.ui.Fragments.AddImage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.protography.BuildConfig;
import com.example.protography.R;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class PlaceSelectFragment extends Fragment implements Step {
    private TextView coords;
    private String coordsValue;
    private GoogleMap map;
    boolean firstMoved = false;
    private SharedPreferences sharedPref;
    private String TAG = "PlaceSelectFragment";



    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            map.setMinZoomPreference(5.0f);
            setMyLocationButton(googleMap);
            int darkMode;

            darkMode = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            // Settaggio della dark mode, se il tema è salvato lo prende, altrimenti, controlla il tema del dispositivo
            if(darkMode == Configuration.UI_MODE_NIGHT_YES || sharedPref.getString("THEME","").equalsIgnoreCase("dark"))
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.map_dark_mode));

            googleMap.setOnCameraMoveListener(() -> {
                firstMoved = true;
                coordsValue = Double.toString(googleMap.getCameraPosition().target.latitude).substring(0,7) + " , " + Double.toString(googleMap.getCameraPosition().target.longitude).substring(0,7);
                coords.setText(coordsValue);
            });
            googleMap.setOnCameraIdleListener(() -> {
                if(firstMoved) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    long latitude =  Double.doubleToLongBits(googleMap.getCameraPosition().target.latitude);
                    long longitude = Double.doubleToLongBits(googleMap.getCameraPosition().target.longitude);
                    String latLng = googleMap.getCameraPosition().target.latitude + "," + googleMap.getCameraPosition().target.longitude;
                    editor.putString("LATLNG", latLng);
                    editor.putLong("LATITUDE", latitude);
                    editor.putLong("LONGITUDE", longitude);
                    editor.apply();
                }
                else
                {
                    LatLng bicocca = new LatLng(45, 9);
                    map.moveCamera(CameraUpdateFactory.newLatLng(bicocca));
                }
            });
        }
    };

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("COORDS", coordsValue);
    }

    @Override
    public void onViewStateRestored(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null)
            coords.setText(savedInstanceState.getString("COORDS", null));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_place_select, container, false);
        coords = v.findViewById(R.id.coords);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        SetSearchBar();
    }

    private void setMyLocationButton(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setPadding(0, 175, 0, 0);
    }

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
            autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS);

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

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull @NotNull VerificationError error) {

    }
}