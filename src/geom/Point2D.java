package geom;
import math.ColorRGBA;
import renderer.ImageRaster;

/**
 * 代表一个2D点。
 */
public class Point2D implements Drawable{

    public int x, y;
    public ColorRGBA color;
    
    public void draw(ImageRaster raster) {
        raster.drawPixel(x, y, color);
    }
}
