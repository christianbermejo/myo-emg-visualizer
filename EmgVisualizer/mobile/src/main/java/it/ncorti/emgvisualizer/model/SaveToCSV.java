package it.ncorti.emgvisualizer.model;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.squareup.otto.Subscribe;

import java.text.MessageFormat;
import java.util.LinkedList;

import it.ncorti.emgvisualizer.R;
import it.ncorti.emgvisualizer.ui.MySensorManager;
import it.ncorti.emgvisualizer.ui.views.SensorGraphView;

/**
 * Class which saves files to CSV
 * @author ChristianB
 */

public class SaveToCSV {


    /** TAG for debugging purpose */
    private static final String TAG = "SaveToCSV";

    /** Framerate ms gap */
    private static final int FRAMERATE_SKIP_MS = 20;

    /** Reference to sensor */
    private Sensor sensor;
    /** Point spread */
    private float spread = 0;
    /** Reference to thread handler */
    private Handler handler;
    /** Reference to runnable for graph timing */
    private Runnable runner;

    /** Array of normalized points */
    private float[] normalized;


    /**
     * Public constructor to create a new SaveToCSV
     */
    public SaveToCSV() {
        this.sensor = MySensorManager.getInstance().getMyo();
        this.normalized = new float[sensor.getChannels()];
        handler = new Handler(Looper.getMainLooper());
        checkForErrorMessage();

        initialiseSensorData();
    }


    /**
     * Private method to check if an error message must be displayed
     */
    private void checkForErrorMessage() {
        Log.d(TAG, "Measuring: " + sensor.isMeasuring() + " Status conn: " + sensor.isConnected());
        if (sensor.isMeasuring() && sensor.isConnected()) {
//            errorMessage.setVisibility(View.GONE);
//            graph.setVisibility(View.VISIBLE);
        } else {
//            errorMessage.setVisibility(View.VISIBLE);
//            graph.setVisibility(View.GONE);
        }
    }

    public void attachEvents() {
        EventBusProvider.register(this);
    }

    public void detachEvents() {
        EventBusProvider.unregister(this);
    }

    /**
     * Method to initialize sensor data to be displayed
     */
    protected void initialiseSensorData() {
        spread = sensor.getMaxValue() - sensor.getMinValue();
        LinkedList<RawDataPoint> dataPoints = sensor.getDataPoints();

        if (dataPoints == null || dataPoints.isEmpty()) {
            Log.w("sensor data", "no data found for sensor " + sensor.getName());
            return;
        }

        int channels = sensor.getChannels();

        LinkedList<Float>[] normalisedValues = new LinkedList[channels];
        for (int i = 0; i < channels; ++i) {
            normalisedValues[i] = new LinkedList<Float>();
        }


        for (RawDataPoint dataPoint : dataPoints) {
            for (int i = 0; i < channels; ++i) {
                float normalised = (dataPoint.getValues()[i] - sensor.getMinValue()) / spread;
                normalisedValues[i].add(normalised);
            }
        }
    }

    /**
     * Callback for sensor connect event
     * @param event Just received event
     */
    @Subscribe
    public void onSensorConnectEvent(SensorConnectEvent event) {
        if (event.getSensor().getName().contentEquals(sensor.getName())) {
            checkForErrorMessage();
            Log.d(TAG, "Event connected received " + event.getState());
        }
    }

    /**
     * Callback for sensor measuring event
     * @param event Just received event
     */
    @Subscribe
    public void onSensorMeasuringEvent(SensorMeasuringEvent event) {
        if (event.getSensor().getName().contentEquals(sensor.getName())) {
            checkForErrorMessage();
            Log.d(TAG, "Event measuring received " + event.getState());
        }
    }

    /**
     * Callback for sensor updated
     * @param event Just received event
     */
    @Subscribe
    public void onSensorUpdatedEvent(SensorUpdateEvent event) {
        if (!event.getSensor().getName().contentEquals(sensor.getName())) return;
        for (int i = 0; i < sensor.getChannels(); i++) {
            normalized[i] = (event.getDataPoint().getValues()[i] - sensor.getMinValue()) / spread;
        }

    }

    /**
     * Callback for sensor connect event
     * @param event Just received event
     */
    @Subscribe
    public void onSensorRangeEvent(SensorRangeEvent event) {
        if (event.getSensor().getName().contentEquals(sensor.getName()))
            initialiseSensorData();

    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // handle item selection
//        switch (item.getItemId()) {
//            case R.id.action_start_graph:
//                runner = new Runnable() {
//                    long last = System.currentTimeMillis();
//                    long actual;
//
//                    public void run() {
//                        graph.invalidate();
//                        actual = System.currentTimeMillis();
//                        if (actual - last > FRAMERATE_SKIP_MS)
//                            handler.postDelayed(this, actual - last);
//                        else
//                            handler.postDelayed(this, FRAMERATE_SKIP_MS);
//                        last = actual;
//                    }
//                };
//                graph.setRunning(true);
//                handler.post(runner);
//                return true;
//            case R.id.action_pause_graph:
//                graph.setRunning(false);
//                handler.removeCallbacks(runner);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
}
