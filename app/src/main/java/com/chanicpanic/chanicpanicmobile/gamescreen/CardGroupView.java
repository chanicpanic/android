/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.chanicpanic.chanicpanicmobile.R;
import com.chanicpanic.chanicpanicmobile.game.Card;
import com.chanicpanic.chanicpanicmobile.game.CardGroup;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * This class controls the visual representation of a CardGroup
 * It is based on a RecyclerView
 */
public class CardGroupView extends RecyclerView implements Iterable<CardView> {
    private CardGroup cardGroup;
    private final SnapHelper helper = new LinearSnapHelper();

    public CardGroupView(Context context) {
        super(context);
    }

    public CardGroupView(Context context, CardGroup cardGroup) {
        super(context);
        setCardGroup(cardGroup);
    }

    public CardGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardGroupView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCardGroup(CardGroup cardGroup, int position) {
        setCardGroup(cardGroup, true, false, false, 0, position, 0);
    }

    public void setCardGroup(CardGroup cardGroup) {
        setCardGroup(cardGroup, true, false, false, 0, 0, 0);
    }

    public void setCardGroup(CardGroup cardGroup, boolean faceUp, boolean selectable, boolean clickable, int maxSelectable, int position, int positionIndex) {
        this.cardGroup = cardGroup;
        setLayoutManager(new LockableLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        setAdapter(new CardViewAdapter(cardGroup.toArrayList(), faceUp, selectable, clickable, maxSelectable, position, positionIndex));

        helper.attachToRecyclerView(this);

        (new android.os.Handler()).post(() -> smoothScrollBy(1, 0));
    }

    public CardGroup getCardGroup() {
        return cardGroup;
    }

    public void setScrollable(boolean scrollable) {
        ((LockableLayoutManager) getLayoutManager()).setScrollable(scrollable);
    }

    public CardViewAdapter getCardViewAdapter() {
        return (CardViewAdapter) getAdapter();
    }

    @NonNull
    @Override
    public Iterator<CardView> iterator() {
        return new SparseArrayIterator(getCardViewAdapter().cardViews);
    }


    public class CardViewAdapter extends AbstractCardViewAdapter<CardViewAdapter.ViewHolder> {
        public static final int SELECTED = 0;
        public static final int SELECTABLE = 1;
        public static final int CLICKABLE = 2;

        private final List<Card> dataSet;
        private final SparseArray<CardView> cardViews = new SparseArray<>();
        private final boolean faceUp;
        private int maxSelectable;
        private boolean selectable;
        private boolean clickable;
        private final int position;
        private final int positionIndex;
        private int maxSelectableValue = Card.MAX_VALUE;
        private EnumSet<Card.Suit> selectableSuits = EnumSet.noneOf(Card.Suit.class);
        private boolean isClicked = false;


        public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
            public final CardView cardView;

            public ViewHolder(CardView v) {
                super(v);
                cardView = v;
                v.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (!isClicked) {
                    isClicked = true;

                    toggleSelection(getAdapterPosition());


                    if (cardView.getPosition() == CardView.POSITION_HAND) {
                        ((GameScreen) getContext()).toggleSelection(cardView);

                        for (CardView cardView : CardGroupView.this) {
                            if (((GameScreen) getContext()).isPlayableSelectionOn()) {
                                setSelectable();
                            } else {
                                cardView.setSelectable(getMaxSelectable() == countOf(SELECTED), maxSelectableValue, selectableSuits);
                                setSelectable(cardView, cardView.isSelectable());
                            }
                        }

                    } else if (cardView.getPosition() == CardView.POSITION_BOARD) {
                        GameScreen gameScreen = (GameScreen) getContext();
                        if (gameScreen.isInAttackMode()) {
                            if (cardView.getCard().getSuit() == Card.Suit.SPADES) {
                                gameScreen.toggleSelection(cardView);
                                if (gameScreen.isAutoSelect()) {
                                    gameScreen.updateAttack();
                                } else {
                                    gameScreen.updateAttack(cardView.getCard(), false, true);
                                }
                            } else if (cardView.getCard().getSuit() == Card.Suit.DIAMONDS || cardView.getCard().getSuit() == Card.Suit.HEARTS) {
                                if (!gameScreen.isAutoSelect()) {
                                    gameScreen.updateAttack(cardView.getCard(), false, true);
                                }
                            }
                        } else {
                            gameScreen.toggleSelection(cardView);
                        }
                    }
                    isClicked = false;
                }
            }
        }


        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_card, parent, false);
            return new ViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            holder.cardView.setCard(dataSet.get(position), faceUp);

