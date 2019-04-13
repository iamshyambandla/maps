package com.akhila.amaps;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.GeoJSONObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.feedback.FeedbackBottomSheetListener;
import com.mapbox.services.android.navigation.ui.v5.feedback.FeedbackClickListener;
import com.mapbox.services.android.navigation.ui.v5.feedback.FeedbackItem;
import com.mapbox.services.android.navigation.ui.v5.listeners.FeedbackListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.RouteListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.amirs.JSON;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class NavFrag extends Fragment implements OnNavigationReadyCallback, NavigationListener, ProgressChangeListener, RouteListener, FeedbackListener, FeedbackBottomSheetListener {

    private double ORIGIN_LONGITUDE;
    private double ORIGIN_LATITUDE ;
    private  double DESTINATION_LONGITUDE;
    private  double DESTINATION_LATITUDE;
    private DatabaseReference databaseReference;
    private TextToSpeech speech;
    private double latitude,longitude;
    String resp,sresp;
    private List<Double> lats,langs,slats,slangs;
    private AlertDialog.Builder builder;
    private NavigationView navigationView;
    private DirectionsRoute directionsRoute;
    public NavFrag() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_nav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toast.makeText(getContext(), "nav", Toast.LENGTH_SHORT).show();
        FirebaseApp.initializeApp(getContext());
        navigationView=(NavigationView)view.findViewById(R.id.navigation_view_fragment);
        navigationView.onCreate(savedInstanceState);
        lats=new ArrayList<>();
        langs=new ArrayList<>();
        slats=new ArrayList<>();
        slangs=new ArrayList<>();
        databaseReference= FirebaseDatabase.getInstance().getReference();
        speech=new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                speech.setLanguage(Locale.ENGLISH);
            }
        });
        getresponse();
        navigationView.initialize(this::onNavigationReady);
        ORIGIN_LATITUDE=getArguments().getDouble("orglat",0.000000000000000);
        ORIGIN_LONGITUDE=getArguments().getDouble("orglang",0.000000000000000);
        DESTINATION_LATITUDE=getArguments().getDouble("destlat",0.000000000000000);
        DESTINATION_LONGITUDE=getArguments().getDouble("destlang",0.000000000000000);

    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        Point origin = Point.fromLngLat(ORIGIN_LONGITUDE, ORIGIN_LATITUDE);
        Point destination = Point.fromLngLat(DESTINATION_LONGITUDE, DESTINATION_LATITUDE);
        fetchRoute(origin, destination);

    }
    private void fetchRoute(Point origin, Point destination) {
        NavigationRoute.builder(getContext())
                .accessToken(getString(R.string.access_token))
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        directionsRoute = response.body().routes().get(0);
                        startNavigation();

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                    }
                });
    }
    @Override
    public void onCancelNavigation() {

    }
    private void startNavigation() {

        if (directionsRoute == null) {
            return;
        }

        NavigationViewOptions options = NavigationViewOptions.builder()
                .directionsRoute(directionsRoute)
                .shouldSimulateRoute(true)
                .navigationListener(NavFrag.this)
                .progressChangeListener(this)
                .routeListener(this)
                .build();
        navigationView.startNavigation(options);
    }

    @Override
    public void onNavigationFinished() {

    }
    private void  getresponse(){
        MyTask task=new MyTask();
        task.execute();



    }

    @Override
    public void onFeedbackOpened() {

    }

    @Override
    public void onFeedbackCancelled() {

    }

    @Override
    public void onFeedbackSent(FeedbackItem feedbackItem) {

    }

    @Override
    public void onFeedbackSelected(FeedbackItem feedbackItem) {
        Feed f=new Feed(latitude,longitude,feedbackItem.getFeedbackType());
        databaseReference=FirebaseDatabase.getInstance().getReference();
        databaseReference.push().setValue(f);
        Toast.makeText(getContext(), feedbackItem.getFeedbackType(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFeedbackDismissed() {

    }

    public class MyTask extends AsyncTask<String,Void,String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            JSON json=new JSON(s);
            int j=json.key("features").count();
            try {
                for(int i=0;i<j;i++) {
                    double lat = json.key("features").index(i).key("geometry").key("coordinates").getJsonArray().getDouble(0);
                    double lang = json.key("features").index(i).key("geometry").key("coordinates").getJsonArray().getDouble(1);
                    lats.add(lat);
                    langs.add(lang);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(String... strings) {
            JsonParser parser=new JsonParser();
            resp=parser.makeServiceCall("http://13.232.24.83/2019_projects/Office/DoorLock/maps.geojson");
            sresp=parser.makeServiceCall("http://13.232.24.83/2019_projects/Office/DoorLock/notsafe.geojson");
            JSON json=new JSON(sresp);
            int j=json.key("features").count();
            try {
                for(int i=0;i<j;i++) {
                    double slat = json.key("features").index(i).key("geometry").key("coordinates").getJsonArray().getDouble(0);
                    double slang = json.key("features").index(i).key("geometry").key("coordinates").getJsonArray().getDouble(1);
                    slats.add(slat);
                    slangs.add(slang);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return resp;
        }
    }

    @Override
    public void onNavigationRunning() {

    }

    @Override
    public boolean allowRerouteFrom(Point offRoutePoint) {
        return false;
    }

    @Override
    public void onOffRoute(Point offRoutePoint) {

    }

    @Override
    public void onRerouteAlong(DirectionsRoute directionsRoute) {

    }

    @Override
    public void onFailedReroute(String errorMessage) {

    }

    @Override
    public void onArrival() {

    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        float[] distance=new float[2];
        latitude=location.getLatitude();
        longitude=location.getLongitude();
        for (int i=0;i<lats.size();i++){
            Location.distanceBetween(location.getLatitude(),location.getLongitude(),langs.get(i),lats.get(i),distance);
            if (Math.round(distance[0])<5){
                builder= new AlertDialog.Builder(getContext());
                builder.setMessage("Dangerzone Ahead");
                builder.setIcon(R.drawable.warning);
                builder.setTitle("Danger");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
                speech.speak("danger zone Ahead",TextToSpeech.QUEUE_FLUSH,null);
            }
        }
        float[] sdistance=new float[2];
        for (int i=0;i<slats.size();i++){
            Location.distanceBetween(location.getLatitude(),location.getLongitude(),slangs.get(i),slats.get(i),sdistance);
            if (Math.round(sdistance[0])<5){
                builder= new AlertDialog.Builder(getContext());
                builder.setMessage("Non safe zone Be cautious");
                builder.setIcon(R.drawable.notsafe);
                builder.setTitle("Not Safe");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
                speech.speak("non safe zone ahead be cautious",TextToSpeech.QUEUE_FLUSH,null);
            }
        }

    }

}
