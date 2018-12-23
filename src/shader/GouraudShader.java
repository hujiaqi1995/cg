package shader;

import light.AmbientLight;
import light.DirectionalLight;
import light.Light;
import material.Texture;
import math.Vector3f;
import math.Vector4f;
import scene.RasterizationVertex;
import scene.Vertex;

/**
 * Gouraud着色器
 */
public class GouraudShader extends Shader {

    /// 下列向量，均处于世界空间中
    /// 将它们定义为类的成员，避免在光照计算时总是实例化新的对象。
    
    // 顶点坐标
    private Vector3f position = new Vector3f();
    // 顶点法线
    private Vector3f normal = new Vector3f();
    // 顶点到光源方向向量
    private Vector3f lightVector = new Vector3f();
    // 顶点到眼睛方向向量
    private Vector3f eyeVector = new Vector3f();
    // 光线和眼睛向量之间的半途向量，用于计算高光反射强度。
    private Vector3f halfVector = new Vector3f();
    
    // 光照颜色
    private Vector4f ambient = new Vector4f();
    private Vector4f diffuse = new Vector4f();
    private Vector4f specular = new Vector4f();
    
    private Vector3f color = new Vector3f();
    
    /**
     * 计算光照
     * @param vert
     * @param light
     */
    private Vector3f lighting(RasterizationVertex vert, Light light) {
        color.set(0, 0, 0);

        if (light instanceof AmbientLight) {
            // 环境光
            material.getAmbient().mult(light.getColor(), ambient);
            ambient.multLocal(light.getColor().w);
            return color.set(ambient.x, ambient.y, ambient.z);
        } else if (light instanceof DirectionalLight) {
            DirectionalLight dl = (DirectionalLight) light;
            
            // 顶点位置
            position.set(vert.position.x, vert.position.y, vert.position.z);
            // 顶点法线
            normal.set(vert.normal);
            
            // 计算顶点到光源的方向向量
            lightVector.set(dl.getDirection());
            lightVector.negateLocal();
            lightVector.normalizeLocal();
            
            // 计算顶点到眼睛的方向向量
            cameraPosition.subtract(position, eyeVector);
            eyeVector.normalizeLocal();
            
            // 计算光线和眼睛向量之间的半途向量，用于计算高光反射强度。
            lightVector.add(eyeVector, halfVector);
            halfVector.normalizeLocal();

            // 计算漫反射强度
            float kd = Math.max(normal.dot(lightVector), 0.0f);

            // 计算高光强度
            float ks = Math.max(normal.dot(halfVector), 0.0f);
            ks = (float) Math.pow(ks, material.getShininess());
            
            // 计算漫射光颜色
            material.getDiffuse().mult(light.getColor(), diffuse);
            diffuse.multLocal(kd);
            
            // 计算高光颜色
            material.getSpecular().mult(light.getColor(), specular);
            specular.multLocal(ks);
            
            // 计算光最终的颜色
            diffuse.addLocal(specular).multLocal(light.getColor().w);
            
            return color.set(diffuse.x, diffuse.y, diffuse.z);
        }
        
        return color;
    }
    
    @Override
    public RasterizationVertex vertexShader(Vertex vertex) {
        RasterizationVertex out = copy(vertex);

        // 顶点法线
        normalMatrix.mult(out.normal, out.normal);
        out.normal.normalizeLocal();

        // 顶点位置
        worldMatrix.mult(out.position, out.position);
        
        out.color.set(0, 0, 0, 1);
        
        // 计算光照
        for(int i=0; i < lights.size(); i++) {
            Light l = lights.get(i);
            Vector3f color = lighting(out, l);
            out.color.x += color.x;
            out.color.y += color.y;
            out.color.z += color.z;
        }
        
        // 模型-观察-透视 变换
        viewProjectionMatrix.mult(out.position, out.position);
        
        return out;
    }

    @Override
    public boolean fragmentShader(RasterizationVertex frag) {
        Texture texture = material.getDiffuseMap();
        if (texture != null) {
            Vector4f texColor = texture.sample2d(frag.texCoord);
            frag.color.multLocal(texColor);
        }
        
        return true;
    }

}
