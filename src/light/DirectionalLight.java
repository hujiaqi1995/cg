package light;

import math.Vector3f;
import math.Vector4f;

/**
 * 定向光源（平行光）
 */
public class DirectionalLight extends Light{

    // 光照方向
    protected Vector3f direction;

    public DirectionalLight() {
        super();
        this.direction = new Vector3f(0, 0, -1);
    }
    
    public DirectionalLight(Vector3f direction) {
        super();
        this.direction = direction;
    }
    
    public DirectionalLight(Vector4f color) {
        super(color);
        this.direction = new Vector3f(0, 0, -1);
    }
    
    public DirectionalLight(Vector4f color, Vector3f direction) {
        super(color);
        this.direction = direction;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }
    
}
