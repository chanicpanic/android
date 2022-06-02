/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chanicpanic.chanicpanicmobile.game.Game;

import java.util.List;

import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_ABILITY;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_ATTACK;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_DISCARD;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_DRAW;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_PHASE;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_PLAY;

public class LogAdapter extends BaseAdapter {
    private final Context context;

    private final List<Game.Log.LogInfo> data;

    private final List<Game.Log.LogInfo> filteredData;

    private int filter = TAG_PHASE | TAG_DRAW | TAG_PLAY | TAG_DISCARD | TAG_ABILITY | TAG_ATTACK;

    private final SparseBooleanArray open = new SparseBooleanArray();

    public LogAdapter(Context context, List<Game.Log.LogInfo> data) {
        super();
        this.context = context;
        this.data = data;
        filteredData = data;
    }

    public LogAdapter(Context context, List<Game.Log.LogInfo> data, int filter) {
        super();
        this.context = context;
        this.data = data;

        filteredData = data;
        for (int i = filteredData.size() - 1; i >= 0; i--) {
            if ((filteredData.get(i).getTags() & filter) == 0) {
                filteredData.remove(i);
            }
        }
    }

    public void notifyDataSetChanged() {
        filteredData.clear();
        filteredData.addAll(data);
        for (int i = filteredData.size() - 1; i >= 0; i--) {
            if ((filteredData.get(i).getTags() & filter) == 0) {
                filteredData.remove(i);
            }
        }

        super.notifyDataSetChanged();
    }

    public void setFilter(@Game.Log.LogTag int tags) {
        filter = tags;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        TextView v = new TextView(context);
        v.setText(filteredData.get(position).getMessage());
        v.setPadding(16, 16, 16, 16);

        if (open.get(position)) {
            if (filteredData.get(position).getDetailIndex() > -1 && filteredData.get(position).getDetailIndex() < Game.getInstance().getDetails().size()) {
                v.setText(filteredData.get(position).getMessage() + "\n\n" + Game.getInstance().getDetails().get(filteredData.get(position).getDetailIndex()));
            }
        } else {
            v.setText(filteredData.get(position).getMessage());
        }

        v.setOnClickListener(v1 -> {
            TextView textView = (TextView) v1;
            if (textView.getText().equals(data.get(position).getMessage())) {
                if (filteredData.get(position).getDetailIndex() > -1 && filteredData.get(position).getDetailIndex() < Game.getInstance().getDetails().size()) {
                    textView.setText(filteredData.get(position).getMessage() + "\n\n" + Game.getInstance().getDetails().get(filteredData.get(position).getDetailIndex()));
                    open.put(position, true);
                }
            } else {
                textView.setText(filteredData.get(position).getMessage());
                open.put(position, false);
            }
        });

        return v;
    }
}
