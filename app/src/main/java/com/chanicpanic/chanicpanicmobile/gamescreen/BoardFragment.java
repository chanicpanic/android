/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.chanicpanic.chanicpanicmobile.R;
import com.chanicpanic.chanicpanicmobile.game.Attack;
import com.chanicpanic.chanicpanicmobile.game.Board;
import com.chanicpanic.chanicpanicmobile.game.Card;
import com.chanicpanic.chanicpanicmobile.game.Game;

import java.util.EnumSet;

/**
 * This fragment displays a board and is used in a ViewPager
 */
public class BoardFragment extends Fragment {
    /**
     * the effective turn of the player who owns this board
     */
    private int board;

    private boolean attack;

    private CardGroupView heartGroup;
    private CardGroupView diamondGroup;
    private CardGroupView spadeGroup;

    public ViewGroup v;

    public static GradientDrawable heartBackground;
    public static GradientDrawable diamondBackground;
    public static GradientDrawable spadeBackground;

    public static void loadBackgrounds(Context context) {
        heartBackground = new GradientDrawable();
        heartBackground.setStroke((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics()), Color.BLACK);
        heartBackground.setColors(new int[]{0xffffffff, 0xffff0000});
        heartBackground.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        heartBackground.setShape(GradientDrawable.RECTANGLE);
        heartBackground.setGradientRadius(context.getResources().getDimension(R.dimen._115sdp));

        diamondBackground = new GradientDrawable();
        diamondBackground.setStroke((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics()), Color.BLACK);
        diamondBackground.setColors(new int[]{0xffffffff, 0xff0400ff});
        diamondBackground.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        diamondBackground.setShape(GradientDrawable.RECTANGLE);
        diamondBackground.setGradientRadius(context.getResources().getDimension(R.dimen._115sdp));

        spadeBackground = new GradientDrawable();
        spadeBackground.setStroke((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics()), Color.BLACK);
        spadeBackground.setColors(new int[]{0xffffffff, 0xff000000});
        spadeBackground.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        spadeBackground.setShape(GradientDrawable.RECTANGLE);
        spadeBackground.setGradientRadius(context.getResources().getDimension(R.dimen._115sdp));
    }

