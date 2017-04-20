package com.lakeel.altla.vision.builder.presentation.helper;

import com.lakeel.altla.vision.builder.R;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.ImageView;

public final class ThumbnailLoader {

    private final Context context;

    public ThumbnailLoader(@NonNull Context context) {
        this.context = context;
    }

    public void load(@NonNull Uri uri, @NonNull ImageView imageView) {
        Drawable placeholderDrawable = context.getResources().getDrawable(R.drawable.progress_animation);
        int placeholderTint = context.getResources().getColor(R.color.tint_progress);
        placeholderDrawable.setColorFilter(placeholderTint, PorterDuff.Mode.SRC_ATOP);

        Picasso picasso = Picasso.with(context);
        picasso.setIndicatorsEnabled(true);
        picasso.setLoggingEnabled(true);

        picasso.load(uri)
               .placeholder(placeholderDrawable)
               .error(R.drawable.ic_clear_black_24dp)
               .into(imageView);
    }
}
