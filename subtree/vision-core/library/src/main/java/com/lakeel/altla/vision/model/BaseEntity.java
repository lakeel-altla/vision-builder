package com.lakeel.altla.vision.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import org.parceler.Transient;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Objects;
import java.util.UUID;

public class BaseEntity {

    public static final String FIELD_ID = "id";

    public static final String FIELD_USER_ID = "userId";

    public static final String FIELD_GROUP_ID = "groupId";

    public static final String FIELD_CREATED_AT = "createdAt";

    public static final String FIELD_UPDATED_AT = "updatedAt";

    private String id;

    private String userId;

    private String groupId;

    private Object createdAt;

    private Object updatedAt;

    protected BaseEntity() {
    }

    @NonNull
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public String getUserId() {
        return userId;
    }

    public void setUserId(@Nullable String userId) {
        this.userId = userId;
    }

    @Exclude
    @Transient
    @NonNull
    public String getRequiredUserId() {
        if (userId == null) throw new IllegalStateException("'userId' is null.");
        return userId;
    }

    @Nullable
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(@Nullable String groupId) {
        this.groupId = groupId;
    }

    @NonNull
    public Object getCreatedAt() {
        if (createdAt == null) {
            createdAt = ServerValue.TIMESTAMP;
        }

        return createdAt;
    }

    @NonNull
    public Object getUpdatedAt() {
        if (updatedAt == null) {
            updatedAt = ServerValue.TIMESTAMP;
        }

        return updatedAt;
    }

    @Exclude
    public long getCreatedAtAsLong() {
        if (createdAt instanceof Long) {
            return (Long) createdAt;
        } else {
            return -1;
        }
    }

    public void setCreatedAtAsLong(long createdAt) {
        this.createdAt = createdAt < 0 ? null : createdAt;
    }

    @Exclude
    public long getUpdatedAtAsLong() {
        if (updatedAt instanceof Long) {
            return (Long) updatedAt;
        } else {
            return -1;
        }
    }

    public void setUpdatedAtAsLong(long updatedAt) {
        this.updatedAt = updatedAt < 0 ? null : updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
