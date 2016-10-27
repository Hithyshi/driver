package com.tromke.mydrive;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by satyam on 23/07/15.
 */
public class ParseLogger {
    private static Context mContext;
    private long tripID;
    private int startStopIndicator = 0;
    private Location mLastLocation;
    private static ParseLogger logger = null;
    private ParsetripIDCallback actionListener;
    Context ctx;
    String tripObjectId;
    protected ParseLogger() {

    }
    public interface ParsetripIDCallback {
        public void succsess(int id);
        public void fail();
    }

    public void settripIdListner(ParsetripIDCallback listener) {
        actionListener = listener;
    }


    public static ParseLogger getInstance(Context ctx) {
        mContext=ctx;
        if (logger == null) {
            logger = new ParseLogger();
        }

        return logger;
    }

    public void createNewTripID() {
        tripID = System.currentTimeMillis();
    }

    public void setLastLocation(Location lastLocation,String tripId) {
        mLastLocation = lastLocation;
     if(tripId!=null){
         startStopIndicator=1;
     }
        if (startStopIndicator == 1) {
            ParseObject locationObj = new ParseObject("Location");
            locationObj.put("location", new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            locationObj.put("user", ParseUser.getCurrentUser());
           if(tripId!=null){
               locationObj.put("trip",ParseObject.createWithoutData("Trip",tripId));
           }else{
               locationObj.put("trip",ParseObject.createWithoutData("Trip",tripObjectId));
           }
            locationObj.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(mContext, mContext.getString(R.string.location_updated_message),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.location_update_failed),
                                Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    public void startTracking(Location location) {
        mLastLocation=location;
        startStopIndicator = 1;
        createNewTripID();
        final ParseObject trip = new ParseObject("Trip");
     //   trip.put("tripID", tripID);
        if(mLastLocation!=null) {
            trip.put("startLocation", new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            trip.put("user", ParseUser.getCurrentUser());
            trip.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e==null){
                        tripObjectId=trip.getObjectId();
                        actionListener.succsess(1);
                    }else{
                        actionListener.fail();
                    }
                }
            });
        }
    }

    public void stopTracking(Location  location,String tripId) {
        mLastLocation=location;
        ParseQuery<ParseObject> tripQuery = ParseQuery.getQuery("Trip");
        if(tripId!=null){
            tripQuery.whereEqualTo("objectId",tripId);
        }else{
            tripQuery.whereEqualTo("objectId",tripObjectId);
        }

        tripQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    for (ParseObject obj : list) {
                        obj.put("endLocation", new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                        obj.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    actionListener.succsess(2);
                                } else {
                                    actionListener.fail();

                                }
                            }
                        });
                    }
                } else {
                    actionListener.fail();
                }


            }
        });
        /*tripQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject == null) {
                    parseObject.put("endLocation", new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    parseObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                actionListener.succsess(2);
                            } else {
                                actionListener.fail();

                            }
                        }
                    });
                } else {
                    actionListener.fail();

                }
            }
        });
*/        startStopIndicator = 0;
    }
}
