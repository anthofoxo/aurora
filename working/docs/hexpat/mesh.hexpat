struct Vertex {
    float position[3];
    float normal[3];
    float texcoord[2];
    u8 color[4];
};

struct Triangle {
    u16 indices[3];
};

struct Mesh {
    u32 vertexCount;
    Vertex vertices[vertexCount];
    u32 triangleCount;
    Triangle triangles[triangleCount];
    u16 unknownField; // typically 1
};

struct XFile {
    u32 header;
    u32 meshCount;
    Mesh meshes[meshCount];
};