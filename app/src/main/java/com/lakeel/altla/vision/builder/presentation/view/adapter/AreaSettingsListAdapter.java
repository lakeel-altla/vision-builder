package com.lakeel.altla.vision.builder.presentation.view.adapter;

import com.lakeel.altla.android.binding.ViewBindingFactory;
import com.lakeel.altla.android.binding.converter.RelayConverter;
import com.lakeel.altla.android.binding.converter.ResourceToStringConverter;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.helper.DateFormatHelper;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaSettingsListPresenter;
import com.lakeel.altla.vision.builder.presentation.view.AreaSettingsItemView;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class AreaSettingsListAdapter extends RecyclerView.Adapter<AreaSettingsListAdapter.ViewHolder> {

    private final AreaSettingsListPresenter presenter;

    private RecyclerView recyclerView;

    private LayoutInflater inflater;

    private View selectedItem;

    public AreaSettingsListAdapter(@NonNull AreaSettingsListPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    @Override
    public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (inflater == null) {
            inflater = LayoutInflater.from(parent.getContext());
        }
        View itemView = inflater.inflate(R.layout.item_area_settings, parent, false);
        itemView.setOnClickListener(v -> {
            if (recyclerView != null) {
                int position = recyclerView.getChildAdapterPosition(itemView);

                if (selectedItem == null) {
                    selectedItem = itemView;
                    selectedItem.setSelected(true);
                    presenter.onItemSelected(position);
                } else {
                    if (selectedItem == itemView) {
                        itemView.setSelected(false);
                        presenter.onItemSelected(-1);
                    } else {
                        selectedItem.setSelected(false);
                        selectedItem = itemView;
                        selectedItem.setSelected(true);
                        presenter.onItemSelected(position);
                    }
                }
            }
        });
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemPresenter.onBind(position);
    }

    @Override
    public int getItemCount() {
        return presenter.getItemCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements AreaSettingsItemView {

        private AreaSettingsListPresenter.ItemPresenter itemPresenter;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemPresenter = presenter.createItemPresenter();

            ViewBindingFactory factory = new ViewBindingFactory(itemView);
            factory.create(R.id.text_view_updated_at, "text", itemPresenter.propertyUpdatedAt)
                   .converter(new RelayConverter(
                           value -> DateFormatHelper.format(itemView.getContext(), (long) value)))
                   .bind();
            factory.create(R.id.text_view_area_mode, "text", itemPresenter.propertyAreaMode)
                   .converter(new ResourceToStringConverter(itemView.getResources()))
                   .bind();
            factory.create(R.id.text_view_area_name, "text", itemPresenter.propertyAreaName).bind();
            factory.create(R.id.text_view_area_description_name, "text", itemPresenter.propertyAreaDescriptionName)
                   .bind();
        }
    }
}
