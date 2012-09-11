package de.jensnistler.trackmap.helper;

import javax.microedition.khronos.opengles.GL;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

final class SmoothCanvas extends Canvas {
    Canvas delegate;

    private final Paint mSmooth = new Paint(Paint.FILTER_BITMAP_FLAG);

    public void setBitmap(Bitmap bitmap) {
        delegate.setBitmap(bitmap);
    }

    public void setViewport(int width, int height) {
        delegate.setViewport(width, height);
    }

    public boolean isOpaque() {
        return delegate.isOpaque();
    }

    public int getWidth() {
        return delegate.getWidth();
    }

    public int getHeight() {
        return delegate.getHeight();
    }

    public int save() {
        return delegate.save();
    }

    public int save(int saveFlags) {
        return delegate.save(saveFlags);
    }

    public int saveLayer(RectF bounds, Paint paint, int saveFlags) {
        return delegate.saveLayer(bounds, paint, saveFlags);
    }

    public int saveLayer(float left, float top, float right, float bottom, Paint paint, int saveFlags) {
        return delegate.saveLayer(left, top, right, bottom, paint, saveFlags);
    }

    public int saveLayerAlpha(RectF bounds, int alpha, int saveFlags) {
        return delegate.saveLayerAlpha(bounds, alpha, saveFlags);
    }

    public int saveLayerAlpha(float left, float top, float right, float bottom, int alpha, int saveFlags) {
        return delegate.saveLayerAlpha(left, top, right, bottom, alpha, saveFlags);
    }

    public void restore() {
        delegate.restore();
    }

    public int getSaveCount() {
        return delegate.getSaveCount();
    }

    public void restoreToCount(int saveCount) {
        delegate.restoreToCount(saveCount);
    }

    public void translate(float dx, float dy) {
        delegate.translate(dx, dy);
    }

    public void scale(float sx, float sy) {
        delegate.scale(sx, sy);
    }

    public void rotate(float degrees) {
        delegate.rotate(degrees);
    }

    public void skew(float sx, float sy) {
        delegate.skew(sx, sy);
    }

    public void concat(Matrix matrix) {
        delegate.concat(matrix);
    }

    public void setMatrix(Matrix matrix) {
        delegate.setMatrix(matrix);
    }

    public void getMatrix(Matrix ctm) {
        delegate.getMatrix(ctm);
    }

    public boolean clipRect(RectF rect, Region.Op op) {
        return delegate.clipRect(rect, op);
    }

    public boolean clipRect(Rect rect, Region.Op op) {
        return delegate.clipRect(rect, op);
    }

    public boolean clipRect(RectF rect) {
        return delegate.clipRect(rect);
    }

    public boolean clipRect(Rect rect) {
        return delegate.clipRect(rect);
    }

    public boolean clipRect(float left, float top, float right, float bottom, Region.Op op) {
        return delegate.clipRect(left, top, right, bottom, op);
    }

    public boolean clipRect(float left, float top, float right, float bottom) {
        return delegate.clipRect(left, top, right, bottom);
    }

    public boolean clipRect(int left, int top, int right, int bottom) {
        return delegate.clipRect(left, top, right, bottom);
    }

    public boolean clipPath(Path path, Region.Op op) {
        return delegate.clipPath(path, op);
    }

    public boolean clipPath(Path path) {
        return delegate.clipPath(path);
    }

    public boolean clipRegion(Region region, Region.Op op) {
        return delegate.clipRegion(region, op);
    }

    public boolean clipRegion(Region region) {
        return delegate.clipRegion(region);
    }

    public DrawFilter getDrawFilter() {
        return delegate.getDrawFilter();
    }

    public void setDrawFilter(DrawFilter filter) {
        delegate.setDrawFilter(filter);
    }

    public GL getGL() {
        return delegate.getGL();
    }

    public boolean quickReject(RectF rect, EdgeType type) {
        return delegate.quickReject(rect, type);
    }

    public boolean quickReject(Path path, EdgeType type) {
        return delegate.quickReject(path, type);
    }

    public boolean quickReject(float left, float top, float right, float bottom, EdgeType type) {
        return delegate.quickReject(left, top, right, bottom, type);
    }

    public boolean getClipBounds(Rect bounds) {
        return delegate.getClipBounds(bounds);
    }

    public void drawRGB(int r, int g, int b) {
        delegate.drawRGB(r, g, b);
    }

    public void drawARGB(int a, int r, int g, int b) {
        delegate.drawARGB(a, r, g, b);
    }

    public void drawColor(int color) {
        delegate.drawColor(color);
    }

    public void drawColor(int color, PorterDuff.Mode mode) {
        delegate.drawColor(color, mode);
    }

    public void drawPaint(Paint paint) {
        delegate.drawPaint(paint);
    }

    public void drawPoints(float[] pts, int offset, int count, Paint paint) {
        delegate.drawPoints(pts, offset, count, paint);
    }

