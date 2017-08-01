package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.model.TriggerShapeListModel;
import com.lakeel.altla.vision.model.TriggerShape;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class TriggerShapeListPane extends Pane {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private final PaneContext paneContext;

    private final Adapter adapter = new Adapter();

    private final TriggerShapeListModel triggerShapeListModel = new TriggerShapeListModel();

    public TriggerShapeListPane(@NonNull Activity activity) {
        super(activity, R.id.pane_trigger_shape_list);

        ((ActivityScopeContext) activity).getActivityComponent().inject(this);

        paneContext = (PaneContext) activity;

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
    }

    @OnClick(R.id.image_button_close)
    void onClickClose() {
        paneContext.showEditModeMenuPane();
    }

    public interface PaneContext {

        void onTriggerShapeSelected(@Nullable TriggerShape triggerShape);

        void showEditModeMenuPane();
    }

    final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        View selectedItemView;

        LayoutInflater inflater;

        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            final View itemView = inflater.inflate(R.layout.item_image_asset, parent, false);
            itemView.setOnClickListener(v -> {
                if (selectedItemView != null) {
                    selectedItemView.setSelected(false);
                }

                selectedItemView = (selectedItemView == v) ? null : v;

                int selectedPosition = -1;
                if (selectedItemView != null) {
                    selectedItemView.setSelected(true);
                    selectedPosition = recyclerView.getChildAdapterPosition(selectedItemView);
                }

                triggerShapeListModel.setSelectedPosition(selectedPosition);
                paneContext.onTriggerShapeSelected(triggerShapeListModel.getSelectedItem());
            });

            return new Adapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            final TriggerShape triggerShape = triggerShapeListModel.getItem(position);
            holder.textViewName.setText(triggerShape.name());
        }

        @Override
        public int getItemCount() {
            return triggerShapeListModel.getItemCount();
        }

        final class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.text_view_name)
            TextView textViewName;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
