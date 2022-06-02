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

public class InfoAttackFragment extends Fragment {
    private TextView attackPower;
    private TextView efficiency;
    private TextView damage;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info_attack, container, false);
        attackPower = v.findViewById(R.id.lblAttackPower);
        efficiency = v.findViewById(R.id.lblEfficiency);
        damage = v.findViewById(R.id.lblDamage);
        update(0, 0, 0);
        return v;
    }

    public void update(int p, int e, int d) {
        attackPower.setText(String.format(getString(R.string.attack_power) + " %d", p));
        if (e > 0) {
            efficiency.setText((new StringBuilder()).append(String.format(getString(R.string.efficiency) + " %d", e)).append("%").toString());
        } else {
            efficiency.setText((new StringBuilder()).append(getString(R.string.efficiency)).append(" --"));
        }

        damage.setText(String.format(getString(R.string.damage) + " %d", d));
    }

}
