/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chanicpanic.chanicpanicmobile.R;
import com.chanicpanic.chanicpanicmobile.game.Game;
import com.chanicpanic.chanicpanicmobile.game.abilities.Ability;
import com.chanicpanic.chanicpanicmobile.game.abilities.ActiveAbility;

/**
 * This fragment displays an ability description as well as an activate button
 */
public class AbilityFragment extends Fragment {
    private TextView abilityDescription;
    private Button activate;
    private ImageButton close;
    private ViewGroup viewGroup;
    private Ability ability;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ability, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // get Views
        viewGroup = (ViewGroup) view;
        abilityDescription = view.findViewById(R.id.abilityDescriptionTextView);
        activate = view.findViewById(R.id.btnActivate);
        close = view.findViewById(R.id.btnClose);
        update();
    }

    /**
     * @param ability the ability this fragment displays
     */
    public void setAbility(Ability ability) {
        this.ability = ability;
    }

    /**
     * updates the View based on the ability displayed
     */
    public void update() {
        // get description
        abilityDescription.setText(ability.getName() + ":\n" + ability.getDescription());

        // show activate if this ability is active and it is the turn player's ability
        activate.setVisibility(ability.getGameScreen().getBoardShown() == Game.getInstance().getTurn() && ability.isActive() ? View.VISIBLE : View.INVISIBLE);
        activate.setEnabled(ability.isActive() && ((ActiveAbility) ability).isActivateable());

        activate.setOnClickListener(v -> {
            v.setVisibility(View.INVISIBLE);
            ((ActiveAbility) ability).activate();
        });

        close.setOnClickListener(v -> {
            if (ability.isActive()) {
                ((ActiveAbility) ability).deactivate();
            }
            ability.getGameScreen().setAbilityDisplay(null);

        });
    }

    public void updateActivateAbility() {
        activate.setEnabled(ability.isActive() && ((ActiveAbility) ability).isActivateable());
    }

    public void disableCancellation(String message) {
        close.setVisibility(View.GONE);
        activate.setVisibility(View.GONE);
        TextView textView = new TextView(getContext());
        textView.setText(message);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3));
        int padding = (int) getContext().getResources().getDimension(R.dimen.default_margin);
        textView.setPadding(padding, 0, padding, 0);
        viewGroup.addView(textView);
    }

    public void disable() {
        activate.setEnabled(false);
        close.setEnabled(false);
    }
}