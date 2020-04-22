package com.course.example.mapteaser;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;

public class  MapsActivity extends FragmentActivity implements TextToSpeech.OnInitListener, OnMapReadyCallback, View.OnClickListener {

    private ListView list;
    ArrayList myArray = (new ArrayList());
    ArrayAdapter<String> arrayAdapter;
    private final String listFile = "eventlist.txt";
    private String line;
    private OutputStreamWriter out;

    private TextToSpeech speaker;

    private EditText title;
    private EditText location;
    private Button submit;

    private TabHost tabs;

    private GoogleMap mMap;
    private static final LatLng LOWER = new LatLng(42.3835, -71.2246);
    private static final LatLng UPPER = new LatLng(42.3880, -71.2199);
    private static final LatLng BENT = new LatLng(42.3870, -71.2191);

    private static final float zoom = 16.0f;

    private WebView go_webView;
    private ImageView bentleyb;

    private Button dial;

    private final String CHANNEL_ID = "personal_notifications";
    private final int NOTIFICATION_ID = 001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //tab host instead of action bar
        tabs =(TabHost)findViewById(R.id.tabhost);
        tabs.setup();
        TabHost.TabSpec spec;

        // Initialize a TabSpec for tab1 and add it to the TabHost
        spec=tabs.newTabSpec("tag1");
        spec.setContent(R.id.actionbar_tab_1);
        spec.setIndicator("Events");
        tabs.addTab(spec);
        // Initialize a TabSpec for tab2 and add it to the TabHost
        spec=tabs.newTabSpec("tag2");
        spec.setContent(R.id.actionbar_tab_2);
        spec.setIndicator("Add");
        tabs.addTab(spec);
        // Initialize a TabSpec for tab3 and add it to the TabHost
        spec=tabs.newTabSpec("tag3");
        spec.setContent(R.id.actionbar_tab_3);
        spec.setIndicator("Map");
        tabs.addTab(spec);
        // Initialize a TabSpec for tab4 and add it to the TabHost
        spec=tabs.newTabSpec("tag4");
        spec.setContent(R.id.actionbar_tab_4);
        spec.setIndicator("Web");
        tabs.addTab(spec);
        // Initialize a TabSpec for tab4 and add it to the TabHost
        spec=tabs.newTabSpec("tag5");
        spec.setContent(R.id.actionbar_tab_5);
        spec.setIndicator("Dial");
        tabs.addTab(spec);

        ListView list = findViewById(R.id.list);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myArray);

        list.setAdapter(arrayAdapter);

        //if the event is located on upper campus, the map will create a marker up upper campus
        //if the event is located on lower campus, the map will create a marker on lower campus
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(position);
                mMap.clear();
                if (selectedItem.contains("Upper")) {
                    mMap.addMarker(new MarkerOptions()
                            .position(UPPER)
                            .title("Upper Campus")
                            .snippet("Upper Campus")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UPPER, zoom));

                    tabs.setCurrentTab(2);
                }
                if (selectedItem.contains("Lower")) {
                    mMap.addMarker(new MarkerOptions()
                            .position(LOWER)
                            .title("Lower Campus")
                            .snippet("Lower Campus")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LOWER, zoom));
                    tabs.setCurrentTab(2);
                }
            }
        });

        title = (EditText)findViewById(R.id.title);
        location = (EditText)findViewById(R.id.location);
        submit = (Button)findViewById(R.id.submit);
        submit.setOnClickListener(this);

        dial = (Button)findViewById(R.id.dial);
        dial.setOnClickListener(this);

        bentleyb = (ImageView)findViewById(R.id.image);
        bentleyb.setImageResource(R.drawable.one);

        //Initialize Text to Speech engine (context, listener object)
        speaker = new TextToSpeech(this,this);

        InputStream in = null;
        try {
            in = openFileInput(listFile);

            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);
            String str = null;

            while ((str = reader.readLine()) != null) {
                myArray.add(str);
                arrayAdapter.notifyDataSetChanged();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BENT, zoom));

        mMap.addMarker(new MarkerOptions()
                .position(BENT)
                .title("Bentley University"));

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        go_webView = (WebView) findViewById(R.id.go_webview);
        go_webView.getSettings().setJavaScriptEnabled(true);

        //intercept URL loading and load in widget
        go_webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });


        //when a marker is clicked, the student organization website is pulled up in the WebView
        mMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {

                    @Override
                    public boolean onMarkerClick(Marker m) {
                        String title = m.getTitle();
                        String snip = m.getSnippet();
                        Toast.makeText(getApplicationContext(), title + "\n" + snip, Toast.LENGTH_LONG).show();

                        go_webView.loadUrl("https://www.bentley.edu/university-life/campus-life/student-organization-directory");

                        tabs.setCurrentTab(3);
                        return true;
                    }
                }
        );
}

    //the back key navigates back to the previous web page
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == android.view.KeyEvent.KEYCODE_BACK) && go_webView.canGoBack()) {
            go_webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dial:
                didTapButton();
                launchDialer();
                break;
            case R.id.submit:
                displayNotification();
                didTapButton();
                buttonClicked();
                //open output stream to save the contents of the list to the text file
                try {
                    out = new OutputStreamWriter(openFileOutput(listFile, MODE_PRIVATE));
                } catch (IOException e) {
                }

                for (int i = 0; i < myArray.size(); i++) {
                    line = myArray.get(i).toString();
                    try {
                        out.write(line + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void buttonClicked() {
        String newtitle = title.getText().toString();
        String newlocation = location.getText().toString();

        String newnew = newtitle + " is located on " + newlocation;

        myArray.add(newnew);
        arrayAdapter.notifyDataSetChanged();
        speak(newtitle + "was added to the list of events");
        title.setText("");
        title.setHint("Enter Title of Event");
        location.setHint("Upper or Lower Campus");
        arrayAdapter.notifyDataSetChanged();


        tabs.setCurrentTab(0);
    }

    //speak methods will send text to be spoken
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void speak(String output){
        speaker.speak(output, TextToSpeech.QUEUE_FLUSH, null, "Id 0");
    }

    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // Set preferred language to US english.
        int result = speaker.setLanguage(Locale.US);
    }

    //allows dialer to call bentley when the button is clicked this method is activated
    public void launchDialer(){
        String numberToDial = "tel:7818912000";
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(numberToDial)));
    }

    //when user activates this method the button animation will begin
    public void didTapButton() {
        final Animation myAnim = AnimationUtils.loadAnimation(this,R.anim.bounce);
        dial.startAnimation(myAnim);
        submit.startAnimation(myAnim);
    }

    // method called the notifcation to display at the top of the app, user must scroll down to view and clear notification
    public void displayNotification() {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_sms_notification);
        builder.setContentTitle("Event Notification");
        builder.setContentText("An Event has been added to the Event List");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID,builder.build());

    }

    // allows the channel to be created so the notifications can be displayed
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "studentChannel";
            String description = "Channel for student notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
