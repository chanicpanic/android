/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chanicpanic.chanicpanicmobile.R;
import com.chanicpanic.chanicpanicmobile.game.Card;
import com.chanicpanic.chanicpanicmobile.game.CardGroup;
import com.chanicpanic.chanicpanicmobile.game.Game;
import com.chanicpanic.chanicpanicmobile.gamescreen.CardGroupView.CardViewAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class HandFragment extends Fragment {
    private View v;
    private CardGroupView cgv;
    private int hand;
    private Button left;
    private Button right;

    public static HandFragment newInstance(int hand) {
        Bundle bundle = new Bundle();
        bundle.putInt("hand", hand);

        HandFragment fragment = new HandFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            hand = bundle.getInt("hand");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        readBundle(getArguments());
        if (!Game.getInstance().getPlayer(hand).getHand().isEmpty()) {
            v = inflater.inflate(R.layout.fragment_hand, container, false);
        } else {
            v = inflater.inflate(R.layout.fragment_hand_empty, container, false);
        }

        return v;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        if (!Game.getInstance().getPlayer(hand).getHand().isEmpty()) {
            if (GameScreen.sortHand) {
                CardGroup playerHand = Game.getInstance().getPlayer().getHand();
                Collections.sort(playerHand.toArrayList(), new HandComparator());
            }

            cgv = v.findViewById(R.id.hand);
            cgv.setCardGroup(Game.getInstance().getPlayer(hand).getHand(), Game.getInstance().onSameTeam(hand) && !((GameScreen) view.getContext()).isAutoplay(), hand == Game.getInstance().getTurn(), hand == Game.getInstance().getTurn(), hand == Game.getInstance().getTurn() ? 1 : 0, CardView.POSITION_HAND, hand);
            cgv.setScrollable(!((GameScreen) view.getContext()).isAutoplay());
            //cgv.getCardViewAdapter().notifyDataSetChanged();

            left = view.findViewById(R.id.btnLeftArrow);
            right = view.findViewById(R.id.btnRightArrow);


            left.setOnClickListener(v -> cgv.smoothScrollToPosition(0));

            right.setOnClickListener(v -> cgv.smoothScrollToPosition(cgv.getCardViewAdapter().getItemCount() - 1));

            cgv.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                    int firstVisibleItem = ((LinearLayoutManager) cgv.getLayoutManager()).findFirstVisibleItemPosition();
                    int lastVisibleItem = ((LinearLayoutManager) cgv.getLayoutManager()).findLastVisibleItemPosition();

                    left.setVisibility(firstVisibleItem > 0 ? View.VISIBLE : View.INVISIBLE);
                    right.setVisibility(lastVisibleItem < cgv.getCardViewAdapter().getItemCount() - 1 ? View.VISIBLE : View.INVISIBLE);
                }
            });
        }

    }

    public void notifyDraw(int n) {
        if (GameScreen.sortHand) {
            CardGroup playerHand = Game.getInstance().getPlayer().getHand();
            Collections.sort(playerHand.toArrayList(), new HandComparator());

            cgv.setCardGroup(Game.getInstance().getPlayer(hand).getHand(), Game.getInstance().onSameTeam(hand) && !((GameScreen) v.getContext()).isAutoplay(), hand == Game.getInstance().getTurn(), hand == Game.getInstance().getTurn(), hand == Game.getInstance().getTurn() ? 1 : 0, CardView.POSITION_HAND, hand);
        } else {
            for (int i = 0; i < n; i++) {
                int position = Game.getInstance().getPlayer().getHand().getSize() - (n - i);
                cgv.getCardViewAdapter().newCard(Game.getInstance().getPlayer().getHand().get(position));
                cgv.getCardViewAdapter().notifyItemInserted(position);

            }
        }
    }

    public void scrollToEnd() {
        cgv.scrollToPosition(cgv.getCardViewAdapter().getItemCount() - 1);
    }

    public void removeSelections() {
        cgv.getCardViewAdapter().removeSelectedItems();

        int firstVisibleItem = ((LinearLayoutManager) cgv.getLayoutManager()).findFirstVisibleItemPosition();
        int lastVisibleItem = ((LinearLayoutManager) cgv.getLayoutManager()).findLastVisibleItemPosition();

        left.setVisibility(firstVisibleItem > 0 ? View.VISIBLE : View.INVISIBLE);
        right.setVisibility(lastVisibleItem < cgv.getCardViewAdapter().getItemCount() - 1 ? View.VISIBLE : View.INVISIBLE);
    }

    public void setSelectableCards(int maxSelectableCards, int maxSelectableValue, EnumSet<Card.Suit> suits) {
        ((CardViewAdapter) cgv.getAdapter()).setSelectableCards(maxSelectableCards, maxSelectableValue, suits);
    }

    public void updateSelectables() {
        ((CardViewAdapter) cgv.getAdapter()).clearSelection();
        cgv.getCardViewAdapter().setMaxSelectable(1);
        ((CardViewAdapter) cgv.getAdapter()).setSelectable();
    }

    public CardViewAdapter getAdapter() {
        return (CardViewAdapter) cgv.getAdapter();
    }

    public void showSelectedCard(int position) {
        List<CardView> cardViewsList = cgv.getCardViewAdapter().getCardViews();
        CardView selected = cardViewsList.get(position);
        selected.setFaceUp(true);
        selected.setSelectable(true);
        cgv.getCardViewAdapter().toggleSelection(position);
    }

    private static class HandComparator implements Comparator<Card> {
        @Override
        public int compare(Card o1, Card o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null || o2 == null) {
                return o1 == null ? -1 : 1;
            }
            int value = o1.getSuit().ordinal() - o2.getSuit().ordinal();
            if (value == 0) {
                return o1.getBaseValue() - o2.getBaseValue();
            }
            return value;
        }
    }
}