            cardViews.put(position, holder.cardView);

            holder.cardView.setPosition(this.position, positionIndex);
            holder.cardView.setSelectable(hasProperty(position, SELECTABLE, selectable));
            holder.cardView.setCardSelected(hasProperty(position, SELECTED, false));
            holder.cardView.setClickable(hasProperty(position, CLICKABLE, clickable));

            if (holder.cardView.isSelectable()) {
                if (!hasProperty(position, SELECTED)) {
                    holder.cardView.setCardSelected(false);
                    if (holder.cardView.getPosition() == CardView.POSITION_HAND) {
                        if (((GameScreen) getContext()).isPlayableSelectionOn()) {
                            holder.cardView.setSelectableInHand(countOf(SELECTED) ==
                                    maxSelectable);
                        } else {
                            holder.cardView.setSelectable(getMaxSelectable() ==
                                    countOf(SELECTED), maxSelectableValue, selectableSuits);
                        }
                        setProperty(position, SELECTABLE, holder.cardView.isSelectable());
                    }
                }
            } else if (faceUp) {
                if (maxSelectable > 0) {
                    holder.cardView.setCardSelected(false);
                    setProperty(position, SELECTED, false);
                } else {
                    GameScreen gameScreen = (GameScreen) getContext();
                    if (!gameScreen.isInAttackMode() || gameScreen.isAutoSelect() || holder.cardView.getCard().getSuit() == Card.Suit.SPADES) {
                        holder.cardView.setStandardBackground();
                    } else {
                        holder.cardView.setBackgroundResource(CardView.BACKGROUND_UNSELECTABLE);
                    }
                }
            }
            if (holder.cardView.getPosition() == CardView.POSITION_BOARD) {
                GameScreen gameScreen = (GameScreen) getContext();
                if (!gameScreen.isInAttackMode() && !gameScreen.isPlayableSelectionOn()) {
                    holder.cardView.setSelectable(getMaxSelectable() != 0 || holder.cardView.isCardSelected());
                    setProperty(position, SELECTABLE, holder.cardView.isSelectable());
                }
            }

