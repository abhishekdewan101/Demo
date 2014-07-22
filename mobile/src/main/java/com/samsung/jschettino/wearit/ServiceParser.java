package com.samsung.jschettino.wearit;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by j.schettino on 7/22/14.
 */

public class ServiceParser {
    private static final String MESSAGE_TAG = "message";
    private static final String BRIEF_TAG = "brief";
    private static final String SERVICES_TAG = "services";
    private static final String APP_TAG = "app";
    private static final String NAME_TAG = "name";
    private static final String ICON_TAG = "icon";
    //private static final String DEFAULT_TAG = "default";
    private ArrayList<String> apps;
    private String json;

    private Context context;

    private JSONObject jsonObj;

    public ServiceParser(Context context, String serviceJson) {
        json = serviceJson;
        this.context = context;
        try {
            jsonObj = new JSONObject(serviceJson);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("yo", e.toString());

            jsonObj = null;
        }

    }

    public String getMessageTag() { try { return jsonObj.getString(MESSAGE_TAG); } catch (Exception e) { return ""; } }

    public String getBriefTag()  { try { return jsonObj.getString(BRIEF_TAG); } catch (Exception e) { return ""; } }

    public String getAppByOffset(int offset) { if (apps.size() > offset) return apps.get(offset); else return ""; }

    /**
     * @param notificationBuilder build notification options from json
     */
    void BuildNotifications(final NotificationCompat.Builder notificationBuilder) {

        // parse json, for each item make a notification
        Parse(new ItemAction() {
                  @Override
                  public void Item(String name, String app, String icon, int index) {

                      Intent launchIntent = new Intent(context, PerformActionActvity.class); //BeaconActivity
                      launchIntent.putExtra(context.getString(R.string.extra_action), index + 1);  // each action needs a unique Id
                      Log.i("yo", app);
                      launchIntent.putExtra(context.getString(R.string.extra_launch), app); // and a launch action

                      PendingIntent launchPendingIntent =
                              PendingIntent.getActivity(context, index + 1, launchIntent, 0);

                      // default on error - need a default icon...
                      int iconId = R.drawable.ic_smart_tv;
                      try {
                          iconId = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
                      } catch (Exception e) {
                          iconId = R.drawable.ic_smart_tv;
                      }
                      notificationBuilder.addAction(iconId, name, launchPendingIntent);

                  }
              }
            );
    }

    void BuildChoiceList(Spinner s) {
        final ArrayList<String> choices = new ArrayList<String>();
        final ArrayList<String> icons = new ArrayList<String>();
        apps = new ArrayList<String>();

        // parse json, for each item make an item in a spinner list
        Parse(new ItemAction() {
                  @Override
                  public void Item(String name, String app, String icon, int index) {
                      choices.add(name);
                      icons.add(icon);
                      apps.add(app);
                  }
              }
        );
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, choices);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(spinnerArrayAdapter);
    }

    /**
     * Call action callback for each item in choices array in json
     * @param action
     */
    void Parse(ItemAction action) {

        try {
            // parse array of services
            JSONArray services = jsonObj.getJSONArray(SERVICES_TAG);

            for (int i = 0; i < services.length(); i++) {
                JSONObject service = services.getJSONObject(i);
                Log.i("yo", service.toString());

                // do something smart if we get bad json data
                String service_app, service_label, service_icon;

                try {
                    service_app = service.getString(APP_TAG);
                    service_label = service.getString(NAME_TAG);
                    service_icon = service.getString(ICON_TAG);
                } catch (Exception e) {
                    Log.e("yo", "Service List, bad/missing json value for tag: " + APP_TAG);
                    continue;
                }

                action.Item(service_label, service_app, service_icon, i);

            }
        } catch (JSONException e) {
            Log.e("yo", "Json service list epic fail. " + json);
            e.printStackTrace();
        }

    }


}
