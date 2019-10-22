package com.example.eduardorodriguez.comeaqui.review.food_review_look;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.eduardorodriguez.comeaqui.R;
import com.example.eduardorodriguez.comeaqui.objects.ReviewObject;
import com.example.eduardorodriguez.comeaqui.objects.ReviewReplyObject;
import com.example.eduardorodriguez.comeaqui.server.PostAsyncTask;
import com.example.eduardorodriguez.comeaqui.utilities.ErrorMessageFragment;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ReplyReviewActivity extends AppCompatActivity {

    TextView review;
    TextView usernameTime;
    ImageButton close;
    Button post;
    EditText reply;
    FrameLayout errorMessage;

    ReviewObject reviewObject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_review);

        review = findViewById(R.id.review);
        usernameTime = findViewById(R.id.username_and_time);
        close = findViewById(R.id.close);
        post = findViewById(R.id.post_reply);
        reply = findViewById(R.id.reply);
        errorMessage = findViewById(R.id.error_message);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if(b != null && b.get("review") != null){
            reviewObject = (ReviewObject) b.get("review");

            setThings();
        }
    }

    void setThings(){
        review.setText(reviewObject.review);
        usernameTime.setText(reviewObject.owner.username + " " + reviewObject.createdAt);
        post.setOnClickListener(v -> postReviewReply());
        close.setOnClickListener(v -> finish());
    }

    void postReviewReply(){
        PostAsyncTask createOrder = new PostAsyncTask(getResources().getString(R.string.server) + "/create_review_reply/");
        try {
            String response = createOrder.execute(
                    new String[]{"reply", reply.getText().toString()},
                    new String[]{"review_id", reviewObject.id + ""}
            ).get(5, TimeUnit.SECONDS);
            JsonObject jo = new JsonParser().parse(response).getAsJsonObject();
            ReviewReplyObject reviewObject = new ReviewReplyObject(jo);
            finish();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            showErrorMessage();
        }
    }

    void showErrorMessage(){
        errorMessage.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.error_message_frame, ErrorMessageFragment.newInstance(
                        "Error during posting",
                        "Please make sure that you have connection to the internet"))
                .commit();
    }
}