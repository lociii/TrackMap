package de.jensnistler.routemap.helper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class DirectionPathOverlay extends Overlay{
    private GeoPoint mSource;
    private GeoPoint mTarget;
    
    public DirectionPathOverlay(GeoPoint source, GeoPoint target) {
        mSource = source;
        mTarget = target;
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
        Projection projection = mapView.getProjection();
        if (shadow == false) {
            Point point = new Point();
            projection.toPixels(mSource, point);

            Point point2 = new Point();
            projection.toPixels(mTarget, point2);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(8);
            canvas.drawLine(
                (float) point.x,
                (float) point.y,
                (float) point2.x,
                (float) point2.y,
                paint
            );
        }
        return super.draw(canvas, mapView, shadow, when);
    }
}
