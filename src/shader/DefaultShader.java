package shader;

import material.Texture;
import math.Vector4f;
import scene.RasterizationVertex;
import scene.Vertex;

/**
 * 默认着色器
 */
public class DefaultShader extends Shader {

    @Override
    public RasterizationVertex vertexShader(Vertex vertex) {
        RasterizationVertex out = copy(vertex);

        // 模型-观察-透视 变换
        worldViewProjectionMatrix.mult(out.position, out.position);
        
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
