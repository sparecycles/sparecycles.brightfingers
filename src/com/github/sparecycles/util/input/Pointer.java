package com.github.sparecycles.util.input;

import java.util.HashMap;

import android.view.MotionEvent;
import android.view.View;

public abstract class Pointer {
	public void cancel() {}
	public void touch(int x, int y, float pressure) {}
	public void release(int x, int y, float pressure) {}
	public void move(int x, int y, float pressure) {}
	
	public
	static interface Factory<PointerType extends Pointer> {
		PointerType touch(int x, int y);
		void recycle(PointerType pointer);
	}
	
	@SuppressWarnings("serial")
	public
	static class Manager<PointerType extends Pointer> extends HashMap<Integer, PointerType> implements View.OnTouchListener {
		Factory<PointerType> factory;
		
		public Manager(View view, Factory<PointerType> factory) {
			super();
			this.factory = factory;
			view.setOnTouchListener(this);
		}

		public boolean onTouch(View view, MotionEvent event) {
			int pointerIndex = event.getActionIndex();
			int pointerId = event.getPointerId(pointerIndex);
			int action = event.getActionMasked();
			int x, y;
			float pressure;
			PointerType pointer;

			switch(action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				x = (int)event.getX(pointerIndex);
				y = (int)event.getY(pointerIndex);
				pressure = event.getPressure(pointerIndex);

				PointerType newPointer = factory.touch(x,y);
				if(newPointer == null)
					break;
				put(pointerId, newPointer);
				newPointer.touch(x, y, pressure);
				newPointer.move(x, y, pressure);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				if(!containsKey(pointerId))
					break;;
				pointer = remove(pointerId);
				x = (int)event.getX(pointerIndex);
				y = (int)event.getY(pointerIndex);
				pressure = event.getPressure(pointerIndex);
				pointer.release(x, y, pressure);
				factory.recycle(pointer);
				break;
			case MotionEvent.ACTION_OUTSIDE:
			case MotionEvent.ACTION_CANCEL:
				if(!containsKey(pointerId))
					break;
				pointer = remove(pointerId);
				pointer.cancel();
				factory.recycle(pointer);
				break;
			case MotionEvent.ACTION_MOVE:
				int historySize = event.getHistorySize();
				int pointerCount = event.getPointerCount();
				for(pointerIndex = 0; pointerIndex < pointerCount; pointerIndex++) {
					pointerId = event.getPointerId(pointerIndex);
					if(!containsKey(pointerId))
						continue;
					pointer = get(pointerId);
					for(int pos = 0; pos < historySize; pos++) {
						x = (int)event.getHistoricalX(pointerIndex, pos);
						y = (int)event.getHistoricalY(pointerIndex, pos);
						pressure = event.getHistoricalPressure(pointerIndex, pos);
						pointer.move(x, y, pressure);
					}
					x = (int)event.getX(pointerIndex);
					y = (int)event.getY(pointerIndex);
					pressure = event.getPressure(pointerIndex);
					pointer.move(x, y, pressure);
				}
				
				break;
			}
			return true;
		}
	}
}