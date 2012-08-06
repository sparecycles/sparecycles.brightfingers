package com.github.sparecycles.util.input;

import java.util.HashSet;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.lang.ref.WeakReference;

public abstract class Sensory implements SensorEventListener {
	static final HashSet<WeakReference<Sensory> > sensors = new HashSet<WeakReference<Sensory> >();

	final Context context;
	final int sensor_delay, sensor_type;
	float values[];
	WeakReference<Sensory> entry;
	
	Sensory(Context context, int sensor_type, int sensor_delay)
	{
		sensors.add(entry = new WeakReference<Sensory>(this));
		this.context = context;
		this.sensor_delay = sensor_delay;
		this.sensor_type = sensor_type;
	}
	
	@Override
	protected void finalize() throws Throwable {
		sensors.remove(entry);
		super.finalize();
	}
	
	public void activate() {
		SensorManager manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = manager.getDefaultSensor(sensor_type);
		if(sensor != null) {
			manager.registerListener(this, sensor, sensor_delay);
		}
	}
	
	public void deactivate() {
		SensorManager manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		manager.unregisterListener(this);
	}
	
	public static void activateAll() {
		for(WeakReference<Sensory> sensor_ref : sensors) {
			Sensory sensor = sensor_ref.get();
			if(sensor == null)
				continue;
			sensor.activate();
		}
	}

	public static void deactivateAll() {
		for(WeakReference<Sensory> sensor_ref : sensors) {
			Sensory sensor = sensor_ref.get();
			if(sensor == null)
				continue;
			sensor.deactivate();
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	@Override
	public abstract void onSensorChanged(SensorEvent event);
	
	public static class Accelerometer extends Sensory {
		public interface Listener {
			void values(float x, float y, float z);
		};
		
		Listener listener;
		
		public Accelerometer(Context context, int sampling_delay, Listener listener) {
			super(context, Sensor.TYPE_ACCELEROMETER, sampling_delay);
			this.listener = listener;
		}

		public Accelerometer(Context context, Listener listener) {
			super(context, Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_NORMAL);
			this.listener = listener;
		}
		
		@Override
		public void onSensorChanged(SensorEvent event)
		{
			float values[] = event.values;
			listener.values(values[0], values[1], values[2]);
		}
	}
}
