package com.samsung.jschettino.wearit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.samsung.multiscreen.application.Application;
import com.samsung.multiscreen.application.ApplicationAsyncResult;
import com.samsung.multiscreen.application.ApplicationError;
import com.samsung.multiscreen.channel.Channel;
import com.samsung.multiscreen.device.Device;
import com.samsung.multiscreen.device.DeviceAsyncResult;
import com.samsung.multiscreen.device.DeviceError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by a.dewan on 7/18/14.
 */
public class PerformActionActvity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_event);
        int notificationId = 001;
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        notificationManager.cancel(notificationId);

        // get json from Extras/launch
        Intent iin= getIntent();
        Bundle b = iin.getExtras();

        String app = null;

        if(b!=null) {
            app = (String) b.get(getString(R.string.extra_launch));

            Log.i("yo", "app " + app);
        }

        use(app);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_event, menu);
        return true;
    }

    public void use(final String app){

        //TV Opening Code
        Toast.makeText(getApplicationContext(), "Starting Application " +  ((app != null) ? app : "smarthome2"), Toast.LENGTH_LONG).show();
        Device.search(new DeviceAsyncResult<List<Device>>() {
            @Override
            public void onResult(List<Device> devices) {
                final Device device = devices.iterator().next();
                device.getApplication((app != null) ? app : "smarthome2", new DeviceAsyncResult<Application>() {
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
                                        Toast.makeText(getApplicationContext(), "Application " +  ((app != null) ? app : "smarthome2") + " Launched", Toast.LENGTH_LONG).show();
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

    }

    public void closeTV(View view){
        Log.e("Beacon Status", "Entered Close");
        Device.search(new DeviceAsyncResult<List<Device>>() {
            @Override
            public void onResult(List<Device> devices) {
                Device device = devices.iterator().next();
                String channelID = "myChannelID";
                Map<String,String> clientAttributes = new HashMap<String, String>();
                clientAttributes.put("name","Mobile Client");
                device.connectToChannel(channelID,clientAttributes,new DeviceAsyncResult<Channel>() {
                    @Override
                    public void onResult(Channel channel) {
                        channel.broadcast("Hello Everyone except me");
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
    }
}
