package renderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import light.Light;
import material.Material;
import material.RenderState.CullMode;
import math.ColorRGBA;
import math.Matrix3f;
import math.Matrix4f;
import math.Vector3f;
import scene.Geometry;
import scene.Mesh;
import scene.RasterizationVertex;
import scene.Vertex;
import shader.Shader;

/**
 * 渲染器
 */
public class Renderer {

    // 渲染图像
    private Image image;
    // 光栅器
    private SoftwareRaster raster;
    // 清屏颜色
    private ColorRGBA clearColor = ColorRGBA.WHITE;
    // 光源
    private List<Light> lights;
    
    /**
     * 初始化渲染器
     * @param width
     * @param height
     */
    public Renderer(int width, int height) {
        image = new Image(width, height);
        raster = new SoftwareRaster(this, image);
        
        // 计算视口变换矩阵
        updateViewportMatrix(width, height);
    }

    /**
     * 设置背景色
     * @param color
     */
    public void setBackgroundColor(ColorRGBA color) {
        if (color != null) {
            this.clearColor = color;
        }
    }
    
    /**
     * 使用背景色填充图像数据
     */
    public void clear() {
        raster.fill(clearColor);
        raster.clearDepthBuffer();
    }

    /**
     * 获得渲染好的图像
     * @return
     */
    public Image getRenderContext() {
        return image;
    }

    /**
     * 获得光栅器
     * @return
     */
    public ImageRaster getImageRaster() {
        return raster;
    }

    private Matrix4f worldMatrix = new Matrix4f();
    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f projectionMatrix = new Matrix4f();
    private Matrix4f viewProjectionMatrix = new Matrix4f();
    
    private Matrix4f worldViewMatrix = new Matrix4f();
    private Matrix4f worldViewProjectionMatrix = new Matrix4f();
    
    // 法向量变换矩阵
    private Matrix3f normalMatrix = new Matrix3f();
    // 摄像机位置
    private Vector3f cameraPosition = new Vector3f();
    
    private Matrix4f viewportMatrix = new Matrix4f();
    
    private Material material;
    
    /**
     * 视口变换矩阵
     */
    public void updateViewportMatrix(float width, float height) {
        float w = width * 0.5f;
        float h = height * 0.5f;
        
        // 把模型移到屏幕中心，并且按屏幕比例放大。
        float m00 = w, m01 = 0,  m02 = 0,  m03 = w;
        float m10 = 0, m11 = -h, m12 = 0,  m13 = h;
        float m20 = 0, m21 = 0,  m22 = 1f, m23 = 0;
        float m30 = 0, m31 = 0,  m32 = 0,  m33 = 1;
        
        viewportMatrix.set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
    }
    
    /**
     * 视口变换矩阵
     */
    public void updateViewportMatrix(float xmin, float ymin, float xmax, float ymax, float near, float far) {
        // 把模型移到屏幕中心，并且按屏幕比例放大。
        float m00 = (xmax - xmin) * 0.5f, m01 = 0,                     m02 = 0,                 m03 = (xmax + xmin) * 0.5f;
        float m10 = 0,                    m11 = -(ymax - ymin) * 0.5f, m12 = 0,                 m13 = (ymax + ymin) * 0.5f;
        float m20 = 0,                    m21 = 0,                     m22 = (far-near) * 0.5f, m23 = (far + near) * 0.5f;
        float m30 = 0,                    m31 = 0,                     m32 = 0,                 m33 = 1f;
        
        viewportMatrix.set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
    }

    /**
     * 渲染场景
     * @param geomList
     * @param camera
     */
    public void render(List<Geometry> geomList, Camera camera) {
        
        // 根据Camera初始化观察变换矩阵。
        viewMatrix.set(camera.getViewMatrix());
        projectionMatrix.set(camera.getProjectionMatrix());
        viewProjectionMatrix.set(camera.getViewProjectionMatrix());
        cameraPosition.set(camera.getLocation());
        
        // TODO 剔除那些不可见的物体
        
        // 遍历场景中的Mesh
        for(int i=0; i<geomList.size(); i++) {
            Geometry geom = geomList.get(i);
            
            // 根据物体的世界变换，计算MVP等变换矩阵。
            worldMatrix.set(geom.getWorldTransform().toTransformMatrix());
            viewMatrix.mult(worldMatrix, worldViewMatrix);
            viewProjectionMatrix.mult(worldMatrix, worldViewProjectionMatrix);
            
            // 计算法向量变换矩阵
            worldMatrix.toRotationMatrix(normalMatrix);
            // FIXME 先判断是否为正交矩阵，然后在决定是否要计算Invert、Transpose矩阵。
            normalMatrix.invertLocal();
            normalMatrix.transposeLocal();
            
            // TODO 使用包围体，剔除不可见物体
            
            // 渲染
            render(geom);
        }
    }

