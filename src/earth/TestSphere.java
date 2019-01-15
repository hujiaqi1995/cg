package earth;

import material.Material;
import material.RenderState;
import material.Texture;
import math.Vector3f;
import math.Vector4f;
import renderer.Camera;
import renderer.Image;
import scene.Geometry;
import scene.Mesh;
import scene.shape.Box;
import scene.shape.Sphere;
import shader.UnshadedShader;

import java.awt.*;

/**
 * 测试Sphere网格
 */
public class TestSphere extends Application {

    private final static float PI = 3.1415926f;
    private final static float _2PI = PI * 2;
    private float angle = 0;// 旋转角度

    private Geometry geom;

    public static void main(String[] args) {
        TestSphere app = new TestSphere();
        app.setResolution(800, 600);
        app.setTitle("Test earth");
        app.setFrameRate(60);
        app.start();
    }

    @Override
    protected void initialize() {
        // 初始化摄像机
        Camera cam = getCamera();
        cam.lookAt(new Vector3f(3, 4, 5), Vector3f.ZERO, Vector3f.UNIT_Y);

        // 创建网格
        Mesh mesh = new Sphere(2f, 36, 32);

        // 创建材质
        Material material = new Material();
        this.geom = new Geometry(mesh, material);

        // 设置颜色
        material.setDiffuse(new Vector4f(1, 1, 1, 1));

        try {
            material.setDiffuseMap(new Texture(new Image("res/map1.jpg")));
        } catch (Exception e){
            material.setDiffuseMap(new Texture());
        }

        // 添加到场景中
        Geometry geometry = new Geometry(mesh, material);
//        rootNode.attachChild(geometry);
        rootNode.attachChild(geom);

        // 设置着色器
        material.getRenderState().setFillMode(RenderState.FillMode.LINE);
        material.setShader(new UnshadedShader());
    }

    @Override
    protected void update(float delta) {
        angle += delta * PI;

        // 若已经旋转360°，则减去360°。
        if (angle > _2PI) {
            angle -= _2PI;
        }

        // 计算旋转：绕Z轴顺时针方向旋转
        geom.getLocalTransform().getRotation().fromAxisAngle(Vector3f.UNIT_Y, -angle);
    }
}