package com.github.sparecycles.util.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class CanvasBuffer {
	Bitmap bitmap;

	public Canvas start(Canvas canvas) {
		if(null != bitmap && (bitmap.getWidth() != canvas.getWidth() || bitmap.getHeight() != canvas.getHeight()))
			bitmap = null;
		
		if(bitmap == null)
			bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
		
		return new Canvas(bitmap);
	}
	
	public void end(Canvas canvas) {
		end(canvas, new Matrix());
	}
	
	public void end(Canvas canvas, Matrix matrix) {
		canvas.drawBitmap(bitmap, matrix, null);
	}
}