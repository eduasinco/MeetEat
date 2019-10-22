package com.example.eduardorodriguez.comeaqui.general;

import android.content.Context;
import android.content.Intent;

import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.eduardorodriguez.comeaqui.R;
import com.example.eduardorodriguez.comeaqui.objects.FoodPostDetail;
import com.example.eduardorodriguez.comeaqui.objects.User;
import com.example.eduardorodriguez.comeaqui.objects.FoodPost;
import com.example.eduardorodriguez.comeaqui.objects.OrderObject;
import com.example.eduardorodriguez.comeaqui.order.OrderLookActivity;
import com.example.eduardorodriguez.comeaqui.profile.ProfileViewActivity;
import com.example.eduardorodriguez.comeaqui.profile.edit_profile.edit_account_details.payment.PaymentMethodsActivity;
import com.example.eduardorodriguez.comeaqui.server.GetAsyncTask;
import com.example.eduardorodriguez.comeaqui.server.Server;
import com.example.eduardorodriguez.comeaqui.server.PostAsyncTask;
import com.example.eduardorodriguez.comeaqui.utilities.ErrorMessageFragment;
import com.example.eduardorodriguez.comeaqui.utilities.FoodTypeFragment;
import com.example.eduardorodriguez.comeaqui.utilities.ImageLookActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.example.eduardorodriguez.comeaqui.App.USER;

public class FoodLookActivity extends AppCompatActivity {

    static Context context;

    TextView plateNameView;
    TextView descriptionView;
    TextView priceView;
    TextView timeView;
    TextView usernameView;
    TextView posterNameView;
    TextView posterLocationView;
    TextView changePaymentMethod;
    Button placeOrderButton;

    ImageView postImage;
    ImageView posterImage;
    ImageView staticMapView;
    View backView;
    LinearLayout paymentMethod;
    CardView postImageLayout;
    View placeOrderProgress;
    FrameLayout placeOrderErrorMessage;
    LinearLayout dinnersListView;
    ImageView[] dinnerArray;