            // remove excess cardviews
            // prevent iterating over a cardview more than once, or one that is not in the list
            if (cardViews.size() > dataSet.size()) {
                for (int i = dataSet.size(), z = cardViews.size(); i < z; i++) {
                    cardViews.remove(i);
                    delete(i);
                }
            }
        }

        @Override
        public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
            super.onViewAttachedToWindow(holder);

            int position = holder.getAdapterPosition();

            holder.cardView.setPosition(this.position, positionIndex);

            if (this.position == CardView.POSITION_HAND && selectable) {
                holder.cardView.setSelectable(selectable);
            } else {
                holder.cardView.setSelectable(hasProperty(position, SELECTABLE, selectable));
            }

            holder.cardView.setCardSelected(hasProperty(position, SELECTED, false));
            holder.cardView.setClickable(hasProperty(position, CLICKABLE, clickable));

            if (holder.cardView.isSelectable()) {
                if (!hasProperty(position, SELECTED)) {
                    holder.cardView.setCardSelected(false);
                    if (holder.cardView.getPosition() == CardView.POSITION_HAND) {
                        if (((GameScreen) getContext()).isPlayableSelectionOn()) {
                            holder.cardView.setSelectableInHand(countOf(SELECTED) ==
                                    maxSelectable);
                        } else {
                            holder.cardView.setSelectable(getMaxSelectable() ==
                                    countOf(SELECTED), maxSelectableValue, selectableSuits);
                        }
                        setProperty(position, SELECTABLE, holder.cardView.isSelectable());
                    }
                }
            } else if (faceUp) {
                if (maxSelectable > 0) {
                    holder.cardView.setCardSelected(false);
                    setProperty(position, SELECTED, false);
                } else {
                    GameScreen gameScreen = (GameScreen) getContext();
                    if (!gameScreen.isInAttackMode() || gameScreen.isAutoSelect() || holder.cardView.getCard().getSuit() == Card.Suit.SPADES) {
                        holder.cardView.setStandardBackground();
                    } else {
                        holder.cardView.setBackgroundResource(CardView.BACKGROUND_UNSELECTABLE);
                    }
                }
            }
            if (holder.cardView.getPosition() == CardView.POSITION_BOARD) {
                GameScreen gameScreen = (GameScreen) getContext();
                if (!gameScreen.isInAttackMode() && !gameScreen.isPlayableSelectionOn()) {
                    holder.cardView.setSelectable(getMaxSelectable() != 0 || holder.cardView.isCardSelected());
                    setProperty(position, SELECTABLE, holder.cardView.isSelectable());
                }
            }
        }

        public CardViewAdapter(List<Card> list, boolean faceUp, boolean selectable, boolean clickable, int maxSelectable, int position, int positionIndex) {
            super(3);
            dataSet = new ArrayList<>();
            dataSet.addAll(list);
            this.faceUp = faceUp;
            this.selectable = selectable;
            this.clickable = clickable;
            this.maxSelectable = maxSelectable;
            this.position = position;
            this.positionIndex = positionIndex;
        }


        public Iterator<CardView> getIterator() {
            return new SparseArrayIterator(cardViews);
        }

        public List<CardView> getCardViews() {
            ArrayList<CardView> copy = new ArrayList<>();
            Iterator<CardView> iter = getIterator();
            while (iter.hasNext()) {
                copy.add(iter.next());
            }
            return copy;
        }

        public int getMaxSelectable() {
            return maxSelectable;
        }

        public void setMaxSelectable(int maxSelectable) {
            this.maxSelectable = maxSelectable;
        }

        public void setClickable(CardView cardView, boolean clickable) {
            SparseArrayIterator iterator = new SparseArrayIterator(cardViews);
            while (iterator.hasNext()) {
                CardView next = iterator.next();
                if (next.equals(cardView)) {
                    next.setClickable(clickable);
                    setProperty(iterator.index() - 1, CLICKABLE, clickable);
                    return;
                }
            }
        }

        public void setClickable(boolean clickable) {
            this.clickable = clickable;
            for (CardView cardView : CardGroupView.this) {
                cardView.setClickable(clickable);
            }
            for (int i = 0, z = dataSet.size(); i < z; i++) {
                setProperty(i, CLICKABLE, clickable);
            }
        }

        public void setSelectable(CardView cardView, boolean selectable, boolean clickable) {
            SparseArrayIterator iterator = new SparseArrayIterator(cardViews);
            while (iterator.hasNext()) {
                CardView next = iterator.next();
                if (next.equals(cardView)) {
                    next.setSelectable(clickable);
                    setProperty(iterator.index() - 1, SELECTABLE, selectable);
                    setProperty(iterator.index() - 1, CLICKABLE, clickable);
                    return;
                }
            }
        }

        public void setSelectable(CardView cardView, boolean selectable) {
            setSelectable(cardView, selectable, selectable);
        }

        public void setSelectable(CardViewEvaluator evaluator) {
            SparseArrayIterator iterator = new SparseArrayIterator(cardViews);
            while (iterator.hasNext()) {
                CardView next = iterator.next();
                next.setSelectable(evaluator.evaluate(next));
                setProperty(iterator.index() - 1, SELECTABLE, next.isSelectable());
                setProperty(iterator.index() - 1, CLICKABLE, next.isSelectable());
            }
        }

        public void setSelectable(boolean selectable, boolean clickable) {
            this.selectable = selectable;
            this.clickable = clickable;
            for (CardView cardView : CardGroupView.this) {
                cardView.setSelectable(selectable);
                cardView.setClickable(clickable);
            }
            for (int i = 0, z = dataSet.size(); i < z; i++) {
                setProperty(i, SELECTABLE, selectable);
                setProperty(i, CLICKABLE, clickable);
            }
            notifyDataSetChanged();
        }

        public void setSelectableProperty(boolean selectable) {
            this.selectable = selectable;
        }

        public void setSelectable(boolean selectable) {
            setSelectable(selectable, selectable);
        }

        public void setSelectable() {
            SparseArrayIterator iterator = new SparseArrayIterator(cardViews);
            while (iterator.hasNext()) {
                CardView cardView = iterator.next();
                if (position == CardView.POSITION_HAND) {
                    cardView.setSelectableInHand(getMaxSelectable() == countOf(SELECTED));
                    setProperty(iterator.index() - 1, SELECTABLE, cardView.isSelectable());
                    setProperty(iterator.index() - 1, CLICKABLE, cardView.isSelectable());
                }
            }
            // todo review this
//            notifyDataSetChanged();
        }

        public void setSelectableCards(int maxSelectableCards, int maxSelectableValue, EnumSet<Card.Suit> selectableSuits) {
            this.maxSelectableValue = maxSelectableValue;
            this.selectableSuits = selectableSuits;
            setMaxSelectable(maxSelectableCards);

            SparseArrayIterator iterator = new SparseArrayIterator(cardViews);
            while (iterator.hasNext()) {
                CardView cardView = iterator.next();
                if (position == CardView.POSITION_HAND || position == CardView.POSITION_BOARD) {
                    cardView.setSelectable(maxSelectableCards == countOf(SELECTED), maxSelectableValue, selectableSuits);
                    setProperty(iterator.index() - 1, SELECTABLE, cardView.isSelectable());
                    setProperty(iterator.index() - 1, CLICKABLE, cardView.isSelectable());
                }
            }
            // todo review
//            notifyDataSetChanged();
        }


        public int getItemCount() {
            return dataSet.size();
        }

        public void clearSelection() {
            clearProperty(SELECTED);

            for (CardView cardView : CardGroupView.this) {
                cardView.setCardSelected(false);
                if (((GameScreen) getContext()).isPlayableSelectionOn()) {
                    cardView.setSelectableInHand(countOf(SELECTED) == maxSelectable);
                } else {
                    cardView.setSelectable(getMaxSelectable() == countOf(SELECTED), maxSelectableValue, selectableSuits);
                }
            }
            // necessary to prevent bugs for now
            notifyDataSetChanged();
        }

        public void toggleSelection(int position) {
            if (position >= 0) {
                CardView o = cardViews.valueAt(position);
                CardView cardView;
                if (o == null) {
                    cardView = cardViews.get(position);
                } else {
                    cardView = o;
                }

                cardView.toggleSelected();
                toggle(position, SELECTED);
            }
        }

        public void toggleSelection(CardView card) {
            SparseArrayIterator iterator = new SparseArrayIterator(cardViews);
            while (iterator.hasNext()) {
                if (iterator.next().equals(card)) {
                    toggleSelection(iterator.index() - 1);
                    return;
                }
            }
        }

        public void selectAll() {
            clearProperty(SELECTED);

            for (int i = 0; i < dataSet.size(); i++) {
                toggle(i, SELECTED);
            }
        }

        public void removeSelectedItems() {
            List<Integer> selections = itemsWith(SELECTED);
            clearProperty(SELECTED);
            for (int i = selections.size() - 1; i >= 0; i--) {
                dataSet.remove(selections.get(i).intValue());
                cardViews.remove(selections.get(i));
                delete(selections.get(i));
            }
            for (int i = selections.size() - 1; i >= 0; i--) {
                notifyItemRemoved(selections.get(i));
            }

            notifyDataSetChanged();
        }

        public void newCard(Card card) {
            dataSet.add(card);
        }
    }

    private static class SparseArrayIterator implements Iterator<CardView> {
        private final SparseArray<CardView> array;
        private int index = 0;

        SparseArrayIterator(SparseArray<CardView> array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return index < array.size();
        }

        @Override
        public CardView next() {
            return array.valueAt(index++);
        }

        public int index() {
            return index;
        }
    }


    // to stop scrolling
    public class LockableLayoutManager extends LinearLayoutManager {
        public boolean isScrollable() {
            return scrollable;
        }

        public void setScrollable(boolean scrollable) {
            this.scrollable = scrollable;
        }

        private boolean scrollable = true;

        private static final float MILLISECONDS_PER_INCH = 200f;

        public LockableLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public LockableLayoutManager(Context context) {
            super(context);
        }

        public LockableLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public boolean canScrollHorizontally() {
            return scrollable && super.canScrollHorizontally();
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, State state, final int position) {
            LinearSmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
                @Override
                protected int getHorizontalSnapPreference() {
                    return position > findFirstVisibleItemPosition() ? SNAP_TO_START : SNAP_TO_END;
                }

                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    // range: .83 - 1.67
                    return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                }
            };

            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }
    }

    public interface CardViewEvaluator {
        boolean evaluate(CardView cardView);
    }
}
