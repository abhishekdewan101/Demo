package com.samsung.jschettino.wearit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.jschettino.wearit.R;
import com.samsung.multiscreen.application.Application;
import com.samsung.multiscreen.application.ApplicationAsyncResult;
import com.samsung.multiscreen.application.ApplicationError;
import com.samsung.multiscreen.device.Device;
import com.samsung.multiscreen.device.DeviceAsyncResult;
import com.samsung.multiscreen.device.DeviceError;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewEventActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        // dismiss notification
        int notificationId = 001;
        useTV();
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        notificationManager.cancel(notificationId);
    }

    public void useTV(){
        // "manually" do what the other view does when opened.
        // learn how intents work

        //TV Opening Code
        Toast.makeText(getApplicationContext(),"Starting Application",Toast.LENGTH_LONG).show();
        Device.search(new DeviceAsyncResult<List<Device>>() {
            @Override
            public void onResult(List<Device> devices) {
                final Device device = devices.iterator().next();
                device.getApplication("smarthome", new DeviceAsyncResult<Application>() {
                    @Override
                    public void onResult(Application application) {
                        Map<String, String> parameters = new HashMap<String, String>();
                        parameters.put("launchby", "mobile");
                        application.launch(parameters, new ApplicationAsyncResult<Boolean>() {
                            @Override
                            public void onResult(final Boolean aBoolean) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"Application Launched",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(ApplicationError applicationError) {

                            }
                        });
                    }

                    @Override
                    public void onError(DeviceError deviceError) {

                    }
                });
            }

            @Override
            public void onError(DeviceError deviceError) {

            }
        });

        Intent i = new Intent(this, ActionEventActivity.class);
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
