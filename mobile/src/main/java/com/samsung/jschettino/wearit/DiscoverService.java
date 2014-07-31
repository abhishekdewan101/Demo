package com.samsung.jschettino.wearit;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by a.dewan on 7/24/14.
 */
public class DiscoverService extends IntentService implements IBeaconConsumer {


    private String SpaceManager = "65d5286d.ngrok.com";     // hostname of space manager
    private String USER_ID = Build.MODEL +"-"+Build.BRAND+"-"+Build.SERIAL;
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    Boolean somethingFound = false;
    Boolean somethingWrong = false;
    Boolean commandIssued = false;

    public DiscoverService(){super("DiscoverService");};

    @Override
    protected void onHandleIntent(Intent intent){
        Log.e("Error","Service Started");
        iBeaconManager.bind(this);
        while(!somethingFound){if(somethingWrong){Log.e("Error","Something went wrong close service");break;}}
    }

    @Override
    public void onDestroy(){
        Log.e("Error","On Destory");
        super.onDestroy();
        iBeaconManager.unBind(this);
    }

    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                Iterator<IBeacon> iBeaconIterator = iBeacons.iterator();
                while (iBeaconIterator.hasNext()) {
                    IBeacon temp = iBeaconIterator.next();
                    setBeaconDetails(iBeacons);
                    if (temp.getAccuracy() < 2) {
                        Log.e("Beacon Status Accuracy",temp.getAccuracy()+"");
                        if (temp.getMinor() == 319) {
                            if (commandIssued == false) {
                                sendMessage();
                                commandIssued = true;
                            }
                        }
                    }
                    Log.e("Error","Calling setState");
                    setState(true, temp.getMajor(), temp.getMinor());
                }
            }
        });

        try{
            iBeaconManager.startRangingBeaconsInRegion(new Region("myUniqueID",null,null,null));
        }catch (RemoteException e){
            somethingWrong =true;
            e.printStackTrace();
        }
    }

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
                    somethingWrong = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    somethingWrong = true;
                }
            }
        }).start();
    }

    private void setState(boolean active, final int major,final int minor){
        if(active) {
            Log.e("Error","Uploading Data");
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
                        somethingWrong = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        somethingWrong = true;
                    }
                }
            }).start();
            somethingFound = true;
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
                        somethingWrong = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        somethingWrong = true;
                    }
                }
            }).start();
        }
    }
}
