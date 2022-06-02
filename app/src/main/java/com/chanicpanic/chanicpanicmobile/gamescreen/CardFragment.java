/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chanicpanic.chanicpanicmobile.R;
import com.chanicpanic.chanicpanicmobile.menu.MainActivity;
import com.chanicpanic.chanicpanicmobile.menu.InstructionsExpandableListAdapter;
import com.chanicpanic.chanicpanicmobile.game.Game;
import com.chanicpanic.chanicpanicmobile.game.abilities.SeerAbility;

/**
 * This fragment displays deck and discard information, as well as buttons
 * to access the game log and return to the main menu.
 */
public class CardFragment extends Fragment {
    /**
     * field to hold the deck/discard TextView
     */
    private TextView discard;

    /**
     * field to hold the CardView that displays the top card of the discard pile
     */
    private CardView discardPile;

    private CardView deckView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_card, container, false);
        discard = v.findViewById(R.id.lblDiscard);
        deckView = v.findViewById(R.id.viewDeck);
        discardPile = v.findViewById(R.id.viewDiscard);
        discardPile.setSelectable(true);
        discardPile.setClickable(true);
        discardPile.setCardSelected(false);
        update();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // when the discard pile is clicked, this fragment is replaced with the DiscardFragment
        discardPile.setOnClickListener(v -> ((GameScreen) getContext()).setDiscardPileDisplay(true));

        if (Game.getInstance().getPlayer().hasSpecialAbility(SeerAbility.class)) {
            deckView.setCard(Game.getInstance().getDeck().get(0), true);
            deckView.setAlpha(.5f);
        } else {
            deckView.setCard(Game.getInstance().getDeck().get(0), false);
            deckView.setAlpha(1f);
        }

        // show confirmation dialog for returning to the main menu
        view.findViewById(R.id.btnMenu).setOnClickListener(v -> (new GameScreen.DialogBuilder((GameScreen) getContext()))
                .setTitle(getString(R.string.dialog_menu_title))
                .setMessage(getString(R.string.dialog_menu_message))
                .setPositiveButton(getString(R.string.yes), v13 -> {
                    Game.getInstance().clear();

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(intent);
                    ((GameScreen) getContext()).finish();

                })
                .setNegativeButton(getString(R.string.no), null)
                .setCancelable(true)
                .setFullScreen(false)
                .show());

        // show the dialog for the game log
        view.findViewById(R.id.btnLog).setOnClickListener(v -> {
            final ViewGroup gameFrame = ((GameScreen) getContext()).findViewById(R.id.gameScreenFrame);
            View view12 = getLayoutInflater().inflate(R.layout.dialog_game_log, gameFrame);

            ((GameScreen) getContext()).configureLog(view12);

            view12.findViewById(R.id.btnCloseLog).setOnClickListener(v12 -> gameFrame.removeViewAt(gameFrame.getChildCount() - 1));
        });

        view.findViewById(R.id.btnHelp).setOnClickListener(v -> {
            final ViewGroup gameFrame = ((GameScreen) getContext()).findViewById(R.id.gameScreenFrame);
            View view1 = getLayoutInflater().inflate(R.layout.dialog_instructions, gameFrame);

            ((ExpandableListView) view1.findViewById(R.id.listInstructions)).setAdapter(new InstructionsExpandableListAdapter(getContext()));

            view1.findViewById(R.id.btnCloseInstructions).setOnClickListener(v1 -> gameFrame.removeViewAt(gameFrame.getChildCount() - 1));
        });
    }

    /**
     * This updates the display for the number of cards in the deck/discard pile
     * as well as the top card of the discard pile
     */
    public void update() {
        discard.setText(String.format(getString(R.string.deck) + " %d\n\n" + getString(R.string
                .discard_pile) + " %d", Game.getInstance().getDeck().getSize(), Game.getInstance
                ().getDiscardPile().getSize()));
        if (!Game.getInstance().getDiscardPile().isEmpty()) {
            discardPile.setCard(Game.getInstance().getDiscardPile().get(0));
        } else {
            discardPile.setBackgroundResource(R.drawable.background_card_border);
            discardPile.setText("");
        }

        if (Game.getInstance().getPlayer().hasSpecialAbility(SeerAbility.class)) {
            deckView.setCard(Game.getInstance().getDeck().get(0), true);
            deckView.setAlpha(.5f);
        } else {
            deckView.setCard(Game.getInstance().getDeck().get(0), false);
            deckView.setAlpha(1f);
        }
    }
}