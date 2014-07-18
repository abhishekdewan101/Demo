package com.samsung.jschettino.wearit;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
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

import java.util.Collection;
import java.util.Iterator;
//import android.support.v4.app.NotificationCompat.WearableExtender;


public class MyActivity extends Activity implements IBeaconConsumer {
    public static final String EXTRA_EVENT_ID = "eventId";
    private boolean commandIssued = false;
   // private TextView process;
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        //process = (TextView) findViewById(R.id.process);
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

    @Override
    public void onIBeaconServiceConnect() {
            iBeaconManager.setRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                    Iterator<IBeacon> iBeaconIterator = iBeacons.iterator();
                    while(iBeaconIterator.hasNext()){
                        IBeacon temp = iBeaconIterator.next();
                        if(temp.getMinor() == 319){
                            Log.e("Beacon Status Accuracy",temp.getAccuracy()+"");
                            if(temp.getAccuracy()<3){
                                Log.e("Beacon Status","entered");
                                if(commandIssued == false){
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
                                getString(R.string.useTv), viewShareIntent);
// Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

// Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
        Log.e("Beacon Status","Got out Send Message");
    }
}
