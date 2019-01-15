package scene;

import math.Vector2f;
import math.Vector3f;
import math.Vector4f;

/**
 * 定义三角形网格
 */
public class Mesh {
    /**
     * 顶点数据
     */
    protected Vertex[] vertexes;
    /**
     * 顶点索引
     */
    protected int[] indexes;


    public Vertex[] getVertexes() {
        return vertexes;
    }
    
    public int[] getIndexes() {
        return indexes;
    }
    
    public Mesh() {
    }
    
    public Mesh(Vector3f[] positions, int[] indexes) {
        this(positions, indexes, null, null, null);
    }
    
    public Mesh(Vector3f[] positions, int[] indexes, Vector2f[] texCoords, Vector3f[] normals, Vector4f[] colors) {
        this.indexes = indexes;
        this.vertexes = new Vertex[positions.length];
        for(int i = 0; i < indexes.length; i++) {
            int index = indexes[i];
            vertexes[index] = new Vertex();
            vertexes[index].position = positions[index];
            if (normals != null) {
                vertexes[index].normal = normals[index];
            }
            if (colors != null) {
                vertexes[index].color = colors[index];
            }
            if (texCoords != null) {
                vertexes[index].texCoord = texCoords[index];
            }
        }
    }
}