    public void drawPoints(float[] pts, Paint paint) {
        delegate.drawPoints(pts, paint);
    }

    public void drawPoint(float x, float y, Paint paint) {
        delegate.drawPoint(x, y, paint);
    }

    public void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
        delegate.drawLine(startX, startY, stopX, stopY, paint);
    }

    public void drawLines(float[] pts, int offset, int count, Paint paint) {
        delegate.drawLines(pts, offset, count, paint);
    }

    public void drawLines(float[] pts, Paint paint) {
        delegate.drawLines(pts, paint);
    }

    public void drawRect(RectF rect, Paint paint) {
        delegate.drawRect(rect, paint);
    }

    public void drawRect(Rect r, Paint paint) {
        delegate.drawRect(r, paint);
    }

    public void drawRect(float left, float top, float right, float bottom, Paint paint) {
        delegate.drawRect(left, top, right, bottom, paint);
    }

    public void drawOval(RectF oval, Paint paint) {
        delegate.drawOval(oval, paint);
    }

    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        delegate.drawCircle(cx, cy, radius, paint);
    }

    public void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        delegate.drawArc(oval, startAngle, sweepAngle, useCenter, paint);
    }

    public void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {
        delegate.drawRoundRect(rect, rx, ry, paint);
    }

    public void drawPath(Path path, Paint paint) {
        delegate.drawPath(path, paint);
    }

    public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        if (paint == null) {
            paint = mSmooth;
        } else {
            paint.setFilterBitmap(true);
        }
        delegate.drawBitmap(bitmap, left, top, paint);
    }

    public void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
        if (paint == null) {
            paint = mSmooth;
        } else {
            paint.setFilterBitmap(true);
        }
        delegate.drawBitmap(bitmap, src, dst, paint);
    }

    public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
        if (paint == null) {
            paint = mSmooth;
        } else {
            paint.setFilterBitmap(true);
        }
        delegate.drawBitmap(bitmap, src, dst, paint);
    }

    public void drawBitmap(int[] colors, int offset, int stride, int x, int y, int width, int height, boolean hasAlpha, Paint paint) {
        if (paint == null) {
            paint = mSmooth;
        } else {
            paint.setFilterBitmap(true);
        }
        delegate.drawBitmap(colors, offset, stride, x, y, width, height, hasAlpha, paint);
    }

    public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
        if (paint == null) {
            paint = mSmooth;
        } else {
            paint.setFilterBitmap(true);
        }
        delegate.drawBitmap(bitmap, matrix, paint);
    }

    public void drawBitmapMesh(
        Bitmap bitmap, int meshWidth, int meshHeight, float[] verts, int vertOffset, int[] colors, int colorOffset, Paint paint
    ) {
        delegate.drawBitmapMesh(bitmap, meshWidth, meshHeight, verts, vertOffset, colors, colorOffset, paint);
    }

    public void drawVertices(
        VertexMode mode, int vertexCount,
        float[] verts, int vertOffset,
        float[] texs, int texOffset, int[] colors, int
        colorOffset, short[] indices,
        int indexOffset, int indexCount, Paint paint
    ) {
        delegate.drawVertices(
            mode, vertexCount, verts, vertOffset, texs, texOffset, colors, colorOffset, indices, indexOffset, indexCount, paint
        );
    }

    public void drawText(char[] text, int index, int count, float x, float y, Paint paint) {
        delegate.drawText(text, index, count, x, y, paint);
    }

    public void drawText(String text, float x, float y, Paint paint) {
        delegate.drawText(text, x, y, paint);
    }

    public void drawText(String text, int start, int end, float x, float y, Paint paint) {
        delegate.drawText(text, start, end, x, y, paint);
    }

    public void drawText(CharSequence text, int start, int end, float x, float y, Paint paint) {
        delegate.drawText(text, start, end, x, y, paint);
    }

    public void drawPosText(char[] text, int index, int count, float[] pos, Paint paint) {
        delegate.drawPosText(text, index, count, pos, paint);
    }

    public void drawPosText(String text, float[] pos, Paint paint) {
        delegate.drawPosText(text, pos, paint);
    }

    public void drawTextOnPath(char[] text, int index, int count, Path path, float hOffset, float vOffset, Paint paint) {
        delegate.drawTextOnPath(text, index, count, path, hOffset, vOffset, paint);
    }

    public void drawTextOnPath(String text, Path path, float hOffset, float vOffset, Paint paint) {
        delegate.drawTextOnPath(text, path, hOffset, vOffset, paint);
    }

    public void drawPicture(Picture picture) {
        delegate.drawPicture(picture);
    }

    public void drawPicture(Picture picture, RectF dst) {
        delegate.drawPicture(picture, dst);
    }

    public void drawPicture(Picture picture, Rect dst) {
        delegate.drawPicture(picture, dst);
    }
}
