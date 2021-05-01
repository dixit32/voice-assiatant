package com.example.messenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private RecyclerView chatsRV;
    private ImageButton sendMsgIB;
    private EditText userMsgEdt;
    private FloatingActionButton voice_btn;
    private final String USER_KEY = "user";
    private final String BOT_KEY = "bot";
private TextToSpeech textToSpeech;

    // creating a variable for

    // creating a variable for array list and adapter class.
    private ArrayList<MessageModal> messageModalArrayList;
    private static final int REQUEST_CODE_SPEECH_INPUT =100 ;
    private MessageRVAdapter messageRVAdapter;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatsRV = findViewById(R.id.idRVChats);
        voice_btn = findViewById(R.id.voice_btn);
        sendMsgIB = findViewById(R.id.idIBSend);
        userMsgEdt = findViewById(R.id.idEdtMessage);


        // below line is to initialize our request queue.

        // our volley request queue.
        RequestQueue mRequestQueue = Volley.newRequestQueue(MainActivity.this);
        mRequestQueue.getCache().clear();



        // creating a new array list

        messageModalArrayList = new ArrayList<>();

        // adding on click listener for send message button.
        sendMsgIB.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                // checking if the message entered
                // by user is empty or not.
                if (userMsgEdt.getText().toString().isEmpty()) {
                    // if the edit text is empty display a toast message.
                    Toast.makeText(MainActivity.this, "Please enter your message..", Toast.LENGTH_SHORT).show();

                    return;

                }

                // calling a method to send message
                // to our bot to get response.
                sendMessage(userMsgEdt.getText().toString());

                // below line we are setting text in our edit text as empty
                userMsgEdt.setText("");

            }
        });


        voice_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "speak");


                try {
                    startActivityForResult(intent,
                            REQUEST_CODE_SPEECH_INPUT);

                } catch (ActivityNotFoundException e) {

                    Toast.makeText(getApplicationContext(), "sorry your device not support", Toast.LENGTH_SHORT).show();
                }

            }

        });

          textToSpeech =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setLanguage(Locale.US);
            }
        });





        messageRVAdapter = new MessageRVAdapter(messageModalArrayList, this);
    // below line we are creating a variable for our linear layout manager.
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);

    // below line is to set layout
    // manager to our recycler view.

        chatsRV.setLayoutManager(linearLayoutManager);


         // below line we are setting
         // adapter to our recycler view.
        chatsRV.setAdapter(messageRVAdapter);

}


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);


            switch (requestCode) {

                case REQUEST_CODE_SPEECH_INPUT: {
                    if (resultCode == RESULT_OK && null != data) {

                        ArrayList<String> result =  data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                        sendMessage(result.get(0));

                    }
                    break;
                }

            }

        }





        // on below line we are initialing our adapter class and passing our array lit to it.

    private void sendMessage(String userMsg) {

        // below line is to pass message to our

        // array list which is entered by the user.

        messageModalArrayList.add(new MessageModal(userMsg, USER_KEY));
        messageRVAdapter.notifyDataSetChanged();

        // url for our brain

        // make sure to add your url.
        String url = "http://azdhebaryt.pythonanywhere.com/api/v1/users?msg=%22"  + userMsg +"%22" ;

        // creating a variable for our request queue.

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        // at last adding json object
        // request to our queue.


        JsonObjectRequest jsonArrayRequest1=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonArray) {
                try {

                    // in on response method we are extracting data

                    // from json response and adding this response to our array list.

                    String botResponse = jsonArray.getString("msg");

                    textToSpeech.speak(botResponse,TextToSpeech.QUEUE_FLUSH,null);

                    messageModalArrayList.add(new MessageModal(botResponse, BOT_KEY));

                    // notifying our adapter as data changed.

                    messageRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {

                    e.printStackTrace();

                    // handling error response from bot.

                    messageModalArrayList.add(new MessageModal("No response", BOT_KEY));

                    messageRVAdapter.notifyDataSetChanged();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // error handling.
                Log.d("hello7777777777",error.toString());
                messageModalArrayList.add(new MessageModal("Sorry no response found", BOT_KEY));

                Toast.makeText(MainActivity.this, "No response from the bot..", Toast.LENGTH_SHORT).show();

            }
        });

        jsonArrayRequest1.setRetryPolicy(new DefaultRetryPolicy(6000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(jsonArrayRequest1);

    }
}








