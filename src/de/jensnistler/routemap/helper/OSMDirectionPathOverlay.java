package de.jensnistler.routemap.helper;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class OSMDirectionPathOverlay extends Overlay{
    private GeoPoint mSource;
    private GeoPoint mTarget;

    public OSMDirectionPathOverlay(Context context, GeoPoint source, GeoPoint target) {
        super(context);

        mSource = source;
        mTarget = target;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
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
    }
}
