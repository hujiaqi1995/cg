package light;

import math.Vector4f;

/**
 * 光源
 */
public abstract class Light {

    // 光源的颜色
    protected Vector4f color;

    public Light() {
        color = new Vector4f(1, 1, 1, 1);
    }

    public Light(Vector4f color) {
        this.color = color;
    }

    public Vector4f getColor() {
        return color;
    }

    public void setColor(Vector4f color) {
        this.color = color;
    }
    
}
