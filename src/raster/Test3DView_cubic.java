package raster;

import earth.Application;
import material.Material;
import material.RenderState;
import math.Vector3f;
import renderer.Camera;
import scene.Geometry;
import scene.Mesh;
import scene.shape.Box;

public class Test3DView_cubic extends Application {

    private final static float PI = 3.1415926f;
    private final static float _2PI = PI * 2;
    private float angle = 0;// 旋转角度

    private Geometry geom;

    @Override
    protected void initialize() {
        Mesh mesh = new Box();

        // 材质
        Material material = new Material();

        // 添加到场景中
        this.geom = new Geometry(mesh, material);
        rootNode.attachChild(geom);
//        material.getRenderState().setFillMode(RenderState.FillMode.LINE);
        // 调整摄像机的位置
        Camera cam = getCamera();
        cam.lookAt(new Vector3f(3, 4, 8), Vector3f.ZERO, Vector3f.UNIT_Y);
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

    public static void main(String[] args) {
        Test3DView_cubic app = new Test3DView_cubic();
        app.setResolution(800, 600);
        app.setTitle("3D View");
        app.setFrameRate(60);
        app.start();
    }
}
