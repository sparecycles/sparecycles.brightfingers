package com.github.sparecycles.brightfingers;

import java.util.ArrayList;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.github.sparecycles.util.input.Touch;
import com.github.sparecycles.util.input.Sensory;
import com.github.sparecycles.util.view.CanvasBuffer;
import com.github.sparecycles.util.view.Screen;

public class Main extends Activity {
	Screen screen;
	boolean needDraw;
	boolean noCursors = true;
	Touch<MyFinger> touchInput;
	float erase;
	
	Sensory.Accelerometer accelerometer = new Sensory.Accelerometer(this, SensorManager.SENSOR_DELAY_UI,
		new Sensory.Accelerometer.Listener() {
			@Override
			public void values(float x, float y, float z) {
				float mag = android.util.FloatMath.sqrt(x*x + y*y + z*z) / SensorManager.GRAVITY_EARTH;
				mag -= 1.3;
				if(mag > 0) {
					Main.this.erase += mag;
					Main.this.needDraw = true;
				}
			}
		}
	);
		
	CanvasBuffer canvasBuffer = new CanvasBuffer();
		
	void draw(Canvas canvas) {
		canvas.drawColor(0xFF000000);		
		Canvas buffered = canvasBuffer.start(canvas);
		Paint paint = new Paint();
		
		paint.setColor(0x40CCCCCC);
		
		for(MyFinger pointer : touchInput.values()) {
			MyFinger finger = (MyFinger)pointer;
			ArrayList<MyFinger.Record> points = finger.locations;
			
			if(points.isEmpty())
				continue;
			
			MyFinger.Record start = points.get(0);
			paint.setColor(finger.color);
			
			for(MyFinger.Record record : points.subList(1, points.size())) {
				float stroke_width = record.pressure*30;
				paint.setStrokeWidth(stroke_width);
				buffered.drawLine(start.x, start.y, record.x, record.y, paint);
				start = record;
			}
			
			for(MyFinger.Record record : points) {
				float stroke_width = record.pressure*30/2;
				buffered.drawCircle(record.x, record.y, stroke_width, paint);
			}
			
			points.subList(0, points.size() - 1).clear();
		}
		
		if(erase > 0) {
			paint.setColor(0xFF000000);
			int erase_alpha = (int)(erase/2 * 256);
			if(erase_alpha > 255) {
				erase_alpha = 255;
			}
			paint.setAlpha(erase_alpha);
			buffered.drawPaint(paint);
			erase = 0;
		}
		
		canvasBuffer.end(canvas);
		
		paint.setColor(0xFF008000);
		
		if(noCursors)
			return;
		
		paint.setStrokeWidth(1);
		
		for(MyFinger finger : touchInput.values()) {
			ArrayList<MyFinger.Record> points = finger.locations;
			if(points.isEmpty()) continue;
			MyFinger.Record cursor = points.get(0);
			paint.setColor(finger.color);
			paint.setAlpha((int)(256*cursor.pressure));
			canvas.drawLine(0, cursor.y, canvas.getWidth(), cursor.y, paint);
			canvas.drawLine(cursor.x, 0, cursor.x, canvas.getHeight(), paint);
		}
	}
	
	static final java.util.Random random = new java.util.Random();

	public class MyFinger implements Touch.Finger {
		int color;
		ArrayList<Record> locations = new ArrayList<Record>();
		
		class Record {
			int x, y;
			float pressure;
			Record(int x, int y, float pressure) {
				this.x = x;
				this.y = y;
				this.pressure = pressure;
			}
		};		

		@Override
		public void cancel() {
			locations.clear();
			Main.this.needDraw = true;			
		}

		@Override
		public void touch(int x, int y, float pressure) {
			color = random.nextInt() | 0xFF000000;
		}
		
		@Override
		public void release(int x, int y, float pressure) {
			cancel();
		}

		@Override
		public void move(int x, int y, float pressure) {
			locations.add(new Record(x, y, pressure));
			Main.this.needDraw = true;
		}
	};

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContentView(screen = new Screen(this) {
			public boolean checkDraw() {
				return Main.this.needDraw;
			}
			public void draw(Canvas canvas)  {
				Main.this.needDraw = false;
				Main.this.draw(canvas);
			}
		});
		
		touchInput = new Touch.Multi<MyFinger>() {
			@Override
			protected MyFinger make() { return new MyFinger(); }
			{ feel(screen); }
		};
	}
	
	@Override
	protected void onPause()  {
		super.onPause();
		Sensory.deactivateAll();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Sensory.activateAll();
		needDraw = true;
	}
}