    /**
     * @param board the effective turn of the player who owns this board
     * @return a new BoardFragment
     */
    public static BoardFragment newInstance(int board, boolean attack) {
        Bundle bundle = new Bundle();
        bundle.putInt("board", board);
        bundle.putBoolean("attack", attack);

        BoardFragment fragment = new BoardFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * gets the board value from the bundle
     *
     * @param bundle the bundle to read
     */
    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            board = bundle.getInt("board");
            attack = bundle.getBoolean("attack");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = (ViewGroup) inflater.inflate(R.layout.fragment_board, container, false);

        // get board
        readBundle(getArguments());

        // change orientation based on whose turn it is
        if (board == Game.getInstance().getTurn() && !((GameScreen) v.getContext()).isAutoplay()) {
            v.findViewById(R.id.topBackground).setBackground(spadeBackground);
            v.findViewById(R.id.bottomBackground).setBackground(heartBackground);
        } else {
            v.findViewById(R.id.topBackground).setBackground(heartBackground);
            v.findViewById(R.id.bottomBackground).setBackground(spadeBackground);
        }
        v.findViewById(R.id.middleBackground).setBackground(diamondBackground);
        return v;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        diamondGroup = view.findViewById(R.id.middle);
        if (board == Game.getInstance().getTurn()) {
            heartGroup = view.findViewById(R.id.bottom);
            spadeGroup = view.findViewById(R.id.top);
        } else {
            heartGroup = view.findViewById(R.id.top);
            spadeGroup = view.findViewById(R.id.bottom);
        }

        if (attack) {
            transition(true, false);
        } else {
            heartGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.HEARTS), CardView.POSITION_BOARD);
            diamondGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.DIAMONDS), CardView.POSITION_BOARD);
            spadeGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.SPADES), CardView.POSITION_BOARD);
        }

        heartGroup.setScrollable(false);
        diamondGroup.setScrollable(false);
        spadeGroup.setScrollable(false);
    }

    /**
     * updates the appropriate CardGroupView based on the suit of the card played
     *
     * @param suit the suit of the card played
     */
    public void play(Card.Suit suit) {
        Board board = Game.getInstance().getBoard(this.board);
        switch (suit) {
            case HEARTS:
                heartGroup.getCardViewAdapter().newCard(board.getRow(Card.Suit.HEARTS).get(board.rowSize(Card.Suit.HEARTS) - 1));
                heartGroup.getAdapter().notifyItemInserted(board.rowSize(Card.Suit.HEARTS) - 1);
                break;
            case DIAMONDS:
                diamondGroup.getCardViewAdapter().newCard(board.getRow(Card.Suit.DIAMONDS).get(board.rowSize(Card.Suit.DIAMONDS) - 1));
                diamondGroup.getAdapter().notifyItemInserted(board.rowSize(Card.Suit.DIAMONDS) - 1);
                break;
            case SPADES:
                spadeGroup.getCardViewAdapter().newCard(board.getRow(Card.Suit.SPADES).get(board.rowSize(Card.Suit.SPADES) - 1));
                spadeGroup.getAdapter().notifyItemInserted(board.rowSize(Card.Suit.SPADES) - 1);
                break;
        }
    }

    public void transition(boolean attack, boolean transition) {
        if (!transition) {
            v.setLayoutTransition(null);
        } else {
            v.setLayoutTransition(new LayoutTransition());
        }

        final SwitchCompat bypass = v.findViewById(R.id.swtBypass);

        bypass.setOnCheckedChangeListener(null);
        bypass.setChecked(false);

        View layout = v.findViewById(R.id.boardSwitches);

        final SwitchCompat auto = v.findViewById(R.id.swtAutoSelect);

        if (attack) {
            heartGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.HEARTS), true, true, false, 3, CardView.POSITION_BOARD, board);
            diamondGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.DIAMONDS), true, true, false, 3, CardView.POSITION_BOARD, board);
            spadeGroup.setCardGroup(Game.getInstance().getBoard(Game.getInstance().getTurn()).getRow(Card.Suit.SPADES), true, true, true, 3, CardView.POSITION_BOARD, board);

            if (Game.getInstance().getRound() >= Game.BYPASS_ROUND) {
                bypass.setVisibility(View.VISIBLE);
                bypass.setEnabled(Game.getInstance().getPlayer(board).isBypassableBy(Game.getInstance().getPlayer()));
            } else {
                bypass.setEnabled(false);
            }

            layout.setVisibility(View.VISIBLE);

            final GameScreen gameScreen = ((GameScreen) getContext());

            if (!gameScreen.isAutoSelect()) {
                gameScreen.updateAttack();
                updateAttack(gameScreen.getAttack());
            }

            bypass.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (gameScreen.isAutoSelect()) {
                    gameScreen.updateAttack();
                } else {
                    diamondGroup.getCardViewAdapter().clearSelection();
                    heartGroup.getCardViewAdapter().clearSelection();
                    gameScreen.updateAttack(null, true, true);
                }
            });

            auto.setOnCheckedChangeListener((buttonView, isChecked) -> {
                gameScreen.setAutoSelect(isChecked);

                if (isChecked) {
                    diamondGroup.getCardViewAdapter().setSelectable(true, false);
                    heartGroup.getCardViewAdapter().setSelectable(true, false);

                    // when turned on
                    gameScreen.updateAttack();

                } else {
                    gameScreen.updateAttack();

                    // show possible selections based on currently selected spades
                    updateAttack(gameScreen.getAttack());
                }
            });
        } else {
            heartGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.HEARTS), CardView.POSITION_BOARD);
            diamondGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.DIAMONDS), CardView.POSITION_BOARD);
            spadeGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.SPADES), CardView.POSITION_BOARD);
            layout.setVisibility(View.GONE);
        }
    }

    public void setSelectionMode(boolean selectable, int maxSelectableCards, int maxSelectableValue, EnumSet<Card.Suit> selectableSuits) {

        if (selectable)
        {
            heartGroup.getCardViewAdapter().setSelectableCards(maxSelectableCards, maxSelectableValue, selectableSuits);
            diamondGroup.getCardViewAdapter().setSelectableCards(maxSelectableCards, maxSelectableValue, selectableSuits);
            spadeGroup.getCardViewAdapter().setSelectableCards(maxSelectableCards, maxSelectableValue, selectableSuits);

            heartGroup.getCardViewAdapter().clearSelection();
            diamondGroup.getCardViewAdapter().clearSelection();
            spadeGroup.getCardViewAdapter().clearSelection();

            heartGroup.getCardViewAdapter().setSelectable(true);
            diamondGroup.getCardViewAdapter().setSelectable(true);
            spadeGroup.getCardViewAdapter().setSelectable(true);
        }
        else {
            heartGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.HEARTS), true, true, false, 3, CardView.POSITION_BOARD, board);
            diamondGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.DIAMONDS), true, true, false, 3, CardView.POSITION_BOARD, board);
            spadeGroup.setCardGroup(Game.getInstance().getBoard(Game.getInstance().getTurn()).getRow(Card.Suit.SPADES), true, true, false, 3, CardView.POSITION_BOARD, board);
        }
    }

    public void updateSelections(boolean selectionIsMaxed) {
        heartGroup.getCardViewAdapter().setMaxSelectable(selectionIsMaxed ? 0 : Board.FULL_ROW);
        heartGroup.getCardViewAdapter().notifyDataSetChanged();
        diamondGroup.getCardViewAdapter().setMaxSelectable(selectionIsMaxed ? 0 : Board.FULL_ROW);
        diamondGroup.getCardViewAdapter().notifyDataSetChanged();
        spadeGroup.getCardViewAdapter().setMaxSelectable(selectionIsMaxed ? 0 : Board.FULL_ROW);
        spadeGroup.getCardViewAdapter().notifyDataSetChanged();
    }

    public void updateAttack(Attack attack) {
        if (attack.getDamage() > attack.getAttackPower()) {
            GameScreen gameScreen = ((GameScreen) getContext());
            for (CardView cardView : heartGroup) {
                if (cardView.isCardSelected()) {
                    heartGroup.getCardViewAdapter().toggleSelection(cardView);
                    gameScreen.updateAttack(cardView.getCard(), false, false);
                    attack = gameScreen.getAttack();
                    if (attack.getDamage() <= attack.getAttackPower()) {
                        break;
                    }
                }
            }

            if (attack.getDamage() > attack.getAttackPower()) {
                for (CardView cardView : diamondGroup) {
                    if (cardView.isCardSelected()) {
                        diamondGroup.getCardViewAdapter().toggleSelection(cardView);
                        gameScreen.updateAttack(cardView.getCard(), false, false);
                        attack = gameScreen.getAttack();
                        if (attack.getDamage() <= attack.getAttackPower()) {
                            break;
                        }
                    }
                }
            }
        }

        if (!attack.isBypass()) {
            int hearts = 0;
            for (CardView cardView : heartGroup) {
                if (cardView.isCardSelected()) {
                    hearts++;
                }
            }

            if (hearts > 0) {
                diamondGroup.getCardViewAdapter().setClickable(false);
            } else {
                if (attack.getAttackPower() == 0) {
                    diamondGroup.getCardViewAdapter().setSelectable(false);
                } else {
                    for (CardView card : diamondGroup) {
                        if (!card.isCardSelected()) {
                            card.setSelectable(card.getCard().getValue() <= attack.getAttackPower() - attack.getDamage());
                            diamondGroup.getCardViewAdapter().setSelectable(card, card.isSelectable());
                        } else {
                            card.setClickable(true);
                            diamondGroup.getCardViewAdapter().setClickable(card, true);
                        }
                    }
                }

            }

            int diamonds = 0;
            for (CardView cardView : diamondGroup) {
                if (cardView.isCardSelected()) {
                    diamonds++;
                }
            }

            if ((diamonds > 0 && diamonds == diamondGroup.getCardGroup().getSize()) || diamondGroup.getCardGroup().isEmpty()) {
                for (CardView card : heartGroup) {
                    if (!card.isCardSelected()) {
                        card.setSelectable(card.getCard().getValue() <= attack.getAttackPower() - attack.getDamage());
                        heartGroup.getCardViewAdapter().setSelectable(card, card.isSelectable());
                    }
                }
            } else {
                heartGroup.getCardViewAdapter().setSelectable(false);
            }
        } else {
            diamondGroup.getCardViewAdapter().setSelectable(false);
            for (CardView card : heartGroup) {
                if (!card.isCardSelected()) {
                    card.setSelectable(card.getCard().getValue() <= attack.getAttackPower() - attack.getDamage());
                    heartGroup.getCardViewAdapter().setSelectable(card, card.isSelectable());
                }
            }
        }

    }

    public void update() {
        heartGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.HEARTS), true, true, false, 3, CardView.POSITION_BOARD, board);
        diamondGroup.setCardGroup(Game.getInstance().getBoard(board).getRow(Card.Suit.DIAMONDS), true, true, false, 3, CardView.POSITION_BOARD, board);
        spadeGroup.setCardGroup(Game.getInstance().getBoard(Game.getInstance().getTurn()).getRow(Card.Suit.SPADES), true, true, true, 3, CardView.POSITION_BOARD, board);
    }
}