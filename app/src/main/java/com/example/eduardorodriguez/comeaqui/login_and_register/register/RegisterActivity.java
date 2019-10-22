package com.example.eduardorodriguez.comeaqui.login_and_register.register;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eduardorodriguez.comeaqui.R;
import com.example.eduardorodriguez.comeaqui.objects.User;
import com.example.eduardorodriguez.comeaqui.utilities.ImageLookActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // String name, surname, phoneNumber, password, email;

    ImageView backArrow;
    EditText username;
    EditText name;
    EditText surname;
    Button emailButton;
    Button phoneButton;
    EditText email;
    CountryCodePicker ccp;
    EditText phone;
    ConstraintLayout phoneWhole;
    EditText password;
    TextView validationText;
    Button registerButton;
    View progress;

    TextView emailValtext;
    TextView nameValtext;
    TextView surnameValtext;
    TextView usernameValtext;
    TextView passwordValtext;
    TextView phoneValtext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        backArrow = findViewById(R.id.back_arr);
        username = findViewById(R.id.username);
        name = findViewById(R.id.name_name);
        surname = findViewById(R.id.surname);
        emailButton = findViewById(R.id.email_button);
        phoneButton = findViewById(R.id.phone_button);
        email = findViewById(R.id.email);
        ccp = findViewById(R.id.ccp);
        phone = findViewById(R.id.phone_number_edt);
        phoneWhole = findViewById(R.id.phone_whole);
        password = findViewById(R.id.password);
        validationText = findViewById(R.id.validation_text);
        registerButton = findViewById(R.id.register_button);
        progress = findViewById(R.id.register_progress);

        emailValtext = findViewById(R.id.email_valtext);
        nameValtext = findViewById(R.id.name_valtext);
        surnameValtext = findViewById(R.id.surname_valtext);
        passwordValtext = findViewById(R.id.password_valtext);
        usernameValtext = findViewById(R.id.username_valtext);
        phoneValtext = findViewById(R.id.phone_valtext);

        email.setVisibility(View.GONE);
        phoneWhole.setVisibility(View.GONE);


        setEditText(username, usernameValtext);
        setEditText(name, nameValtext);
        setEditText(surname, surnameValtext);
        setEditText(email, emailValtext);
        setEditText(phone, phoneValtext);
        setEditText(password, passwordValtext);
        setEmailOrPhone();

        registerButton.setOnClickListener((v) -> register());
    }

    void register(){
        if (valid()){
            submit();
        }
    }

    void showProgress(boolean show){
        if (show){
            progress.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.GONE);
        } else {
            progress.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
        }
    }

    void submit(){
        RegisterAsyncTask post = new RegisterAsyncTask(getResources().getString(R.string.server) + "/register/");
        showProgress(true);
        try {
            String response = post.execute(
                    new String[]{"username", username.getText().toString()},
                    new String[]{"first_name", name.getText().toString()},
                    new String[]{"last_name", surname.getText().toString()},
                    new String[]{"email", email.getText().toString()},
                    new String[]{"phone_code", ccp.getFullNumber()},
                    new String[]{"phone_number", phone.getText().toString()},
                    new String[]{"password", password.getText().toString()}
            ).get();
            JsonObject jo = new JsonParser().parse(response).getAsJsonObject();
            try{
                User newUser = new User(jo);
                Intent imageLook = new Intent(this, VerifyEmailActivity.class);
                imageLook.putExtra("user", newUser);
                startActivity(imageLook);
            } catch (Exception e){
                if (jo != null && jo.get("username") != null){
                    showValtext(usernameValtext, jo.get("username").getAsJsonArray().get(0).getAsString(), username);
                }
                if (jo != null && jo.get("email") != null){
                    showValtext(emailValtext, jo.get("email").getAsJsonArray().get(0).getAsString(), email);
                }
                validationText.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setEmailOrPhone(){
        emailButton.setOnClickListener(v -> {
            emailButton.setBackgroundColor(Color.TRANSPARENT);
            phoneButton.setBackground(ContextCompat.getDrawable(getApplication(), R.drawable.text_input_shape));
            phoneWhole.setVisibility(View.GONE);
            email.setVisibility(View.VISIBLE);
        });

        phoneButton.setOnClickListener(v -> {
            emailButton.setBackground(ContextCompat.getDrawable(getApplication(), R.drawable.text_input_shape));
            phoneButton.setBackgroundColor(Color.TRANSPARENT);
            phoneWhole.setVisibility(View.VISIBLE);
            email.setVisibility(View.GONE);
        });
    }

    void showValtext(TextView tv, String text, EditText et){
        tv.setText(text);
        tv.setVisibility(View.VISIBLE);
        et.setBackground(ContextCompat.getDrawable(getApplication(), R.drawable.text_input_shape_error));
    }

    boolean valid(){
        boolean valid = true;

        if (TextUtils.isEmpty(username.getText().toString())){
            showValtext(usernameValtext, "Please, insert a username ", username);
            valid = false;
        }

        if (TextUtils.isEmpty(name.getText().toString())){
            showValtext(nameValtext, "Please, insert a name", name);
            valid = false;
        }

        if (TextUtils.isEmpty(surname.getText().toString())){
            showValtext(surnameValtext, "Please, insert a surname", surname);
            valid = false;
        }
        String target = email.getText().toString();
        if (!(!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches())){
            showValtext(emailValtext, "Not a valid email", email);
            valid = false;
        }
        if (phone.getText().toString() == null || phone.getText().toString().trim().equals("")){
            showValtext(phoneValtext,  "Please, insert a phone number", phone);
            valid = false;
        }
        Pattern p = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$");
        Matcher m = p.matcher(password.getText().toString());
        if (false && !m.find()){
            String text = "A digit must occur at least once \n" +
                    "A lower case letter must occur at least once \n" +
                    "An upper case letter must occur at least once \n" +
                    "A special character (!?@#$%^&+=) must occur at least once \n" +
                    "No whitespace allowed in the entire string \n";
            showValtext(passwordValtext, text, password);
            password.setBackground(ContextCompat.getDrawable(getApplication(), R.drawable.text_input_shape_error));
            valid = false;
        }
        if (!valid){
            validationText.setVisibility(View.VISIBLE);
        } else {
            validationText.setVisibility(View.GONE);
        }
        return valid;
    }

    void setEditText(EditText editText, TextView valtext){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setBackground(ContextCompat.getDrawable(getApplication(), R.drawable.text_input_shape));
                valtext.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public class RegisterAsyncTask extends AsyncTask<String[], Void, String>
    {
        public RegisterAsyncTask(String uri){
            this.uri = uri;
        }
        String uri;
        public Bitmap bitmap;
        @Override
        protected String doInBackground(String[]... params)
        {

            HttpPost httpPost = new HttpPost(uri);
            HttpClient httpclient = new DefaultHttpClient();
            String boundary = "-------------" + System.currentTimeMillis();

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .setBoundary(boundary);

            for(String[] ss: params){
                if (ss[0].equals("food_photo") && bitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] imageBytes = baos.toByteArray();
                    entityBuilder.addPart(ss[0], new ByteArrayBody(imageBytes, "ANDROID.png"));
                } else {
                    entityBuilder.addPart(ss[0], new StringBody(ss[1], ContentType.TEXT_PLAIN));
                }
            }

            HttpEntity entity = entityBuilder.build();

            httpPost.setEntity(entity);
            try {
                HttpResponse response = httpclient.execute(httpPost);
                InputStream instream = response.getEntity().getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(instream));
                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuffer.append(line);
                }
                return stringBuffer.toString();

            }  catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            showProgress(false);
            if(response != null) {}
        }
    }
}