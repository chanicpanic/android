/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chanicpanic.chanicpanicmobile.R;
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen;
import com.chanicpanic.chanicpanicmobile.game.Board;
import com.chanicpanic.chanicpanicmobile.game.Card;
import com.chanicpanic.chanicpanicmobile.game.Game;
import com.chanicpanic.chanicpanicmobile.game.Player;
import com.chanicpanic.chanicpanicmobile.game.UtilitiesKt;

import java.util.List;

/**
 * This class is the adapter for the spinner that shows the boards of players
 */
public class BoardSpinnerAdapter extends ArrayAdapter<Player> {
    private static final int RESOURCE = R.layout.spinner_board;

    public BoardSpinnerAdapter(Context context, List<Player> data) {
        super(context, RESOURCE, R.id.tvPlayer, data);
    }

    public @NonNull
    View getRegularView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView != null) {
            if (convertView.getId() == R.id.frame_board_spinner) {
                return updateView(convertView, position, false);
            }
        }
        return createView(position, parent, false);
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView != null) {
            if (convertView.getId() == R.id.frame_board_spinner) {
                return updateView(convertView, position, true);
            }
        }
        return createView(position, parent, true);
    }

    /**
     * creates the view associated with the given position (board)
     *
     * @param position the position of the view
     * @param parent   the parent to inflate to
     * @return an inflated view
     */
    private View createView(int position, ViewGroup parent, boolean dropdown) {
        // inflate view
        View v = LayoutInflater.from(getContext()).inflate(RESOURCE, parent, false);

        updateView(v, position, dropdown);

        return v;
    }

    private View updateView(View v, int position, boolean dropdown) {
        // get references to text views
        TextView tvPlayer = v.findViewById(R.id.tvPlayer);
        TextView tvBoard = v.findViewById(R.id.tvBoard);

        // get the player at the position in the data
        Player player = Game.getInstance().getPlayer(position);

        GameScreen gameScreen = (GameScreen) getContext();
        int boardView = gameScreen.getBoardShown();

        String playerInfo;
        String name = Game.getInstance().getPlayer(position).getName();
        if (!dropdown && name.length() > 10) {
            name = name.substring(0, 8).concat("...");
        }
        if (gameScreen.isInAttackMode() && position == boardView) // show different for attack mode
        {
            // set text
            tvPlayer.setTextColor(Color.BLACK);
            playerInfo = name + "\n\n" + Game.getInstance().getPlayer().getName();
            String board = UtilitiesKt.suitToString(Card.Suit.HEARTS) + Board.Companion.toString(player.getBoard().getRow(Card.Suit.HEARTS))
                    + "\n" + UtilitiesKt.suitToString(Card.Suit.DIAMONDS) + Board.Companion.toString(player.getBoard().getRow(Card.Suit.DIAMONDS))
                    + "\n" + UtilitiesKt.suitToString(Card.Suit.SPADES) + Board.Companion.toString(Game.getInstance().getBoard(Game.getInstance().getTurn()).getRow(Card.Suit.SPADES));
            tvBoard.setText(board);
        } else {
            // set text
            tvPlayer.setTextColor(getContext().getResources().getColor(GameScreen.TEAM_COLORS[Game.getInstance().getTeamColorIndex(Game.getInstance().getPlayer(position).getStartingTurn() % Game.getInstance().getTeams())]));
            playerInfo = name;
            if (player.getTurn() == Game.getInstance().getTurn()) {
                playerInfo += "\n(You)";
            } else if (Game.getInstance().onSameTeam(player)) {
                playerInfo += "\n(Teammate)";
            } else {
                playerInfo += "\n(Opponent)";
            }
            if (Game.getInstance().isPresenceActive()) {
                playerInfo += "\n" + player.getPresence() + " Presence";
            }
            tvBoard.setText(player.getBoard().toString());
        }

        tvPlayer.setText(playerInfo);

        if (Game.getInstance().getStartingPlayerCount() != Game.getInstance().getTeams() && dropdown) {
            tvPlayer.setTextColor(getContext().getResources().getColor(GameScreen.TEAM_COLORS[Game.getInstance().getTeamColorIndex(Game.getInstance().getPlayer(position).getStartingTurn() % Game.getInstance().getTeams())]));
            TextView teams = v.findViewById(R.id.tvTeam);
            String text = "Team " + (Game.getInstance().getPlayer(position).getStartingTurn() % Game.getInstance().getTeams() + 1);
            text += "\nTeammates: " + Game.getInstance().teammatesOf(position).size();
            int presence = Game.getInstance().getPlayer(position).getPresence();
            List<Player> teammates = Game.getInstance().teammatesOf(position);
            for (Player p : teammates) {
                presence += p.getPresence();
            }
            presence /= teammates.size() + 1;
            text += "\nTeam Presence: " + presence;
            teams.setText(text);
            teams.setTextColor(getContext().getResources().getColor(GameScreen.TEAM_COLORS[Game.getInstance().getTeamColorIndex(Game.getInstance().getPlayer(position).getStartingTurn() % Game.getInstance().getTeams())]));

            teams.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }

        return v;
    }

    public int getCount() {
        return Game.getInstance().getPlayerCount();
    }
}
