package com.lakeel.altla.vision.model;

public abstract class MeshComponent extends Component {

    private boolean visible;

    private boolean visibleAtRuntime;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisibleAtRuntime() {
        return visibleAtRuntime;
    }

    public void setVisibleAtRuntime(boolean visibleAtRuntime) {
        this.visibleAtRuntime = visibleAtRuntime;
    }
}
