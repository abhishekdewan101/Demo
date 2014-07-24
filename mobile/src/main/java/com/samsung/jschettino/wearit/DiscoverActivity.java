package com.samsung.jschettino.wearit;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.*;
import com.radiusnetworks.ibeacon.IBeaconManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
//import android.support.v4.app.NotificationCompat.WearableExtender;


public class DiscoverActivity extends Activity implements IBeaconConsumer {

    private String SpaceManager = "298a49bb.ngrok.com";     // hostname of space manager


    private boolean commandIssued = false;
    private String USER_ID;
    private IBeacon oldBeacon;
    private EditText name;
    private Button run;
    private TextView nameLabel;
    Boolean startScans = false;
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        run = (Button)findViewById(R.id.run);
        name = (EditText) findViewById(R.id.username);
        nameLabel = (TextView) findViewById(R.id.name);
        USER_ID = Build.MODEL +"-"+Build.BRAND+"-"+Build.SERIAL;
        checkUser(USER_ID);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost("http://"+SpaceManager+"/users");
                        try{
                            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                            nameValuePairs.add(new BasicNameValuePair("user[name]",name.getText().toString()));
                            nameValuePairs.add(new BasicNameValuePair("user[uniqueid]",USER_ID));
                            nameValuePairs.add(new BasicNameValuePair("user[activebeacon]","null"));

                            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                            httpClient.execute(httpPost);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    name.setVisibility(View.GONE);
                                    run.setVisibility(View.GONE);
                                    nameLabel.setText(nameLabel.getText()+" "+name.getText().toString());
                                }
                            });
                            startScans = true;
                        }catch(ClientProtocolException e){
                            e.printStackTrace();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        //cancel the alarm

        AlarmManager alarmManager = (AlarmManager) getSystemService(getApplicationContext().ALARM_SERVICE);
        Intent intent = new Intent(this,DiscoverService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,intent,0);
        alarmManager.cancel(pendingIntent);
        Log.e("Error","Cancelling Alarm");
        pendingIntent.cancel();
        iBeaconManager.bind(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public void onPause(){
        Log.e("Error","Starting calendar instance");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND,10);
        Intent intent = new Intent(this,DiscoverService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,intent,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),(1000*60*1),pendingIntent);
        super.onPause();
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
              //  Log.e("Closest Outer", iBeacon.getMinor() + "-" + iBeacon.getAccuracy());
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
                    if(startScans == true) {
                        Log.e("Error","The beacon thing just started");
                        Iterator<IBeacon> iBeaconIterator = iBeacons.iterator();
                        while (iBeaconIterator.hasNext()) {
                            IBeacon temp = iBeaconIterator.next();
                            setBeaconDetails(iBeacons);
                            if (temp.getAccuracy() < 2) {
                                //  Log.e("Beacon Status Accuracy",temp.getAccuracy()+"");
                                setState(true, temp.getMajor(), temp.getMinor());
                                if (temp.getMinor() == 319) {
                                    if (commandIssued == false) {
                                        sendMessage();
                                        commandIssued = true;
                                    }
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

    private void checkUser(final String USER_ID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet("http://"+SpaceManager+"/users?uniqueid="+USER_ID);
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
                    final JSONArray jsonArray = new JSONArray(builder.toString());
                    if(jsonArray.length() == 0){
                        startScans = false;
                        Log.e("Error","User us not registered");
                    }else {
                        final JSONObject jsonObject = jsonArray.getJSONObject(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                name.setVisibility(View.GONE);
                                run.setVisibility(View.GONE);
                                try {
                                    nameLabel.setText(nameLabel.getText() + " " + jsonObject.getString("name"));
                                }catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        startScans = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }


    /**
     * Called when the system detects proximity to a SmartTV
     */
     public void sendMessage() {

        // this is our service list
        // TBD get the service list from the Space Manager :)
        InputStream inputStream = getResources().openRawResource(R.raw.service);
        // String serviceList = getString (R.string.servicelist);
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

         byte buf[] = new byte[1024];
         int len;
         try {
             while ((len = inputStream.read(buf)) != -1) {
                 outputStream.write(buf, 0, len);
             }
             outputStream.close();
             inputStream.close();
         } catch (IOException e) {

         }
         String serviceList = outputStream.toString();


         ServiceParser sp = new ServiceParser(this, serviceList);


            // big message
            NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
            bigStyle.bigText(sp.getMessageTag());  //"Hey, I found a big display in this room. Do you want to use it?"


            int notificationId = 001;
            // Build intent for default notification content
            Intent viewIntent = new Intent(this, ViewEventActivity.class);
            viewIntent.putExtra(getString(R.string.extra_action), 0);  //event_id
            viewIntent.putExtra(getString(R.string.extra_launch), serviceList); // no launch action - pass service list as intent, render UI with it

            PendingIntent viewPendingIntent =
                    PendingIntent.getActivity(this, 0, viewIntent, 0);

            // build base notification here. Use array of services to make additional options after
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_smart_tv)
                            .setLargeIcon(BitmapFactory.decodeResource(
                                    getResources(), R.drawable.smart_tv))
                            .setContentTitle(getString(R.string.discovery_message_title))
                            .setContentText(sp.getBriefTag())
                            .setContentIntent(viewPendingIntent)
                            .setStyle(bigStyle);

            sp.BuildNotifications(notificationBuilder);

            // shake the user's wrist
            notificationBuilder.setVibrate((new long[]{100, 100, 100}));

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);

            notificationManager.notify(notificationId, notificationBuilder.build());

    }


    private void setBeaconDetails(final Collection<IBeacon> iBeacons){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Iterator<IBeacon> iBeaconIterator = iBeacons.iterator();
                String beaconDetails = "";
                while(iBeaconIterator.hasNext()){
                    IBeacon tempBeacon = iBeaconIterator.next();
                    beaconDetails = beaconDetails + tempBeacon.getMajor() +"::"+tempBeacon.getMinor()+"::"+tempBeacon.getAccuracy()+";";

                }
                HttpClient client = new DefaultHttpClient();
                try {
                    HttpGet httpGet1 = new HttpGet("http://"+SpaceManager+"/users/beacondetails?uniqueid="+URLEncoder.encode(USER_ID,"utf-8")+"&beacondetails="+URLEncoder.encode(beaconDetails,"utf-8"));
                    HttpResponse response = client.execute(httpGet1);
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setState(boolean active, final int major,final int minor){
        if(active) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String beaconName ="";
                    HttpClient client = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet("http://"+SpaceManager+"/beacons?major="+major+"&minor="+minor);
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
                        HttpGet httpGet1 = new HttpGet("http://"+SpaceManager+"/users/setactive?uniqueid="+ URLEncoder.encode(USER_ID, "utf-8")+"&activebeacon="+URLEncoder.encode(beaconName,"utf-8"));
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
                    HttpGet httpGet1 = new HttpGet("http://"+SpaceManager+"/users/setpassive?uniqueid="+USER_ID);
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
