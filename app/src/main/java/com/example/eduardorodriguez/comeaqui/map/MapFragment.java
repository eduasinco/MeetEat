package com.example.eduardorodriguez.comeaqui.map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.eduardorodriguez.comeaqui.objects.FoodPost;
import com.example.eduardorodriguez.comeaqui.objects.OrderObject;
import com.example.eduardorodriguez.comeaqui.utilities.MyLocation;
import com.example.eduardorodriguez.comeaqui.utilities.UpperNotificationFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.eduardorodriguez.comeaqui.R;
import com.example.eduardorodriguez.comeaqui.server.Server;
import com.example.eduardorodriguez.comeaqui.server.GetAsyncTask;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment{

    MapView mMapView;
    static View rootView;
    private static GoogleMap googleMap;
    public static HashMap<Integer, FoodPost> foodPostHashMap = new HashMap<>();;
    int fabCount;

    ConstraintLayout mapPickerPanView;
    CardView cardView;
    ImageView shadow;
    ImageView hande;
    ImageView shadowPoint;
    TextView pickedAdress;
    FloatingActionButton myFab;
    ImageView cancelPostView;

    double lng;
    double lat;

    Set<Integer> touchedMarkers = new HashSet<>();
    public static HashMap<Integer, Marker> markerHashMap = new HashMap<>();
    LatLng latLng;

    void setMarkers(){
        for (int key : foodPostHashMap.keySet()) {
            FoodPost fp = foodPostHashMap.get(key);
            lat = fp.lat;
            lng = fp.lng;

            Marker marker =  googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng)));
            marker.setTag(key);

            if (fp.favourite){
                markerPutColor(marker, R.color.favourite);
            } else if (touchedMarkers.contains(key)){
                markerPutColor(marker, R.color.grey);
            } else {
                markerPutColor(marker, R.color.colorPrimary);
            }
            markerHashMap.put(fp.id, marker);
        }
    }

    private static BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    void makeList(JsonArray jsonArray, boolean favourite){
        try {
            for (JsonElement pa : jsonArray) {
                JsonObject jo = favourite ? pa.getAsJsonObject().get("post").getAsJsonObject() : pa.getAsJsonObject();
                FoodPost fp = new FoodPost(jo);
                fp.favourite = favourite;
                fp.favouriteId = pa.getAsJsonObject().get("id").getAsInt();
                foodPostHashMap.put(fp.id, fp);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        cancelPost();
        try{setMapMarkers();}catch(Exception ignored){}
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        fabCount = 0;

        mMapView = rootView.findViewById(R.id.mapView);
        myFab =  rootView.findViewById(R.id.fab);
        mapPickerPanView = rootView.findViewById(R.id.map_picker_pan);
        shadow = rootView.findViewById(R.id.shadow);
        hande = rootView.findViewById(R.id.handle);
        shadowPoint = rootView.findViewById(R.id.shadow_point);
        pickedAdress = rootView.findViewById(R.id.pickedAdress);
        cancelPostView = rootView.findViewById(R.id.cancel_post);
        cardView = rootView.findViewById(R.id.card);

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        cancelPostView.setOnClickListener(v -> {
            cancelPost();
        });

        myFab.setOnClickListener(v -> {
            fabFunctionality();
        });

        mMapView.getMapAsync(mMap -> {
            setMap(mMap);
        });


        getFragmentManager().beginTransaction()
                .replace(R.id.upper_notification, UpperNotificationFragment.newInstance())
                .commit();

        listenToChatMessages();
        return rootView;
    }

    public void listenToChatMessages(){
        try {
            URI uri = new URI(getResources().getString(R.string.server) + "/ws/posts/");
            WebSocketClient mWebSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    // runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Unread Messages!", Toast.LENGTH_LONG).show());
                }
                @Override
                public void onMessage(String s) {
                    getActivity().runOnUiThread(() -> {
                        JsonObject jo = new JsonParser().parse(s).getAsJsonObject().get("message").getAsJsonObject().get("food_post").getAsJsonObject();;
                        FoodPost fp = new FoodPost(jo);
                        foodPostHashMap.put(fp.id, fp);

                        Marker marker =  googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(fp.lat, fp.lng)));
                        marker.setTag(fp.id);
                        markerPutColor(marker, fp.favourite ? R.color.favourite : R.color.colorPrimary);
                        markerHashMap.put(fp.id, marker);
                    });
                }
                @Override
                public void onClose(int i, String s, boolean b) {
                    Log.i("Websocket", "Closed " + s);
                }
                @Override
                public void onError(Exception e) {
                    Log.i("Websocket", "Error " + e.getMessage());
                }
            };
            mWebSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("RestrictedApi")
    void setMap(GoogleMap mMap){
        googleMap = mMap;
        googleMap.setMyLocationEnabled(true);

        setLocationPicker();
        setMapMarkers();
        // For dropping a marker at a point on the Map
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                lng = location.getLongitude();
                lat = location.getLatitude();

                LatLng place = new LatLng(lat, lng);
                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(place).zoom(15).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(getContext(), locationResult);


        googleMap.setOnMarkerClickListener(marker -> {

            cardView.setVisibility(View.GONE);
            myFab.setVisibility(View.GONE);
            cancelPostView.setVisibility(View.VISIBLE);

            final int key = (int) (marker.getTag());
            touchedMarkers.add(key);
            FoodPost foodPost = foodPostHashMap.get(key);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.container1, MapCardFragment.newInstance(foodPost))
                    .commit();
            moveCardUp();
            return false;
        });
    }

    void moveCardUp(){
        int move = 200;
        cardView.setScaleX(0);
        cardView.setScaleY(0);
        cardView.setVisibility(View.VISIBLE);
        cardView.animate().scaleY(1).scaleX(1).setDuration(move);
    }

    void moveCardDown(){
        int move = 200;
        cardView.setVisibility(View.VISIBLE);
        cardView.animate().scaleY(0).scaleX(0).setDuration(move).withEndAction(() -> {
            cardView.setVisibility(View.GONE);
        });
    }

    void setMapMarkers(){
        GetAsyncTask getPostLocations = new GetAsyncTask("GET", getResources().getString(R.string.server) + "/foods/");
        try {
            String response = getPostLocations.execute().get();
            if (response != null)
                makeList(new JsonParser().parse(response).getAsJsonArray(), false);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        GetAsyncTask getFavouritePosts = new GetAsyncTask("GET", getResources().getString(R.string.server) + "/my_favourites/");
        try {
            String response = getFavouritePosts.execute().get();
            if (response != null)
                makeList(new JsonParser().parse(response).getAsJsonArray(), true);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        setMarkers();
    }

    void setLocationPicker(){
        googleMap.setOnCameraMoveStartedListener(i -> {
            pickedAdress.setVisibility(View.GONE);
            moveMapPicker(true);
        });
        googleMap.setOnCameraIdleListener(() -> {
            moveMapPicker(false);
            latLng = googleMap.getCameraPosition().target;
            String latLngString = latLng.latitude + "," + latLng.longitude;
            String uri = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latLngString + "&key=" + getResources().getString(R.string.google_key);
            Server gAPI = new Server("GET", uri);
            try {
                String jsonString = gAPI.execute().get(5, TimeUnit.SECONDS);
                if (jsonString != null){
                    JsonObject joo = new JsonParser().parse(jsonString).getAsJsonObject();
                    JsonArray jsonArray = joo.get("results").getAsJsonArray();
                    if (jsonArray.size() > 0) {
                        pickedAdress.setVisibility(View.VISIBLE);
                        pickedAdress.setText(jsonArray.get(0).getAsJsonObject().get("formatted_address").getAsString());
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        });
    }

    void fabFunctionality(){
        apearMapPicker(true);
        if (fabCount == 0){
            markersVisibility(false);
            fabCount = 1;
            cancelPostView.setVisibility(View.VISIBLE);
            switchFabImage(true);
        } else if (fabCount == 1) {
            Intent addFood = new Intent(getActivity(), AddFoodActivity.class);
            addFood.putExtra("address" , pickedAdress.getText().toString());
            addFood.putExtra("lat" , latLng.latitude);
            addFood.putExtra("lng" , latLng.longitude);
            getActivity().startActivity(addFood);
        } else {
            fabCount = 2;
            switchFabImage(false);
        }
    }

    @SuppressLint("RestrictedApi")
    void cancelPost(){
        markersVisibility(true);
        switchFabImage(false);
        fabCount = 0;
        cancelPostView.setVisibility(View.GONE);
        myFab.setVisibility(View.VISIBLE);
        apearMapPicker(false);
        moveCardDown();
    }

    void switchFabImage(boolean toPlus){
        myFab.animate().scaleY(0).setDuration(200).withEndAction(() -> {
            myFab.setImageDrawable(ContextCompat.getDrawable(getActivity(), toPlus ? R.drawable.plus_sign : R.drawable.add_food));
            myFab.animate().scaleY(1).setDuration(200);
        }).start();
    }

    void apearMapPicker(boolean apear){
        shadowPoint.setVisibility(apear ? View.VISIBLE: View.INVISIBLE);
        mapPickerPanView.setVisibility(apear ? View.VISIBLE: View.GONE);
        hande.setVisibility(apear ? View.VISIBLE: View.GONE);
        shadow.setVisibility(apear ? View.VISIBLE: View.GONE);
        shadow.setVisibility(apear ? View.VISIBLE: View.GONE);
    }

    void moveMapPicker(boolean up){
        int move = 50;
        int secs = move * 2;
        if (up) {
            shadowPoint.animate().alpha(0.5f).setDuration(secs);
            mapPickerPanView.animate().translationY(-move).setDuration(secs);
            hande.animate().translationY(-move).setDuration(secs);
            shadow.animate().translationY(-move * 2 / 3).setDuration(secs);
            shadow.animate().translationX(move * 1 / 3).setDuration(secs);
        } else {
            shadowPoint.animate().alpha(0).setDuration(secs);
            mapPickerPanView.animate().translationY(0).setDuration(secs);
            hande.animate().translationY(0).setDuration(secs);
            shadow.animate().translationY(0).setDuration(secs);
            shadow.animate().translationX(0).setDuration(secs);
        }
    }

    void markersVisibility(boolean visible){
        for (Marker marker: markerHashMap.values()){
            marker.setVisible(visible);
        }
    }

    static void markerPutColor(Marker marker, int color){
        Drawable myIcon = rootView.getResources().getDrawable( R.drawable.map_food_icon);
        ColorFilter filter = new LightingColorFilter(
                ContextCompat.getColor(rootView.getContext(), color),
                ContextCompat.getColor(rootView.getContext(), color)
        );
        myIcon.setColorFilter(filter);
        marker.setIcon(getMarkerIconFromDrawable(myIcon));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's state here
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}