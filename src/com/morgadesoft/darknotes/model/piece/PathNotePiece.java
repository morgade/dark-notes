package com.morgadesoft.darknotes.model.piece;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.morgadesoft.darknotes.engine.NotePieceBitmapBuilder;

public class PathNotePiece extends BaseNotePiece implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<List<short[]>> paths;
	
	private transient List<short[]> currentPath;
	
	private transient short minX = -1;
	private transient short minY = -1;
	private transient short maxX = -1;
	private transient short maxY = -1;
	
	public PathNotePiece() {
		this.paths = new LinkedList<List<short[]>>();
	}

	public void startNewPath() {
		if (currentPath==null || !currentPath.isEmpty()) {
			this.paths.add(currentPath = new LinkedList<short[]>());
		}
	}

	public void addPoint(short x, short y) {
		addPoint(new short[]{x,y});
	}
	
	public void addPoint(short[] point) {
		if (currentPath==null) {
			startNewPath();
		}
		
		currentPath.add(point);
		updateBounds(point);
	}
	
	public void drawCropped(Canvas canvas, Paint paint, Matrix matrix, float pX, float pY) {
		recalcBounds();
		
		NotePieceBitmapBuilder croppedBuilder = new NotePieceBitmapBuilder(pX, pY, canvas, paint);
		float[] mappedMin = new float[2];
		float[] mappedPoint = new float[2];
		matrix.mapPoints(mappedMin, new float[]{minX,minY});
		
		for (List<short[]> path : paths) {
			croppedBuilder.nextPath();
			
			for (short[] point : path) {
				mappedPoint[0] = point[0];
				mappedPoint[1] = point[1];
				matrix.mapPoints(mappedPoint);
				croppedBuilder.nextPoint(mappedPoint[0] - mappedMin[0],  mappedPoint[1] - mappedMin[1]);
			}
		}
	}
	
	public List<short[]> getCurrentPath() {
		return currentPath;
	}

	public int getWidth() {
		recalcBounds();
		return maxX - minX;
	}
	
	public int getHeight() {
		recalcBounds();
		return maxY - minY;
	}

	@Override
	public boolean isWhiteSpace() {
		return false;
	}

	@Override
	public boolean isLineBreak() {
		return false;
	}

	public List<List<short[]>> getPaths() {
		return paths;
	}
	
	public int getMinX() {
		recalcBounds();
		return minX;
	}
	
	public int getMinY() {
		recalcBounds();
		return minY;
	}

	public void dropCurrentPath() {
		paths.remove(paths.size()-1);
		if (!paths.isEmpty()) {
			currentPath = paths.get(paths.size()-1);
		}
		
		minX = minY = maxX = maxY = -1;
	}
	
	public void forceRecalcBounds() {
		minX = minY = maxX = maxY = -1;
		recalcBounds();
	}
	
	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		stream.defaultReadObject();
		forceRecalcBounds();
	}
	
	private void recalcBounds() {
		if (minX<0) {
			for (List<short[]> path : paths) {
				for (short[] point : path) {
					updateBounds(point);
				}
			}
		}
	}

	private void updateBounds(short[] point) {
		if (minX<0) {
			minX = point[0];
			maxX = point[0];
			minY = point[1];
			maxY = point[1];
		} else {
			minX = minX<point[0] ? minX : point[0];
			maxX = maxX>point[0] ? maxX : point[0];
			minY = minY<point[1] ? minY : point[1];
			maxY = maxY>point[1] ? maxY : point[1];
		}
	}

	public void concat(PathNotePiece piece) {
		short x;
		short translate;
		if (paths.isEmpty()) {
			paths.addAll(piece.paths);
			translate = 0;
		} else {
			recalcBounds();
			translate = maxX;
			List<short[]> l;
			for (List<short[]> path : piece.paths) {
				paths.add(l = new LinkedList<short[]>());
				x = translate;
				
				for (short[] point : path) {
					short currX = (short) (point[0] + x);
					l.add(new short[]{currX, point[1]});
					translate = currX > maxX ? currX : translate;
				}
			}
		}
		
		
		minX = maxX = minY = maxY = -1;
	}

}
