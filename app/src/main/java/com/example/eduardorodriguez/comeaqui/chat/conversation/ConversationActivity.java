package com.example.eduardorodriguez.comeaqui.chat.conversation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eduardorodriguez.comeaqui.MainActivity;
import com.example.eduardorodriguez.comeaqui.R;
import com.example.eduardorodriguez.comeaqui.chat.ChatObject;
import com.example.eduardorodriguez.comeaqui.chat.MessageObject;
import com.example.eduardorodriguez.comeaqui.objects.User;
import com.example.eduardorodriguez.comeaqui.server.GetAsyncTask;
import com.example.eduardorodriguez.comeaqui.utilities.DateFragment;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.*;
import okio.ByteString;

import java.util.concurrent.ExecutionException;

public class ConversationActivity extends AppCompatActivity {

    ChatObject chat;
    User chattingWith;

    MessageObject lastMessage = null;
    MessageObject lastBrandNewMessage = null;
    String lastMessageDate = "";


    private CircleImageView fotoPerfil;
    private TextView nombre;
    private RecyclerView rvMensajes;
    private EditText txtMensaje;
    private ImageView btnEnviar;
    private ImageView backView;
    private AdapterMensajes adapter;

    private OkHttpClient client;

    WebSocket ws;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        fotoPerfil = findViewById(R.id.dinner_image);
        nombre = findViewById(R.id.nombre);
        rvMensajes = findViewById(R.id.rvMensajes);
        txtMensaje = findViewById(R.id.txtMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);
        backView = findViewById(R.id.back);

        adapter = new AdapterMensajes(this);
        LinearLayoutManager l = new LinearLayoutManager(this);
        rvMensajes.setLayoutManager(l);
        rvMensajes.setAdapter(adapter);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                setScrollbar();
            }
        });

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if(b != null) {
            chat = (ChatObject) b.get("chat");
            chattingWith = MainActivity.user.id == (chat.users.get(0).id) ? chat.users.get(1) : chat.users.get(0);

            nombre.setText(chattingWith.first_name + " " + chattingWith.last_name);
            getChatMessages();
        }
        btnEnviar.setOnClickListener(view -> {
            //createServerMessage();
            ws.send("{ \"message\": \"" + txtMensaje.getText().toString() + "\"," +
                    "\"command\": \"new_message\"," +
                    "\"from\": \"" + MainActivity.user.id + "\"," +
                    "\"chatId\": \"" + chat.id + "\"}"
            );
            txtMensaje.setText("");
        });

        btnEnviar.setScaleX(0);
        btnEnviar.setVisibility(View.GONE);
        txtMensaje.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtMensaje.getText().toString().trim().length() > 0){
                    btnEnviar.setVisibility(View.VISIBLE);
                    btnEnviar.animate().scaleX(1).setDuration(200);
                } else {
                    btnEnviar.animate().scaleX(0).setDuration(200).withEndAction(() -> btnEnviar.setVisibility(View.GONE));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        rvMensajes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    hideKeyboard();
                }
            }
        });

        backView.setOnClickListener(v -> finish());
        client = new OkHttpClient();
        start();
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void getChatMessages(){
        GetAsyncTask process = new GetAsyncTask("GET", getResources().getString(R.string.server) + "/chat_detail/" + chat.id + "/");
        try {
            String response = process.execute().get();
            if (response != null) {
                for (JsonElement je: new JsonParser().parse(response).getAsJsonObject().get("message_set").getAsJsonArray()){
                    MessageObject currentMessage = new MessageObject(je.getAsJsonObject());

                    setMessageMode(currentMessage);

                    adapter.addMensaje(currentMessage);
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setMessageMode(MessageObject currentMessage){

        String currentMessageDate = DateFragment.getDateInSimpleFormat(currentMessage.createdAt);
        if (!lastMessageDate.equals(currentMessageDate)){
            currentMessage.newDay = true;
        }
        lastMessageDate = currentMessageDate;

        if (lastMessage == null){
            currentMessage.topSpace = true;
        } else if (currentMessage.sender.id != lastMessage.sender.id){
            currentMessage.topSpace = true;
            lastMessage.lastInGroup = true;
        }

        if (MainActivity.user.id == currentMessage.sender.id){
            currentMessage.isOwner = true;
        }

        lastMessage = currentMessage;
    }


    private void setScrollbar(){
        rvMensajes.scrollToPosition(adapter.getItemCount() - 1);
    }


    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        public ConversationActivity activity;
        public EchoWebSocketListener(ConversationActivity activity) {
            this.activity = activity;
        }
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            activity.runOnUiThread(() -> Toast.makeText(activity, "Connection Established!", Toast.LENGTH_LONG).show());
        }
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            output(text);
        }
        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            output(bytes.hex());
        }
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            output("Closing : " + code + " / " + reason);
        }
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            output("Error : " + t.getMessage());
        }
    }

    private void start() {
        Request request = new Request.Builder().url(getResources().getString(R.string.server) + "/ws/chat/" + chat.id + "/")
                .build();
        EchoWebSocketListener listener = new EchoWebSocketListener(this);
        ws = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }

    private void output(final String txt) {
        runOnUiThread(() -> {
            MessageObject brandNewMessage = new MessageObject(new JsonParser().parse(txt).getAsJsonObject().get("message").getAsJsonObject());
            if (lastBrandNewMessage != null){
                lastBrandNewMessage.lastInGroup = false;
            }
            brandNewMessage.lastInGroup = true;
            brandNewMessage.isOwner = true;
            adapter.addMensaje(brandNewMessage);
            lastBrandNewMessage = brandNewMessage;
        });
    }

}