    /**
     * 渲染单个物体
     * @param geometry
     */
    protected void render(Geometry geometry) {

        // 设置材质
        this.material = geometry.getMaterial();
        // 设置渲染状态
        this.raster.setRenderState(material.getRenderState());

        // 设置着色器
        Shader shader = material.getShader();
        shader.setLights(lights);
        raster.setShader(shader);

        // 设置全局变量
        shader.setWorldMatrix(worldMatrix);
        shader.setViewMatrix(viewMatrix);
        shader.setProjectionMatrix(projectionMatrix);
        shader.setWorldViewMatrix(worldViewMatrix);
        shader.setViewProjectionMatrix(viewProjectionMatrix);
        shader.setWorldViewProjectionMatrix(worldViewProjectionMatrix);
        shader.setNormalMatrix(normalMatrix);
        shader.setCameraPosition(cameraPosition);

        // 提取网格数据
        Mesh mesh = geometry.getMesh();
        int[] indexes = mesh.getIndexes();
        Vertex[] vertexes = mesh.getVertexes();

        // 执行顶点着色器
        RasterizationVertex[] verts = new RasterizationVertex[vertexes.length];
        for(int i = 0; i<vertexes.length; i++) {
            verts[i] = shader.vertexShader(vertexes[i]);
        }

        // 临时变量
        // 用于保存变换后的向量坐标。
        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        // 用于光栅化
        RasterizationVertex out0 = new RasterizationVertex();
        RasterizationVertex out1 = new RasterizationVertex();
        RasterizationVertex out2 = new RasterizationVertex();

        // 遍历所有三角形
        for (int i = 0; i < indexes.length; i += 3) {

            int idx0 = indexes[i];
            int idx1 = indexes[i + 1];
            int idx2 = indexes[i + 2];

            // 在观察空间进行背面消隐
            worldViewMatrix.mult(vertexes[idx0].position, v0);
            worldViewMatrix.mult(vertexes[idx1].position, v1);
            worldViewMatrix.mult(vertexes[idx2].position, v2);

            if (cullBackFace(v0, v1, v2))
                continue;

            // 准备执行光栅化
            // 为了避免在光栅化阶段
            out0.copy(verts[idx0]);
            out1.copy(verts[idx1]);
            out2.copy(verts[idx2]);

            // 视锥体裁剪
            if (out0.isValid() && out1.isValid() && out2.isValid()) {
                raster.rasterizeTriangle(out0, out1, out2);
            } else {

                List<RasterizationVertex> vertices = new ArrayList<>();
                List<RasterizationVertex> auxillaryList = new ArrayList<>();

                vertices.add(out0);
                vertices.add(out1);
                vertices.add(out2);

                if (clipPolygonAxis(vertices, auxillaryList, 0) &&
                        clipPolygonAxis(vertices, auxillaryList, 1) &&
                        clipPolygonAxis(vertices, auxillaryList, 2)) {

                    RasterizationVertex initialVertex = vertices.get(0);
                    for(int j = 1; j < vertices.size() - 1; j++) {
                        raster.rasterizeTriangle(initialVertex, vertices.get(j), vertices.get(j+1));
                    }
                }
            }
        }
    }

    /**
     * 剔除背面
     * 
     * @param a
     * @param b
     * @param c
     * @return
     */
    protected boolean cullBackFace(Vector3f a, Vector3f b, Vector3f c) {

        // 计算ab向量
        Vector3f ab = b.subtract(a, a);

        // 计算bc向量
        Vector3f bc = c.subtract(b, b);

        // 计算表面法线
        Vector3f faceNormal = ab.crossLocal(bc);
        
        float dot = faceNormal.dot(c);

        CullMode cullMode = material.getRenderState().getCullMode();
        switch (cullMode) {
        case NEVER:
            return false;
        case ALWAYS:
            return true;
        case BACK:
            return dot >= 0;
        case FACE:
            return dot < 0;
        default:
            return false;
        }
    }
    
    /**
     * 使用Sutherland-Hodgman算法，进行多边形裁剪。将三角形的各边与视锥平面进行相交，计算交点。
     * @param vertices
     * @param auxillaryList
     * @param componentIndex
     * @return
     */
    private boolean clipPolygonAxis(List<RasterizationVertex> vertices, List<RasterizationVertex> auxillaryList,
            int componentIndex) {
        // 右边
        clipPolygonComponent(vertices, componentIndex, 1.0f, auxillaryList);
        vertices.clear();

        if(auxillaryList.isEmpty()) {
            return false;
        }

        // 左边
        clipPolygonComponent(auxillaryList, componentIndex, -1.0f, vertices);
        auxillaryList.clear();

        return !vertices.isEmpty();
    }
    
    private void clipPolygonComponent(List<RasterizationVertex> vertices, int componentIndex, 
            float componentFactor, List<RasterizationVertex> result) {
        RasterizationVertex previousVertex = vertices.get(vertices.size() - 1);
        
        float previousComponent = previousVertex.position.get(componentIndex) * componentFactor;
        boolean previousInside = previousComponent <= previousVertex.position.w;

        Iterator<RasterizationVertex> it = vertices.iterator();
        while(it.hasNext()) {
            RasterizationVertex currentVertex = it.next();
            float currentComponent = currentVertex.position.get(componentIndex) * componentFactor;
            boolean currentInside = currentComponent <= currentVertex.position.w;

            if(currentInside ^ previousInside) {
                float lerpAmt = (previousVertex.position.w - previousComponent) /
                    ((previousVertex.position.w - previousComponent) - 
                     (currentVertex.position.w - currentComponent));

                RasterizationVertex v = new RasterizationVertex();
                v.interpolateLocal(previousVertex, currentVertex, lerpAmt);
                result.add(v);
            }

            if(currentInside) {
                result.add(currentVertex);
            }

            previousVertex = currentVertex;
            previousComponent = currentComponent;
            previousInside = currentInside;
        }
}
    
    public Matrix4f getViewportMatrix() {
        return viewportMatrix;
    }
    
    public Matrix4f getViewProjectionMatrix() {
        return viewProjectionMatrix;
    }

    public Material getMaterial() {
        return material;
    }
    
    /**
     * 设置光源
     * @param lights
     */
    public void setLights(List<Light> lights) {
        this.lights = lights;
    }

}
