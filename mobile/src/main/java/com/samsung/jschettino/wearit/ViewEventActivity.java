package com.samsung.jschettino.wearit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

public class ViewEventActivity extends Activity {
    private ServiceParser sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        // dismiss notification
        int notificationId = 001;

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        notificationManager.cancel(notificationId);


        // inflate the dropdown list of options
        Spinner s1 = (Spinner) findViewById(R.id.spinner);

        // get json from Extras/launch
        Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            //int id = b.getInt("Action");
            int id = (Integer) b.get(getString(R.string.extra_action));
            String json =  (String) b.get(getString(R.string.extra_launch));

            Log.i("yo", "Id: " + id + " " + json);

            sp = new ServiceParser(this, json);

            //TextView t=(TextView)findViewById(R.id.description);
            ((TextView)findViewById(R.id.description)).setText(sp.getMessageTag());

            sp.BuildChoiceList(s1);

            s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    Log.i("yo","Position Select: " + position + " id " + id);
                    // your code here
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                    Log.i("yo","Nothing Selected ");
                }

            });
        } else {
            s1.setVisibility(View.GONE); // no spinner
            findViewById(R.id.smartTvButton).setVisibility(View.GONE); //no button
            ((TextView)findViewById(R.id.description)).setText("Something seems to have gone pear shaped.");
        }


    }

    public void onClick(View view) {
        // what do they have selected?
        Spinner s1 = (Spinner) findViewById(R.id.spinner);
        String appname = sp.getAppByOffset(s1.getSelectedItemPosition());

        Intent i = new Intent(this, PerformActionActvity.class);
        // tell it the app to launch...
        i.putExtra(getString(R.string.extra_launch), appname);
        startActivity(i);



    }

    public void useTV(View view){
        // "manually" do what the other view does when opened.
        // learn how intents work
        Intent i = new Intent(this, PerformActionActvity.class);
        startActivity(i);

    }

    public void useStalk(View view){
        // "manually" do what the other view does when opened.
        // learn how intents work
        Intent i = new Intent(this, PerformActionActvity.class);
        startActivity(i);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
