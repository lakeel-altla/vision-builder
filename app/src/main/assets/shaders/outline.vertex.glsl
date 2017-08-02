attribute vec4 a_position;
attribute vec4 a_normal;
uniform mat4 u_viewWorldTrans;
uniform mat4 u_projViewWorldTrans;
uniform float u_outlineWidth;

void main() {
//    float distance = (u_viewWorldTrans * a_position).z;
//    vec4 position = a_position + a_normal * -distance * u_outlineWidth;
    vec4 position = a_position;
    position.xyz += a_normal.xyz * u_outlineWidth;
    gl_Position = u_projViewWorldTrans * position;
}
