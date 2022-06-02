/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.menu;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.chanicpanic.chanicpanicmobile.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InstructionsExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;

    private final List<String> titles = new ArrayList<>();

    private final Map<String, List<String>> details = new HashMap<>();

    public InstructionsExpandableListAdapter(Context c) {
        this.context = c;

        titles.add(context.getString(R.string.instructions_group_overview));
        titles.add(context.getString(R.string.instructions_group_deck));
        titles.add(context.getString(R.string.instructions_group_turn));
        titles.add(context.getString(R.string.instructions_group_board));
        titles.add(context.getString(R.string.instructions_group_playing));
        titles.add(context.getString(R.string.instructions_group_attacking));
        titles.add(context.getString(R.string.instructions_group_abilities));
        titles.add(context.getString(R.string.instructions_group_additional));
        titles.add(context.getString(R.string.instructions_group_interface));
        titles.add(context.getString(R.string.instructions_group_presence));
        titles.add(context.getString(R.string.instructions_group_confused));

        List<String> overview = new ArrayList<>();
        overview.add(context.getString(R.string.instructions_overview0));
        overview.add(context.getString(R.string.instructions_overview1));
        overview.add(context.getString(R.string.instructions_overview2));

        List<String> deck = new ArrayList<>();
        deck.add(context.getString(R.string.instructions_deck0));
        deck.add(context.getString(R.string.instructions_deck1));
        deck.add(context.getString(R.string.instructions_deck2));

        List<String> turn = new ArrayList<>();
        turn.add(context.getString(R.string.instructions_turn0));
        turn.add(context.getString(R.string.instructions_turn1));

        List<String> board = new ArrayList<>();
        board.add(context.getString(R.string.instructions_board0));
        board.add(context.getString(R.string.instructions_board1));
        board.add(context.getString(R.string.instructions_board2));

        List<String> playing = new ArrayList<>();
        playing.add(context.getString(R.string.instructions_playing0));
        playing.add(context.getString(R.string.instructions_playing1));
        playing.add(context.getString(R.string.instructions_playing2));

        List<String> attacking = new ArrayList<>();
        attacking.add(context.getString(R.string.instructions_attacking0));
        attacking.add(context.getString(R.string.instructions_attacking1));
        attacking.add(context.getString(R.string.instructions_attacking2));
        attacking.add(context.getString(R.string.instructions_attacking3));
        attacking.add(context.getString(R.string.instructions_attacking4));
        attacking.add(context.getString(R.string.instructions_attacking5));
        attacking.add(context.getString(R.string.instructions_attacking6));
        attacking.add(context.getString(R.string.instructions_attacking7));
        attacking.add(context.getString(R.string.instructions_attacking8));

        List<String> abilities = new ArrayList<>();
        abilities.add(context.getString(R.string.instructions_abilities0));
        abilities.add(context.getString(R.string.instructions_abilities1));
        abilities.add(context.getString(R.string.instructions_abilities2));

        List<String> additional = new ArrayList<>();
        additional.add(context.getString(R.string.instructions_additional0));
        additional.add(context.getString(R.string.instructions_additional1));

        List<String> interface0 = new ArrayList<>();
        interface0.add(context.getString(R.string.instructions_interface0));
        interface0.add(context.getString(R.string.instructions_interface1));
        interface0.add(context.getString(R.string.instructions_interface2));

        List<String> presence = new ArrayList<>();
        presence.add(context.getString(R.string.instructions_presence0));
        presence.add(context.getString(R.string.instructions_presence1));
        presence.add(context.getString(R.string.instructions_presence2));
        presence.add(context.getString(R.string.instructions_presence3));

        List<String> confused = new ArrayList<>();
        confused.add(context.getString(R.string.instructions_confused0));

        details.put(titles.get(0), overview);
        details.put(titles.get(1), deck);
        details.put(titles.get(2), turn);
        details.put(titles.get(3), board);
        details.put(titles.get(4), playing);
        details.put(titles.get(5), attacking);
        details.put(titles.get(6), abilities);
        details.put(titles.get(7), additional);
        details.put(titles.get(8), interface0);
        details.put(titles.get(9), presence);
        details.put(titles.get(10), confused);

    }

    @Override
    public int getGroupCount() {
        return titles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return details.get(titles.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return titles.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return details.get(titles.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TextView v;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            v = (TextView) inflater.inflate(R.layout.expandable_list_textview, parent, false);
        } else {
            v = (TextView) convertView;
        }

        v.setText(titles.get(groupPosition));
        v.setTypeface(Typeface.DEFAULT_BOLD);
        int padding = (int) context.getResources().getDimension(R.dimen.default_margin);
        v.setPadding(3 * padding, padding, padding, padding);

        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TextView v;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            v = (TextView) inflater.inflate(R.layout.expandable_list_textview, parent, false);
        } else {
            v = (TextView) convertView;
        }

        v.setText(details.get(titles.get(groupPosition)).get(childPosition));

        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
