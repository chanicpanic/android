/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.chanicpanic.chanicpanicmobile.R;
import com.chanicpanic.chanicpanicmobile.game.Card;
import com.chanicpanic.chanicpanicmobile.game.Game;

import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;


/**
 * This class controls the visual representation of a Card
 * It is based on a Button
 */
public class CardView extends androidx.appcompat.widget.AppCompatButton {
    public static final int POSITION_DECK = 1;
    public static final int POSITION_DISCARD = 2;
    public static final int POSITION_HAND = 3;
    public static final int POSITION_BOARD = 4;

    public static GradientDrawable getCardBackground() {
        return background;
    }

    private static GradientDrawable background;

    /**
     * The Card this view represents
     */
    private Card card;

    /**
     * controls whether or not this card can be selected/deselected
     */
    private boolean selectable = true;

    /**
     * whether or not this Card is selected
     */
    private boolean selected = false;

    /**
     * whether or not this card is face up
     * a face down card may not be selected
     */
    private boolean faceUp;

    /**
     * represents the location of this card
     */
    private int position;

    /**
     * represents the index of the card in its position
     * used with hand and board
     */
    private int positionIndex;

    /**
     * Stores the card backgrounds for each Suit
     */
    public static final int[] BACKGROUNDS = {R.drawable.background_card_heart, R.drawable.background_card_diamonds, R.drawable.background_card_spades, R.drawable.background_card_clubs};

    /**
     * The background of a card that is unselectable
     */
    public static final int BACKGROUND_UNSELECTABLE = R.drawable.background_card_unselectable;

    /**
     * The background of a card that is selected
     */
    public static final int BACKGROUND_SELECTED = R.drawable.background_card_selected;


    public boolean isFaceUp() {
        return faceUp;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        setPosition(position, 0);
    }

    public void setPosition(int position, int positionIndex) {
        this.position = position;
        this.positionIndex = positionIndex;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public Card getCard() {
        return card;
    }

    public CardView(Context context) {
        super(context);
        init();
    }

    public CardView(Context context, Card card) {
        super(context);
        setCard(card);
        init();
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        position = POSITION_DECK;
        positionIndex = 0;
    }

    public void setCard(Card card, boolean faceUp) {
        this.card = card;
        setFaceUp(faceUp);
    }

    public boolean isPlayable() {
        return card.isPlayable(Game.getInstance().getPlayer(), Game.getInstance().getPoints(), Game.getInstance().getPhase(), Game.getInstance().getLastClub());
    }

    public void setCard(Card card) {
        setCard(card, true);
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
        if (faceUp) {
            setStandardBackground();
        } else {
            setBackground(background);
            setText("");
        }
    }

    public boolean setSelectable(boolean selectable) {
        if (faceUp) {
            this.selectable = selectable;
            if (selectable) {
                if (!selected) {
                    setBackgroundResource(BACKGROUNDS[card.getSuit().ordinal()]);
                }
            } else {
                selected = false;
                setBackgroundResource(BACKGROUND_UNSELECTABLE);
            }
            setClickable(selectable);
            return true;
        }
        return false;
    }

    public void setCardSelected(boolean selected) {
        if (faceUp) {
            if (selectable) {
                this.selected = selected;
                if (selected) {
                    setBackgroundResource(BACKGROUND_SELECTED);
                } else {
                    setBackgroundResource(BACKGROUNDS[card.getSuit().ordinal()]);
                }
            }
        }
    }

    public boolean isCardSelected() {
        return selected;
    }

    public void toggleSelected() {
        setCardSelected(!selected);
    }

    public boolean isSelectable() {
        return selectable;
    }

    /**
     * Sets the selectable state of the Card
     * It is not selectable if the hand is full
     * otherwise it depends on the playability
     *
     * @param isSelectionMaxed whether or not the max selections are chosen
     */
    public boolean setSelectableInHand(boolean isSelectionMaxed) {
        if (isSelectionMaxed && !selected) {
            return setSelectable(false);
        } else {
            return setSelectable(isPlayable());
        }
    }

    public boolean setSelectable(boolean isSelectionMaxed, int maxSelectableValue, EnumSet<Card.Suit> selectableSuits) {
        if (isSelectionMaxed && !selected) {
            return setSelectable(false);
        } else {
            return setSelectable(selectableSuits.contains(card.getSuit())
                    && card.getValue() <= maxSelectableValue);
        }
    }

    public void setStandardBackground() {
        setText(card.cardViewText());
        if (card.getValue() != card.getBaseValue()) {
            setTextColor(Color.WHITE);
        } else {
            setTextColor(Color.BLACK);
        }
        setBackgroundResource(BACKGROUNDS[card.getSuit().ordinal()]);
    }

    public boolean equals(Object o) {
        if (o instanceof CardView) {
            if (card != null && ((CardView) o).card != null) {
                return card.equals(((CardView) o).card);
            }
        }
        return false;
    }

    @NotNull
    public String toString() {
        if (card == null) {
            return super.toString();
        }
        return card + " " + selectable + " " + selected;
    }

    public static void loadBackground(long colors, DisplayMetrics metrics) {
        long COLOR_MASK = 0x00000000FFFFFFFFL;
        int[] colorArray = {(int) ((colors >> 32) & COLOR_MASK), (int) ((colors & COLOR_MASK))};
        background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colorArray);
        background.setStroke((int) (3 * metrics.density + .5f), Color.BLACK);
    }
}