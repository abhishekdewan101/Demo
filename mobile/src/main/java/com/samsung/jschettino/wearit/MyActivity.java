package com.samsung.jschettino.wearit;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.*;
import com.radiusnetworks.ibeacon.IBeaconManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
//import android.support.v4.app.NotificationCompat.WearableExtender;


public class MyActivity extends Activity implements IBeaconConsumer {
    public static final String EXTRA_EVENT_ID = "eventId";
    private boolean commandIssued = false;
    private String USER_ID;
   // private TextView process;
   private IBeacon oldBeacon;
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        //process = (TextView) findViewById(R.id.process);
        USER_ID = Build.MODEL +"-"+Build.BRAND+"-"+Build.SERIAL;
        iBeaconManager.bind(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        iBeaconManager.unBind(this);
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

    private IBeacon returnClosest(Collection<IBeacon>iBeacons){
        IBeacon tempBeacon = null;
        Iterator<IBeacon>iBeaconIterator = iBeacons.iterator();
        double closest = 100000d;
        while(iBeaconIterator.hasNext()){
            IBeacon iBeacon = iBeaconIterator.next();
            if(iBeacon.getMinor()==319) {
                Log.e("Closest Outer", iBeacon.getMinor() + "-" + iBeacon.getAccuracy());
            }
            if(iBeacon.getAccuracy()<2) {
                if (iBeacon.getAccuracy() < closest) {
                    closest = iBeacon.getAccuracy();
                    tempBeacon = iBeacon;
                    oldBeacon = tempBeacon;
                    Log.e("Closest", iBeacon.getMinor() + "");
                }
            }
        }
        if(tempBeacon == null){
            if(oldBeacon!=null) {
                tempBeacon = oldBeacon;
            }else{
                tempBeacon = new IBeacon("UUUDS",100,1);
            }
        }
        return tempBeacon;
    }

    @Override
    public void onIBeaconServiceConnect() {
            iBeaconManager.setRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                    Iterator<IBeacon> iBeaconIterator = iBeacons.iterator();
                    while(iBeaconIterator.hasNext()){
                        IBeacon temp = iBeaconIterator.next();
                            if(temp.getAccuracy() > 2){
                                setState(true, temp.getMajor(), temp.getMinor());
                                if(temp.getMinor() == 319){
                            Log.e("Beacon Status Accuracy",temp.getAccuracy()+"");
                                if(commandIssued == false) {
                                    sendMessage();
                                    commandIssued = true;
                                }
                            }
                        }
                    }
                }
            });

        try{
            iBeaconManager.startRangingBeaconsInRegion(new Region("myUniqueID",null,null,null));
        }catch (RemoteException e){
            e.printStackTrace();
        }
//          iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
//              @Override
//              public void didEnterRegion(Region region) {
//                  Log.e("Beacon Status", region.toString());
//                  //process.setText("Beacon Detected");
//                  sendMessage();
//              }
//
//              @Override
//              public void didExitRegion(Region region) {
//                  Toast.makeText(getApplicationContext(),"Exited Beacon Region",Toast.LENGTH_LONG).show();
//              }
//
//              @Override
//              public void didDetermineStateForRegion(int i, Region region) {
//
//              }
//          });
//
//        try{
//            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myRegion",null,100,23));
//        }catch (RemoteException e) {
//        }
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(){
        // Do something in response to button
        Log.e("Beacon Status","Got into Send Message");
        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText("Hey, I found a big display in this room. Do you want to use it?");


        int notificationId = 001;
// Build intent for notification content
        Intent viewIntent = new Intent(this, ViewEventActivity.class);
        PendingIntent viewPendingIntent =
        PendingIntent.getActivity(this, 0, viewIntent, 0);

// make second page
        Intent choiceIntent = new Intent(this, ActionEventActivity.class);
        PendingIntent viewShareIntent =
                PendingIntent.getActivity(this, 0, choiceIntent, 0);

// make third page
        Intent stalk = new Intent(this, Stalk.class);
        PendingIntent stalkingIntent =
                PendingIntent.getActivity(this, 0, stalk, 0);
//choiceIntent.putExtra(EXTRA_EVENT_ID, 1); //event_id

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_hardware_dock)
                        .setLargeIcon(BitmapFactory.decodeResource(
                                getResources(), R.drawable.smart_tv))
                        .setContentTitle("Event")
                        .setContentText("Some Text Goes Here")
                        .setContentIntent(viewPendingIntent)
                        .setStyle(bigStyle)
                        .addAction(R.drawable.ic_stat_hardware_dock,
                                getString(R.string.useTv), viewShareIntent)
                        .addAction(R.drawable.powered_by_google_dark,
                getString(R.string.useStalk), stalkingIntent);
// Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

// Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
        Log.e("Beacon Status","Got out Send Message");
    }

    private void setState(boolean active, final int major,final int minor){
        if(active) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String beaconName ="";
                    HttpClient client = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet("http://298a49bb.ngrok.com/beacons?major="+major+"&minor="+minor);
                    StringBuilder builder =new StringBuilder();
                    try {
                        HttpResponse response = client.execute(httpGet);
                        StatusLine statusLine = response.getStatusLine();
                        int statusCode = statusLine.getStatusCode();
                        if (statusCode == 200) {
                            HttpEntity entity = response.getEntity();
                            InputStream content = entity.getContent();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                builder.append(line);
                            }
                        } else {
                            Log.e("ERROR IN READING DATA", "FAILED TO GET ANY DATA");
                        }
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        JSONArray jsonArray = new JSONArray(builder.toString());
                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        beaconName = jsonObject.getString("name");


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                    try {
                        HttpGet httpGet1 = new HttpGet("http://298a49bb.ngrok.com/users/setactive?uniqueid="+ URLEncoder.encode(USER_ID, "utf-8")+"&activebeacon="+URLEncoder.encode(beaconName,"utf-8"));
                        HttpResponse response = client.execute(httpGet1);
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet httpGet1 = new HttpGet("http://298a49bb.ngrok.com/users/setpassive?uniqueid="+USER_ID);
                    try {
                        HttpResponse response = client.execute(httpGet1);
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
