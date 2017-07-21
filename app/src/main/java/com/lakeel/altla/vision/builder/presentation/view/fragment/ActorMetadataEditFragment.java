package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.model.Actor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class ActorMetadataEditFragment extends Fragment {

    @Inject
    ArModel arModel;

    @BindView(R.id.text_input_edit_text_name)
    TextInputEditText textInputEditTextName;

    private FragmentContext fragmentContext;

    @NonNull
    public static ActorMetadataEditFragment newInstance() {
        return new ActorMetadataEditFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ActivityScopeContext) context).getActivityComponent().inject(this);
        fragmentContext = (FragmentContext) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_actor_metadata_edit, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentContext = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentContext.setTitle(R.string.title_actor_metadata_edit_view);
        fragmentContext.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        setHasOptionsMenu(true);

        final Actor actor = arModel.getSelectedActor();
        if (actor == null) throw new IllegalStateException("No actor is selected.");

        textInputEditTextName.setText(actor.getName());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_actor_metadata_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                final Actor actor = arModel.getSelectedActor();
                if (actor == null) throw new IllegalStateException("No actor is selected.");

                actor.setName(textInputEditTextName.getText().toString());

                arModel.saveSelectedActor();

                fragmentContext.backView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface FragmentContext {

        void setTitle(@StringRes int resId);

        void setHomeAsUpIndicator(@DrawableRes int resId);

        void backView();
    }
}
