package com.github.sparecycles.util.view;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class Screen extends SurfaceView {
	public int width, height, format;
	
	public abstract void draw(Canvas canvas);

	public boolean checkDraw() {
		return true;
	}

	public class DrawThread {
		private Thread thread = null;
		private Canvas canvas = null;
		private boolean running = false;
		
		void beforeLock() {}
		void afterPost() {}
		
		synchronized void stop() {
			running = false;
		}
		
		synchronized boolean start() {
			if(!running && thread != null)
				try {
					thread.join();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			if(thread != null)
				return false;
			
			thread = new Thread() {
				public void run() {
					while(running)
					{
						if(checkDraw() == false)
						{
							try {
								Thread.sleep(1000/60);
							} catch (InterruptedException e) {
							}
							continue;
						}
						
						beforeLock();
						
						post(new Runnable() {
							public void run() {
								canvas = getHolder().lockCanvas();
								if(canvas != null) {
									draw(canvas);
								} else {
									Log.w("surface.draw", "lockCanvas failed");
								}
							}
						});
											
						Runnable finish = new Runnable() {
							synchronized public void run() {
								if(canvas != null) {
									getHolder().unlockCanvasAndPost(canvas);
								}
								afterPost();
								notify();
							}
						};
						
						synchronized(finish) {
							post(finish);
							try {
								finish.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					Log.i("surface.draw", "run-complete");
					thread = null;
				}
			};
			
			running = true;
			thread.start();
			return true;
		};
	};
	
	private final DrawThread drawThread; 
	
	public Screen(Context context) {
		super(context);
	}
	
	{
		getHolder().addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				Log.d("surface", "changed");
				synchronized(Screen.this) {
					Screen.this.width = width;
					Screen.this.height = height;
					Screen.this.format = format;
				}
			}
	
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				Log.d("surface", "created");
				drawThread.start();
			}
	
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				Log.d("surface", "destroyed");
				synchronized(Screen.this) {
					Screen.this.width = 0;
					Screen.this.height = 0;
					Screen.this.format = -1;
				}
				drawThread.stop();
			}
		});
		
		drawThread = new DrawThread();
	}
}
