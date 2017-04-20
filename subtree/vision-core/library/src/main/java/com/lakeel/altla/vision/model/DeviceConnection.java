package com.lakeel.altla.vision.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import org.parceler.Parcel;
import org.parceler.Transient;

import android.support.annotation.NonNull;
import android.util.Log;

@Parcel(Parcel.Serialization.BEAN)
public final class DeviceConnection extends BaseEntity {

    private boolean online;

    @Transient
    private Object lastOnlineAt;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @NonNull
    public Object getLastOnlineAt() {
        if (lastOnlineAt == null) {
            Log.d("DeviceConnection", "set ServerValue.TIMESTAMP");
            lastOnlineAt = ServerValue.TIMESTAMP;
        }

        return lastOnlineAt;
    }

    @Exclude
    public long getLastOnlineAtAsLong() {
        if (lastOnlineAt instanceof Long) {
            return (Long) lastOnlineAt;
        } else {
            return -1;
        }
    }

    public void setLastOnlineAtAsLong(long lastOnlineAt) {
        this.lastOnlineAt = lastOnlineAt < 0 ? null : lastOnlineAt;
    }
}
