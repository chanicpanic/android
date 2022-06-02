/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.chanicpanic.chanicpanicmobile.R;
import com.chanicpanic.chanicpanicmobile.game.Game;
import com.chanicpanic.chanicpanicmobile.game.abilities.Ability;

public class SpinnerFragment extends Fragment {
    private LinearLayout abilityButtons;
    private FrameLayout fakeSpinner;
    private BoardSpinnerAdapter adapter;
    private int spinnerPosition;
    private GameScreen gameScreen;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        return inflater.inflate(R.layout.fragment_spinner, container, false);
    }

    public static SpinnerFragment newInstance() {
        return new SpinnerFragment();
    }

    public void setSpinnerPosition(int position) {
        spinnerPosition = position;

        View convertView = fakeSpinner.getChildAt(0);
        fakeSpinner.removeAllViews();
        fakeSpinner.addView(adapter.getRegularView(position, convertView, fakeSpinner));


        abilityButtons.removeAllViews();
        for (final Ability ability : Game.getInstance().getPlayer((((GameScreen) getContext()))
                .getBoardShown()).getAbilities()) {

            LayoutInflater.from(getContext()).inflate(R.layout.button_ability, abilityButtons);
            Button button = (Button) abilityButtons.getChildAt(abilityButtons.getChildCount() - 1);

            button.setText(String.valueOf(ability.getName().charAt(0)));

            button.setOnClickListener(v -> ((GameScreen) getContext()).setAbilityDisplay(ability));

        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        gameScreen = (GameScreen) getContext();

        adapter = new BoardSpinnerAdapter(getContext(), Game.getInstance().getPlayers());

        spinnerPosition = gameScreen.getBoardShown();
        fakeSpinner = view.findViewById(R.id.fakeSpinner);
        fakeSpinner.addView(adapter.getRegularView(spinnerPosition, null, fakeSpinner));

        fakeSpinner.setOnClickListener(v -> {
            ListView list = new ListView(gameScreen);
            list.setAdapter(adapter);
            list.setBackgroundColor(Color.WHITE);

            GameScreen.DialogBuilder.addPopup(gameScreen, list, true);

            list.setOnItemClickListener((parent, view1, position, id) -> {
                GameScreen.DialogBuilder.removeTopPopup(gameScreen);
                gameScreen.getPager().setCurrentItem(position, true);
            });
        });

        abilityButtons = view.findViewById(R.id.abilityButtons);
        for (final Ability ability : Game.getInstance().getPlayer((((GameScreen) getContext()))
                .getBoardShown()).getAbilities()) {

            LayoutInflater.from(getContext()).inflate(R.layout.button_ability, abilityButtons);
            Button button = (Button) abilityButtons.getChildAt(abilityButtons.getChildCount() - 1);

            button.setText(String.valueOf(ability.getName().charAt(0)));

            button.setOnClickListener(v -> ((GameScreen) getContext()).setAbilityDisplay(ability));

        }
    }


    public void setEnabled(boolean enabled) {
        fakeSpinner.setEnabled(enabled);
        for (int i = 0, z = abilityButtons.getChildCount(); i < z; ++i) {
            abilityButtons.getChildAt(i).setEnabled(false);
        }
    }

    public void update() {
        if (spinnerPosition < Game.getInstance().getPlayerCount()) {
            View convertView = fakeSpinner.getChildAt(0);
            fakeSpinner.removeAllViews();
            fakeSpinner.addView(adapter.getRegularView(spinnerPosition, convertView, fakeSpinner));
        }
    }
}