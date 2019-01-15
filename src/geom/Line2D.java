package geom;

import math.ColorRGBA;
import renderer.ImageRaster;

/**
 * 代表一条线段。
 */
public class Line2D implements Drawable {

    public int x0, y0;
    public int x1, y1;
    public ColorRGBA color = ColorRGBA.RED;

    @Override
    public void draw(ImageRaster imageRaster) {
        imageRaster.drawLine(x0, y0, x1, y1, color);
    }

}
