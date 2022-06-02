/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.chanicpanic.chanicpanicmobile.R;
import com.chanicpanic.chanicpanicmobile.game.Game;

public class InfoFragment extends Fragment {
    private TextView phase;
    private TextView points;
    private TextView round;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info, container, false);
        phase = v.findViewById(R.id.phase);
        points = v.findViewById(R.id.points);
        round = v.findViewById(R.id.round);
        update();
        return v;
    }

    public void update() {
        phase.setText(((new StringBuilder()).append(Game.PHASES[Game.getInstance().getPhase()]).append(" ").append(getString(R.string.phase))));
        switch (Game.getInstance().getPhase()) {
            case 2:
                points.setText(String.format(getString(R.string.points) + " %d", Game.getInstance().getPoints()));
                break;
            case 3:
                points.setText(String.format(getString(R.string.last_club) + " %d", Game.getInstance().getLastClub()));
                break;
            default:
                points.setText("");
        }
        round.setText(String.format(getString(R.string.round) + " %d", Game.getInstance().getRound()));
    }

}
