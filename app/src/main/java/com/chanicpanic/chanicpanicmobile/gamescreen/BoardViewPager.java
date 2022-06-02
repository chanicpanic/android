/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen;
import com.chanicpanic.chanicpanicmobile.game.Game;

/**
 * This class is a ViewPager for which paging by the user can be disabled
 */
public class BoardViewPager extends ViewPager {
    private ViewPager.OnPageChangeListener listener;

    public boolean isPagingEnabled() {
        return pagingEnabled;
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        this.pagingEnabled = pagingEnabled;
    }

    private boolean pagingEnabled = true;

    public BoardViewPager(Context context) {
        super(context);
    }

    public BoardViewPager(Context context, AttributeSet attrSet) {
        super(context, attrSet);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return pagingEnabled && super.onTouchEvent(event);
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        return pagingEnabled && super.onInterceptTouchEvent(event);
    }

    // for arrow keys
    public boolean executeKeyEvent(@NonNull KeyEvent event) {
        return pagingEnabled && super.executeKeyEvent(event);
    }

    public int getRealCurrentItem() {
        return getCurrentItem() % ((GameScreen.BoardPagerAdapter) getAdapter()).getRealCount();
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        if (pagingEnabled) {
            super.setCurrentItem(item, smoothScroll);
            setBackground(GameScreen.teamBackgrounds.get(Game.getInstance().getPlayer(item).getStartingTurn() % Game.getInstance().getTeams()));
        }
    }

    public void addOnPageChangeListener(@NonNull ViewPager.OnPageChangeListener listener) {
        this.listener = listener;
        super.addOnPageChangeListener(listener);
    }

    public ViewPager.OnPageChangeListener getListener() {
        return listener;
    }

}
