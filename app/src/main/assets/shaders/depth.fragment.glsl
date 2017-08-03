precision mediump float;
varying vec4 v_position;

void main() {
    float depth = v_position.z / v_position.w;
    gl_FragColor = vec4(depth, depth, depth, 1);
}
