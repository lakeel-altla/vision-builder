attribute vec3 a_position;
attribute vec3 a_normal;
uniform mat4 u_viewWorldTrans;
uniform mat4 u_projViewWorldTrans;
uniform mat3 u_normalMatrix;
uniform mat4 u_projTrans;
uniform float u_outlineWidth;

void main() {
//    float distance = (u_viewWorldTrans * a_position).z;
//    vec4 position = a_position + a_normal * -distance * u_outlineWidth;

    vec4 position = u_projViewWorldTrans * vec4(a_position, 1);
    vec3 normal = normalize(u_normalMatrix * a_normal);
    normal.x *= u_projTrans[0][0];
    normal.y *= u_projTrans[1][1];
    position.xy += normal.xy * u_outlineWidth /** -position.z*/;
    gl_Position = position;

//    gl_Position = u_projViewWorldTrans * vec4(a_position + a_normal * u_outlineWidth, 1);
}
