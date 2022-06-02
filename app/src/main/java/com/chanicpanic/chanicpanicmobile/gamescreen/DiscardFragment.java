/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chanicpanic.chanicpanicmobile.R;
import com.chanicpanic.chanicpanicmobile.game.Game;

public class DiscardFragment extends CardFragment {
    private CardGroupView discardPile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btnCloseDiscard).setOnClickListener(v -> ((GameScreen) getContext()).setDiscardPileDisplay(false));

        discardPile = view.findViewById(R.id.discardPileGroup);
        discardPile.setCardGroup(Game.getInstance().getDiscardPile());
    }

    public void update() {
        discardPile.getCardViewAdapter().notifyItemInserted(0);
    }

}
