attribute vec4 a_position;
uniform mat4 u_projViewWorldTrans;
varying vec4 v_position;

void main() {
    gl_Position = u_projViewWorldTrans * a_position;
    v_position = a_position;
}