    FoodPostDetail foodPostDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_look);
        context = getApplicationContext();

        plateNameView = findViewById(R.id.postPlateName);
        descriptionView = findViewById(R.id.post_description);
        priceView = findViewById(R.id.price);
        timeView = findViewById(R.id.time);
        placeOrderButton = findViewById(R.id.placeOrderButton);
        usernameView = findViewById(R.id.username);
        posterNameView = findViewById(R.id.poster_name);
        posterLocationView = findViewById(R.id.posterLocation);

        postImage = findViewById(R.id.post_image);
        posterImage = findViewById(R.id.poster_image);
        staticMapView = findViewById(R.id.static_map);
        postImageLayout = findViewById(R.id.image_layout);
        backView = findViewById(R.id.back);
        paymentMethod = findViewById(R.id.payment_method_layout);
        changePaymentMethod = findViewById(R.id.change_payment);
        placeOrderProgress = findViewById(R.id.place_order_progress);
        placeOrderErrorMessage = findViewById(R.id.place_order_error_message);
        dinnersListView = findViewById(R.id.dinners_list_view);


        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if(b != null && b.get("object") != null){
            FoodPost fp = (FoodPost) b.get("object");

            getFoodPostDetailsAndSet(fp.id);
        }
        changePaymentMethod.setOnClickListener(v -> {
            Intent paymentMethod = new Intent(this, PaymentMethodsActivity.class);
            startActivity(paymentMethod);
        });
        backView.setOnClickListener(v -> finish());
    }

    void setDetails(){
        posterNameView.setText(foodPostDetail.owner.first_name + " " + foodPostDetail.owner.last_name);
        usernameView.setText(foodPostDetail.owner.username);
        plateNameView.setText(foodPostDetail.plate_name);
        descriptionView.setText(foodPostDetail.description);
        posterLocationView.setText(foodPostDetail.address);
        priceView.setText(foodPostDetail.price + "$");
        timeView.setText(foodPostDetail.time);

        Bundle bundle = new Bundle();
        bundle.putSerializable("type", foodPostDetail.type);
        FoodTypeFragment fragment = new FoodTypeFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.types, fragment)
                .commit();


        if(!foodPostDetail.owner.profile_photo.contains("no-image")) {
            Glide.with(this).load(foodPostDetail.owner.profile_photo).into(posterImage);
            posterImage.setOnClickListener(v -> goToProfileView(foodPostDetail.owner));
        }
        if(!foodPostDetail.food_photo.contains("no-image")){
            postImageLayout.setVisibility(View.VISIBLE);
            Glide.with(this).load(foodPostDetail.food_photo).into(postImage);
            postImageLayout.setOnClickListener((v) -> {
                Intent imageLook = new Intent(this, ImageLookActivity.class);
                imageLook.putExtra("image_url", foodPostDetail.food_photo);
                startActivity(imageLook);
            });
        }
        String url = "http://maps.google.com/maps/api/staticmap?center=" + foodPostDetail.lat + "," + foodPostDetail.lng + "&zoom=15&size=" + 300 + "x" + 200 +"&sensor=false&key=" + getResources().getString(R.string.google_key);
        Glide.with(this).load(url).into(staticMapView);

        setPlaceButton();
        setDinners();
    }

    void setDinners(){
        dinnersListView.setOnClickListener((v) -> {
            startActivity(new Intent(this, DinnersListActivity.class).putExtra("foodPostId", foodPostDetail.id));
        });
        dinnerArray = new ImageView[]{
                findViewById(R.id.dinner0),
                findViewById(R.id.dinner1),
                findViewById(R.id.dinner2),
                findViewById(R.id.dinner3),
                findViewById(R.id.dinner4),
                findViewById(R.id.dinner5),
                findViewById(R.id.dinner6),
                findViewById(R.id.dinner7)
        };
        TextView dinnersNumber = findViewById(R.id.dinners_number);
        dinnersNumber.setText(foodPostDetail.confirmedOrdersList.size() + "/" + foodPostDetail.max_dinners + " dinners for this meal");
        int i = 0;
        while (i < dinnerArray.length && i < foodPostDetail.confirmedOrdersList.size()){
            if(!foodPostDetail.confirmedOrdersList.get(i).owner.profile_photo.contains("no-image")) {
                Glide.with(this).load(foodPostDetail.confirmedOrdersList.get(i).owner.profile_photo).into(dinnerArray[i]);
            }
            dinnerArray[i].setVisibility(View.VISIBLE);
            i++;
        }
    }

    void getFoodPostDetailsAndSet(int foodPostId){
        new GetAsyncTask("GET", context.getResources().getString(R.string.server) + "/foods/" + foodPostId + "/"){
            @Override
            protected void onPostExecute(String response) {
                if (response != null){
                    foodPostDetail = new FoodPostDetail(new JsonParser().parse(response).getAsJsonObject());
                    setDetails();
                }
                super.onPostExecute(response);
            }
        }.execute();
    }

    void goToProfileView(User user){
        Intent k = new Intent(this, ProfileViewActivity.class);
        k.putExtra("user", user);
        startActivity(k);
    }

    void showProgress(boolean show){
        if (show){
            placeOrderButton.setVisibility(View.GONE);
            placeOrderProgress.setVisibility(View.VISIBLE);
        } else {
            placeOrderButton.setVisibility(View.VISIBLE);
            placeOrderProgress.setVisibility(View.GONE);
        }
    }

    void setPlaceButton(){
        if (foodPostDetail.owner.id == USER.id){
            if (foodPostDetail.confirmedOrdersList.size() > 0){
                placeOrderButton.setText("Post confirmed");
                placeOrderButton.setBackgroundColor(Color.TRANSPARENT);
                placeOrderButton.setTextColor(ContextCompat.getColor(this, R.color.success));
            } else {
                placeOrderButton.setText("Delete Post");
                placeOrderButton.setBackgroundColor(ContextCompat.getColor(this, R.color.canceled));
                placeOrderButton.setOnClickListener(v -> {
                    showProgress(true);
                    deleteOrder();
                });
            }
            paymentMethod.setVisibility(View.GONE);
        }else{
            placeOrderButton.setOnClickListener(v -> {
                showProgress(true);
                createOrder();
            });
        }
    }

    void deleteOrder(){
        Server deleteFoodPost = new Server("DELETE", getResources().getString(R.string.server) + "/foods/" + foodPostDetail.id + "/");
        try {
            deleteFoodPost.execute().get();
            finish();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            showErrorMessage();
        }
    }

    void createOrder(){
        PostAsyncTask createOrder = new PostAsyncTask(getResources().getString(R.string.server) + "/create_order_and_notification/");
        try {
            String response = createOrder.execute(
                    new String[]{"food_post_id", "" + foodPostDetail.id}
            ).get(5, TimeUnit.SECONDS);
            JsonObject jo = new JsonParser().parse(response).getAsJsonObject().get("order").getAsJsonObject();
            OrderObject orderObject = new OrderObject(jo);
            goToOrder(orderObject);
            finish();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            showErrorMessage();
            showProgress(false);
        }
    }

    void goToOrder(OrderObject orderObject){
        try{
            Intent goToOrders = new Intent(context, OrderLookActivity.class);
            goToOrders.putExtra("object", orderObject);
            context.startActivity(goToOrders);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void showErrorMessage(){
        placeOrderErrorMessage.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.error_message_frame, ErrorMessageFragment.newInstance(
                        "Error during posting",
                        "Please make sure that you have connection to the internet"))
                .commit();
    }
}