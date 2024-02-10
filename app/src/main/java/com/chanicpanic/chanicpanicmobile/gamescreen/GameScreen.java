/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.chanicpanic.chanicpanicmobile.R;
import com.chanicpanic.chanicpanicmobile.game.AI;
import com.chanicpanic.chanicpanicmobile.game.Attack;
import com.chanicpanic.chanicpanicmobile.game.Board;
import com.chanicpanic.chanicpanicmobile.game.Card;
import com.chanicpanic.chanicpanicmobile.game.CardGroup;
import com.chanicpanic.chanicpanicmobile.game.EndException;
import com.chanicpanic.chanicpanicmobile.game.Game;
import com.chanicpanic.chanicpanicmobile.game.Player;
import com.chanicpanic.chanicpanicmobile.game.abilities.Ability;
import com.chanicpanic.chanicpanicmobile.game.abilities.ActiveAbility;
import com.chanicpanic.chanicpanicmobile.menu.MainActivity;
import com.chanicpanic.chanicpanicmobile.settings.CardSkinPreference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_ABILITY;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_ATTACK;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_DEFAULT;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_DISCARD;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_DRAW;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_EMOJI;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_PHASE;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_PLAY;
import static com.chanicpanic.chanicpanicmobile.game.Game.Log.TAG_ROUND;


/**
 * This class manages the UI of the Game.
 * It serves as the bridge between the Game class (which manages game data)
 * and Views, Fragments, and events.
 */
public class GameScreen extends AppCompatActivity {

    /**
     * This Activity's FragmentManager
     */
    private final FragmentManager fm = getSupportFragmentManager();

    /**
     * A FragmentTransaction field
     */
    private FragmentTransaction ft;

    /**
     * The pager for the Board
     */
    private BoardViewPager pager;

    /**
     * Adapter for the pager
     */
    private BoardPagerAdapter pagerAdapter;

    /**
     * Contains the Selected Cards in Hand, on Board, etc.
     */
    private final CardGroup selectedCards = new CardGroup();

    /**
     * Shows whether or not the screen is displaying attack mode
     */
    private boolean attackMode = false;

    public Attack getAttack() {
        return attack;
    }

    /**
     * Holds the return of AI.calculateAttack
     */
    private Attack attack;

    /**
     * Holds the ids of the drawables for the board backgrounds for different teams
     * Used by the Pager
     */
    public static final int[] TEAM_BACKGROUNDS = {R.drawable.team_1, R.drawable.team_2, R
            .drawable.team_3, R.drawable.team_4, R.drawable.team_5};

    /**
     * Holds the colors of each team.
     */
    public static final int[] TEAM_COLORS = {R.color.team1, R.color.team2, R.color.team3, R.color
            .team4, R.color.team5, R.color.team6, R.color.team7, R.color.team8};


    private static File saveFile;
    private static File propertiesFile;


    /**
     * The UI Flags for this activity
     */
    public static final int UI =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

    /**
     * Tag that is attached to the CardFragment
     */
    private static final String CARD_FRAGMENT_TAG = "piles";

    /**
     * Tag that is attached to the InfoFragment
     */
    private static final String INFO_FRAGMENT_TAG = "info";

    /**
     * Tag that is attached to the HandFragment
     */
    public static final String HAND_FRAGMENT_TAG = "hand";

    /**
     * Tag that is attached to the InfoAttackFragment
     */
    private static final String ATTACK_INFO_FRAGMENT_TAG = "attack";

    /**
     * Tag that is attached to the SpinnerFragment
     */
    private static final String SPINNER_FRAGMENT_TAG = "spin";

    /**
     * Tag that is attached to the AbilityFragment
     */
    private static final String ABILITY_FRAGMENT_TAG = "ability";

    private ActiveAbility activeAbility;

    private InfoFragment infoFragment;

    private boolean abilityShown;

    private boolean playableSelectionOn = true;

    private boolean selectionMustBeMax = true;

    private int maxSelectable = 1;

    private boolean autoplay = false;

    private boolean secureEndTurn;

    private int logFilter = TAG_DEFAULT | TAG_EMOJI | TAG_ROUND | TAG_PHASE | TAG_DRAW | TAG_PLAY | TAG_DISCARD | TAG_ABILITY | TAG_ATTACK;

    private BoardFragment lastBoard;

    private boolean discardPileShown;

    public boolean isAutoSelect() {
        return isAutoSelect;
    }

    public void setAutoSelect(boolean autoSelect) {
        isAutoSelect = autoSelect;
        if (!autoSelect) {
            attack = null;
        }
    }

    private boolean isAutoSelect = true;

    private OnAbilityConfirmListener onAbilityConfirmListener;

    public static final List<GradientDrawable> teamBackgrounds = new ArrayList<>();

    public static boolean sortHand = false;

    public void setAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
    }

    public boolean isAutoplay() {
        return autoplay;
    }

    public void setMaxSelectable(int maxSelectable) {
        this.maxSelectable = maxSelectable;
    }

    public boolean isPlayableSelectionOn() {
        return playableSelectionOn;
    }

    public void setPlayableSelectionOn(boolean playableSelectionOn) {
        this.playableSelectionOn = playableSelectionOn;
        setConfirmButton(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        CardView.loadBackground(preferences.getLong(getString(R.string.PREF_CARD_KEY),
                CardSkinPreference.DEFAULT_VALUE), getResources().getDisplayMetrics());

        BoardFragment.loadBackgrounds(this);

        // load default settings: 2 players, standard
        secureEndTurn = preferences.getBoolean(getString(R.string.PREF_END_TURN_KEY), false);
        sortHand = preferences.getBoolean(getString(R.string.PREF_SORT_HAND_KEY), false);

        Game.getInstance().setLogLimit(Game.LOG_NO_LIMIT);
        if (Game.getInstance().isDeserialized()) {
            Game.getInstance().loadFromDeserialization(this);
        } else {
            try {
                Game.getInstance().initialize();
                Game.getInstance().loadAbilities(this);
                GameScreen.newSave(this);
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating game", Toast.LENGTH_SHORT).show();
                onMenuClicked(null);
                return;
            }
        }


        teamBackgrounds.clear();

        for (int i = 0; i < Game.getInstance().getTeams(); i++) {
            int[] colors = {Color.WHITE, getResources().getColor(TEAM_COLORS[Game.getInstance()
                    .getTeamColorIndex(i)])};
            GradientDrawable background = new GradientDrawable(GradientDrawable.Orientation
                    .TOP_BOTTOM, colors);

            background.setShape(GradientDrawable.RECTANGLE);
            background.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            background.setGradientRadius(getResources().getDimension(com.intuit.sdp.R.dimen._230sdp));
            teamBackgrounds.add(background);

        }

        // load UI and fragments
        loadScreen();

        DialogBuilder.reset();

        if (Game.getInstance().getPlayerCount() == 0) {
            Toast.makeText(this, "Invalid Game State", Toast.LENGTH_SHORT).show();
            onMenuClicked(null);
            return;
        }
        newTurn();
    }

    public CardGroup getSelectedCards() {
        return selectedCards;
    }

    public int getBoardShown() {
        return pager.getRealCurrentItem();
    }

    /**
     * @return whether or not this is displaying an attack
     */
    public boolean isInAttackMode() {
        return attackMode;
    }

    public ViewPager getPager() {
        return pager;
    }

    public void clearSelections() {
        selectedCards.clear();
    }

    public void onStartClicked(View v) {

        setAutoplay(false);
        setAttackMode(false, false);
        setAutoSelect(true);

        infoFragment.update();

        // show the board
        loadPager();

        // set the pager to the turn player's board
        pager.setCurrentItem(Game.getInstance().getTurn(), false);

        Button attack = findViewById(R.id.btnAttack);

        if (attack != null) {
            attack.setVisibility(View.GONE);
            attack.setText(getString(R.string.attack));
        }

        Button confirm = findViewById(R.id.btnConfirm);
        confirm.setVisibility(View.VISIBLE);
        confirm.setEnabled(false);
        Button endTurn = findViewById(R.id.btnEndTurn);
        endTurn.setVisibility(View.VISIBLE);
        endTurn.setEnabled(true);

        SpinnerFragment spinnerFragment = (SpinnerFragment) fm.findFragmentByTag(SPINNER_FRAGMENT_TAG);
        if (spinnerFragment != null) {
            spinnerFragment.setEnabled(true);
        }

        updateSpinnerPosition(Game.getInstance().getTurn());
        updateSpinner();
        clearSelections();

        ViewGroup gameFrame = findViewById(R.id.gameScreenFrame);
        gameFrame.removeViewAt(gameFrame.getChildCount() - 1);

        Game.getInstance().startTurn(this);

        // show Card and Info Fragments which were removed when the previous turn was
        // ended
        ft = fm.beginTransaction();
        ft.replace(R.id.frameCard, new CardFragment(), CARD_FRAGMENT_TAG);
        ft.replace(R.id.frameHand, HandFragment.newInstance(Game.getInstance().getTurn()),
                HAND_FRAGMENT_TAG)
                .replace(R.id.frameInfo, infoFragment, INFO_FRAGMENT_TAG)
                .commitNow();
    }

    public void onAutoPlayClicked(View v) {

        setAutoplay(true);
        if (!Game.getInstance().startTurn(this)) {
            return;
        }

        setAttackMode(false, false);

        if (!Game.getInstance().getPlayer().isAIAttached()) {
            Game.getInstance().getPlayer().attachAI(new AI((GameScreen) pager.getContext()));
        }

        Button attack = findViewById(R.id.btnAttack);

        if (attack != null) {
            attack.setVisibility(View.GONE);
        }

        findViewById(R.id.btnConfirm).setVisibility(View.GONE);
        findViewById(R.id.btnEndTurn).setVisibility(View.GONE);

        pager.setPagingEnabled(false);

        clearSelections();

        Game.getInstance().getPlayer().takeTurn();
    }

    public void onMenuClicked(View v) {
        Game.getInstance().clear();

        Intent intent = new Intent(GameScreen.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
    }

    public void onPlayAgainClicked(View v) {
        (new DialogBuilder(this))
                .setTitle("Do you want to Play Again?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", v1 -> {
                    try {
                        Game.getInstance().initialize();
                        finish();
                        startActivity(new Intent(GameScreen.this, GameScreen.class));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(GameScreen.this, "Error creating game", Toast.LENGTH_SHORT).show();
                    }
                })
                .setFullScreen(false)
                .setCancelable(false)
                .show();
    }

    public void showDialog() {

        setAbilityDisplay(null);

        ViewGroup gameFrame = findViewById(R.id.gameScreenFrame);
        View v;
        if (autoplay) {
            v = gameFrame.getChildAt(gameFrame.getChildCount() - 1);
            if (v.getId() != R.id.layoutNewTurn) {
                v = getLayoutInflater().inflate(R.layout.dialog_new_turn, gameFrame);
            }
        } else {
            v = getLayoutInflater().inflate(R.layout.dialog_new_turn, gameFrame);
        }

        TextView playerName = v.findViewById(R.id.txtPlayer);
        if (playerName != null) {
            playerName.setText(((new StringBuilder()).append(Game.getInstance().getPlayer().getName())
                    .append("\n\n").append(getString(R.string.round)).append(" ").append(Game.getInstance().getRound())));
            playerName.setTextColor(getResources().getColor(TEAM_COLORS[Game.getInstance()
                    .getTeamColorIndex(Game.getInstance().getPlayer().getStartingTurn() % Game
                            .getInstance().getTeams())]));
        }
        v.findViewById(R.id.btnStartTurn).setVisibility(Game.getInstance().getPlayer().getMode()
                .contains(Player.Mode.HUMAN) ? View.VISIBLE : View.GONE);
        v.findViewById(R.id.btnAutoPlay).setVisibility(Game.getInstance().getPlayer().getMode()
                .contains(Player.Mode.CPU) ? View.VISIBLE : View.GONE);
        configureLog(v);
    }

    @SuppressLint("WrongConstant")
    public void configureLog(View v) {
        LogAdapter logAdapter = new LogAdapter(this, Game.getInstance().getFullLog(), logFilter);

        final ListView log = v.findViewById(R.id.listLog);
        log.setAdapter(logAdapter);
        log.setSelection(logAdapter.getCount() - 1);

        LogRefreshListener logRefreshListener = new LogRefreshListener(v, log);
        logRefreshListener.setChecks(logFilter);

        View refresh = v.findViewById(R.id.btnRefreshLog);
        refresh.setOnClickListener(logRefreshListener);
        refresh.setEnabled(false);
    }

    private class LogRefreshListener implements View.OnClickListener {
        private final ListView log;

        private final CheckBox phase;
        private final CheckBox draw;
        private final CheckBox play;
        private final CheckBox discard;
        private final CheckBox ability;
        private final CheckBox attack;

        public LogRefreshListener(View view, ListView log) {
            this.log = log;

            phase = view.findViewById(R.id.cbxPhase);
            draw = view.findViewById(R.id.cbxDraw);
            play = view.findViewById(R.id.cbxPlay);
            discard = view.findViewById(R.id.cbxDiscard);
            ability = view.findViewById(R.id.cbxAbility);
            attack = view.findViewById(R.id.cbxAttack);

            final ImageButton refresh = view.findViewById(R.id.btnRefreshLog);

            CompoundButton.OnCheckedChangeListener checkBoxListener = (buttonView, isChecked) -> refresh.setEnabled(true);

            phase.setOnCheckedChangeListener(checkBoxListener);
            draw.setOnCheckedChangeListener(checkBoxListener);
            play.setOnCheckedChangeListener(checkBoxListener);
            discard.setOnCheckedChangeListener(checkBoxListener);
            ability.setOnCheckedChangeListener(checkBoxListener);
            attack.setOnCheckedChangeListener(checkBoxListener);

        }

        public int getFilter() {
            return TAG_DEFAULT
                    | TAG_EMOJI
                    | TAG_ROUND
                    | (phase.isChecked() ? TAG_PHASE : 0)
                    | (draw.isChecked() ? TAG_DRAW : 0)
                    | (play.isChecked() ? TAG_PLAY : 0)
                    | (discard.isChecked() ? TAG_DISCARD : 0)
                    | (ability.isChecked() ? TAG_ABILITY : 0)
                    | (attack.isChecked() ? TAG_ATTACK : 0);
        }

        public void setChecks(@Game.Log.LogTag int filter) {
            phase.setChecked((filter & TAG_PHASE) == TAG_PHASE);
            draw.setChecked((filter & TAG_DRAW) == TAG_DRAW);
            play.setChecked((filter & TAG_PLAY) == TAG_PLAY);
            discard.setChecked((filter & TAG_DISCARD) == TAG_DISCARD);
            ability.setChecked((filter & TAG_ABILITY) == TAG_ABILITY);
            attack.setChecked((filter & TAG_ATTACK) == TAG_ATTACK);
        }

        @Override
        public void onClick(View v) {
            logFilter = getFilter();
            LogAdapter logAdapter = new LogAdapter(GameScreen.this, Game.getInstance().getFullLog(), logFilter);
            log.setAdapter(logAdapter);
            log.setSelection(logAdapter.getCount() - 1);
            v.setEnabled(false);
        }
    }


    /**
     * prepares for the next turn and displays a message to start the turn
     */
    public void newTurn() {
        Game.getInstance().newLog();

        // set data for the next turn
        Game.getInstance().nextTurn();

        showDialog();
    }

    public void endTurn() {
        if (activeAbility != null) {
            activeAbility.deactivate();
        }

        // hide hand and card Fragments so they are not visible to the next player
        fm.beginTransaction().remove(fm.findFragmentByTag(HAND_FRAGMENT_TAG))
                .remove(fm.findFragmentByTag(CARD_FRAGMENT_TAG))
                .commit();

        AbilityFragment abilityFragment = (AbilityFragment) fm.findFragmentByTag(ABILITY_FRAGMENT_TAG);
        if (abilityFragment != null) {
            abilityFragment.disable();
        }

        SpinnerFragment spinnerFragment = (SpinnerFragment) fm.findFragmentByTag(SPINNER_FRAGMENT_TAG);
        if (spinnerFragment != null) {
            spinnerFragment.setEnabled(false);
        }

        findViewById(R.id.btnEndTurn).setEnabled(false);
        findViewById(R.id.btnConfirm).setEnabled(false);

        setAttackMode(false, true);

        pager.setPagingEnabled(false);

        Button attack = findViewById(R.id.btnAttack);

        if (attack != null) {
            attack.setVisibility(View.GONE);
        }

        saveGame(this);

        newTurn();
    }

    public static void newSave(Context context) throws IOException {
        File file = context.getDir("saves", MODE_PRIVATE);
        String[] fileList = file.list();
        Arrays.sort(fileList);
        int i = 0;
        if (fileList.length > 0 && fileList[fileList.length - 1].equals("save" + (fileList.length
                - 1))) {
            i = fileList.length;
        } else {
            while (i < fileList.length && fileList[i].equals("save" + i)) {
                i++;
            }
        }

        file = new File(file, "save" + i);
        file.mkdir();

        GameScreen.saveFile = new File(file, "save");
        GameScreen.propertiesFile = new File(file, "properties");
        saveFile.createNewFile();
        propertiesFile.createNewFile();
        saveGame(context);
    }

    public static void setSaveFile(File file) {
        saveFile = file;
        propertiesFile = new File(saveFile.getParent(), "properties");
    }

    public static File getSaveFile() {
        return saveFile;
    }

    public static File getPropertiesFile() {
        return propertiesFile;
    }

    public static void saveGame(Context context) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getSaveFile());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(Game.getInstance());
            objectOutputStream.close();
            fileOutputStream.close();

            FileInputStream fileInputStream = new FileInputStream(getPropertiesFile());
            Properties properties = new Properties();
            properties.load(fileInputStream);
            fileInputStream.close();

            properties.setProperty("Time", String.valueOf(System.currentTimeMillis()));
            properties.setProperty("Players", String.valueOf(Game.getInstance()
                    .getStartingPlayerCount()));
            properties.setProperty("Round", String.valueOf(Game.getInstance().getRound()));

            fileOutputStream = new FileOutputStream(getPropertiesFile());
            properties.store(fileOutputStream, "");
            fileOutputStream.close();

        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving game", Toast.LENGTH_SHORT).show();
        }
    }

    public void setSelectionMustBeMax(boolean selectionMustBeMax) {
        this.selectionMustBeMax = selectionMustBeMax;
    }

    public void updateDiscardPile() {
        setDiscardPileDisplay(false);
        ((CardFragment) fm.findFragmentByTag(CARD_FRAGMENT_TAG)).update();
    }

    public void updateSpinner() {
        SpinnerFragment spinnerFragment = (SpinnerFragment) fm.findFragmentByTag
                (SPINNER_FRAGMENT_TAG);
        if (spinnerFragment != null) {
            spinnerFragment.update();
        }
    }

    public void updateSpinnerPosition(int position) {
        SpinnerFragment spinnerFragment = (SpinnerFragment) fm.findFragmentByTag
                (SPINNER_FRAGMENT_TAG);
        if (spinnerFragment != null) {
            spinnerFragment.setSpinnerPosition(position);
        }
    }

    public void updateInfo() {
        infoFragment.update();
    }

    /**
     * loads and initializes Fragments, Views, and listeners of the UI
     * This is only called once in the OnCreate method
     */
    private void loadScreen() {
        // set UI Visibility
        getWindow().getDecorView().setSystemUiVisibility(UI);

        infoFragment = new InfoFragment();

        // load InfoFragment
        ft = fm.beginTransaction();
        ft.add(R.id.frameInfo, infoFragment, INFO_FRAGMENT_TAG)
                .add(R.id.frameSpinner, SpinnerFragment.newInstance(), SPINNER_FRAGMENT_TAG)
                .commit();

        // load boards
        loadPager();

        // set listeners
        pager.addOnPageChangeListener(new BoardPagerListener());
        findViewById(R.id.btnConfirm).setOnClickListener(new ConfirmButtonPlayListener());
        findViewById(R.id.btnAttack).setOnClickListener(new AttackButtonListener());
        findViewById(R.id.btnEndTurn).setOnClickListener(new EndTurnButtonListener());
        pager.setOnSystemUiVisibilityChangeListener(new UIListener());


        findViewById(R.id.btnBoardLeft).setOnClickListener(v -> pager.setCurrentItem((pager.getCurrentItem() - 1 + pagerAdapter.getRealCount()) %
                pagerAdapter.getRealCount(), pager.getCurrentItem() > 0));

        findViewById(R.id.btnBoardRight).setOnClickListener(v -> pager.setCurrentItem((pager.getCurrentItem() + 1) % pagerAdapter.getRealCount(),
                pager.getCurrentItem() < pagerAdapter.getRealCount() - 1));
    }

    /**
     * selects/deselects a card
     *
     * @param card the card of which to add/remove from selectedCards
     */
    public void toggleSelection(CardView card) {
        toggleSelection(card.getCard());
    }

    public void toggleSelection(Card card) {
        // attempt to remove card from selection
        if (!selectedCards.remove(card)) {
            // if the card was not removed, it was not there, add the card
            selectedCards.add(card);
        }

        // ability board selection
        if (!playableSelectionOn && !attackMode) {
            if (lastBoard != null) {
                lastBoard.updateSelections(selectedCards.getSize() == maxSelectable);
            }
        }

        if (selectedCards.isEmpty()) // if no cards are selected
        {
            setConfirmButton(false); // Confirm button cannot be clicked
        } else {
            setConfirmButton(selectedCards.getSize() == maxSelectable || !selectionMustBeMax);
        }
    }

    public void setConfirmAbilityListener(boolean enabled) {
        if (enabled) {
            findViewById(R.id.btnConfirm).setOnClickListener(new ConfirmButtonAbilityListener());
        } else {
            findViewById(R.id.btnConfirm).setOnClickListener(new ConfirmButtonPlayListener());
        }
    }

    /**
     * sets the adapter and background of the pager
     */
    public void loadPager() {
        pager = findViewById(R.id.pager);
        pagerAdapter = new BoardPagerAdapter(fm);
        pager.setAdapter(pagerAdapter);
        pager.setPagingEnabled(true);
        pager.setBackground(ResourcesCompat.getDrawable(getResources(), TEAM_BACKGROUNDS[0], null));
    }

    /**
     * Changes the attack mode of the GameScreen
     * Updates the UI
     *
     * @param attackMode whether or not to be in attack mode
     */
    public void setAttackMode(boolean attackMode, boolean transition) {
        // if this call changes the attack mode
        if (this.attackMode != attackMode) {
            this.attackMode = attackMode;
            setSelectionMustBeMax(!attackMode);
            ft = fm.beginTransaction();

            if (attackMode) {
                // switch to attack info
                ft.replace(R.id.frameInfo, new InfoAttackFragment(), ATTACK_INFO_FRAGMENT_TAG);

                // switch confirm button listener
                findViewById(R.id.btnConfirm).setOnClickListener(new ConfirmButtonAttackListener());
            } else {
                // switch to standard info
                ft.replace(R.id.frameInfo, infoFragment, INFO_FRAGMENT_TAG);

                // switch confirm button listener
                findViewById(R.id.btnConfirm).setOnClickListener(new ConfirmButtonPlayListener());
            }
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commitNow();

            // update board
            if (attackMode) {
                if (lastBoard == null) {
                    lastBoard = ((BoardFragment) pagerAdapter.getFragment(pager.getCurrentItem()));
                }
            }

            // prevent crash from player elimination and board not existing
            if ((attackMode || attack == null || !attack.isKill()) && lastBoard != null) {
                lastBoard.transition(attackMode, transition);
            }

            if (!attackMode) {
                lastBoard = null;
                attack = null;
            }

            // remove selected cards
            selectedCards.clear();

            // disable confirm button
            setConfirmButton(false);
        }
    }

    /**
     * This method updates the screen based on the spades selected for an attack
     *
     * @throws IllegalStateException if GameScreen is not in attack mode
     */
    public void updateAttack() {
        // GameScreen must be in attack mode
        if (!attackMode) {
            throw new IllegalStateException("GameScreen must be in attack mode.");
        }

        View board = ((BoardFragment) pagerAdapter.getFragment(pager.getCurrentItem())).v;

        boolean bypass = ((SwitchCompat) board.findViewById(R.id.swtBypass)).isChecked();

        // find diamond CardGroupView on Screen
        CardGroupView.CardViewAdapter diamonds = ((CardGroupView) board.findViewById(R.id.middle)).getCardViewAdapter();

        // find heart CardGroupView on board
        CardGroupView.CardViewAdapter hearts = ((CardGroupView) board.findViewById(R.id.top)).getCardViewAdapter();
        // get the board of the defender displayed on screen
        Player defender = Game.getInstance().getPlayer(getBoardShown());
        if (isAutoSelect) {
            // determine which diamonds/hearts would be destroyed
            attack = AI.calculateAttack(selectedCards, Game.getInstance().getPlayer(), defender, bypass);

            // clear previous selections
            diamonds.clearSelection();

            // if bypass, diamonds are un-selectable
            diamonds.setSelectable(!bypass, false);

            if (!bypass) {
                // select all diamonds on board that would be destroyed
                for (Card diamond : attack.getDiamondsDestroyed()) {
                    diamonds.toggleSelection(new CardView(this, diamond));
                }
            }

            // clear previous selections
            hearts.clearSelection();

            // select all hearts on board that would be destroyed
            for (Card heart : attack.getHeartsDestroyed()) {
                hearts.toggleSelection(new CardView(this, heart));
            }

            // update attack info
        } else {
            diamonds.clearSelection();
            hearts.clearSelection();

            if (attack != null) {
                attack.getDiamondsDestroyed().clear();
                attack.getHeartsDestroyed().clear();

                attack = AI.buildAttack(attack.getSpadesUsed(), Game.getInstance().getPlayer(), defender,
                        attack.isBypass(),
                        attack.getDiamondsDestroyed(), attack.getHeartsDestroyed());
            } else {
                attack = AI.buildAttack(selectedCards, Game.getInstance().getPlayer(), defender,
                        bypass, new CardGroup(), new CardGroup());
            }

            // update attack info
        }
        ((InfoAttackFragment) fm.findFragmentByTag(ATTACK_INFO_FRAGMENT_TAG)).update(attack
                .getAttackPower(), attack.getEfficiency(), attack.getDamage());
        setConfirmButton(attack.getDamage() > 0 || attack.isKill());

        // update board spinner
        updateSpinner();
    }

    public void updateAttack(Card card, boolean toggleBypass, boolean updateBoard) {
        Player defender = Game.getInstance().getPlayer(getBoardShown());

        if (attack == null) {
            View board = ((BoardFragment) pagerAdapter.getFragment(pager.getCurrentItem())).v;
            boolean bypass = ((SwitchCompat) board.findViewById(R.id.swtBypass)).isChecked();
            attack = AI.buildAttack(selectedCards, Game.getInstance().getPlayer(), defender,
                    bypass, new CardGroup(), new CardGroup());
        }

        if (card != null) {
            switch (card.getSuit()) {
                case HEARTS:
                    if (!attack.getHeartsDestroyed().remove(card)) {
                        attack.getHeartsDestroyed().add(card);
                    }
                    break;
                case DIAMONDS:
                    if (!attack.getDiamondsDestroyed().remove(card)) {
                        attack.getDiamondsDestroyed().add(card);
                    }
                    break;
                case SPADES:
                    if (!attack.getSpadesUsed().remove(card)) {
                        attack.getSpadesUsed().add(card);
                    }
                    break;
            }
        }
        attack = AI.buildAttack(attack.getSpadesUsed(), Game.getInstance().getPlayer(), defender,
                toggleBypass != attack.isBypass(),
                toggleBypass ? new CardGroup() : attack.getDiamondsDestroyed(), toggleBypass ? new CardGroup() : attack.getHeartsDestroyed());

        // update attack info
        ((InfoAttackFragment) fm.findFragmentByTag(ATTACK_INFO_FRAGMENT_TAG)).update(attack
                .getAttackPower(), attack.getEfficiency(), attack.getDamage());

        setConfirmButton(attack.getDamage() > 0 || attack.isKill());

        if (updateBoard) {
            lastBoard.updateAttack(attack);
        }

        // update board spinner
        updateSpinner();
    }

    public void setDiscardPileDisplay(boolean discardPileShown) {
        if (this.discardPileShown != discardPileShown) {
            this.discardPileShown = discardPileShown;

            if (discardPileShown) {
                if (getBoardShown() == Game.getInstance().getTurn() && !autoplay) {
                    ft = fm.beginTransaction().replace(R.id.frameCard, new DiscardFragment(),
                            CARD_FRAGMENT_TAG);
                } else {
                    ft = fm.beginTransaction().replace(R.id.frameHand, new DiscardFragment(),
                            CARD_FRAGMENT_TAG);
                }
            } else {
                if (getBoardShown() == Game.getInstance().getTurn() && !autoplay) {
                    ft = fm.beginTransaction().replace(R.id.frameCard, new CardFragment(),
                            CARD_FRAGMENT_TAG);
                } else {
                    ft = fm.beginTransaction().replace(R.id.frameHand, new CardFragment(),
                            CARD_FRAGMENT_TAG);
                }
            }
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commitNow();
        }
    }

    public void setAbilityDisplay(@Nullable Ability ability) {
        if (ability == null == abilityShown) {
            abilityShown = ability != null;
            if (ability instanceof ActiveAbility) {
                activeAbility = (ActiveAbility) ability;
            }

            ft = fm.beginTransaction();
            if (abilityShown) {
                updateSpinner();
                AbilityFragment abilityFragment = new AbilityFragment();
                ft.replace(R.id.frameSpinner, abilityFragment, ABILITY_FRAGMENT_TAG);
                abilityFragment.setAbility(ability);
            } else {
                ft.replace(R.id.frameSpinner, new SpinnerFragment(), SPINNER_FRAGMENT_TAG);
            }

            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }
    }

    public void setConfirmButton(boolean enabled) {
        findViewById(R.id.btnConfirm).setEnabled(enabled);
    }

    public void playCard() {
        // create a CardView for the selected card
        CardView selectedCard = new CardView(this);
        selectedCard.setCard(selectedCards.get(0));
        clearSelections();

        int handBefore = Game.getInstance().getPlayer().getHand().getSize();

        // play the card and update data
        Game.getInstance().getPlayer(Game.getInstance().getTurn()).playCard(selectedCard.getCard());

        // update the hand on screen
        HandFragment hand = (HandFragment) fm.findFragmentByTag(HAND_FRAGMENT_TAG);
        //hand.getAdapter().clearSelectedItems();

        hand.removeSelections();

        // if it is a Utility Card
        if (Card.Companion.getUTILITY_CARDS().contains(selectedCard.getCard().getSuit())) {

            hand.updateSelectables();

            // update board
            BoardFragment boardFragment = (BoardFragment) pagerAdapter.getFragment(Game
                    .getInstance().getTurn());
            boardFragment.play(selectedCard.getCard().getSuit());

            // change card position to the Board
            selectedCard.setPosition(CardView.POSITION_BOARD, Game.getInstance().getTurn());

            // update Board spinner
            updateSpinner();
        } else // it is a Club
        {
            // update hand for new cards drawn
            hand.notifyDraw(Game.getInstance().getPlayer().getHand().getSize() - handBefore + 1);
            hand.updateSelectables();
            hand.scrollToEnd();

            // set card position to discard pile
            selectedCard.setPosition(CardView.POSITION_DISCARD);

            // update card and info fragments
            setDiscardPileDisplay(false);
            ((CardFragment) fm.findFragmentByTag(CARD_FRAGMENT_TAG)).update();
        }

        AbilityFragment abilityFragment = (AbilityFragment) fm.findFragmentByTag(ABILITY_FRAGMENT_TAG);
        if (abilityFragment != null) {
            abilityFragment.updateActivateAbility();
        }

        infoFragment.update();

        // disable confirm button and clear selections
        setConfirmButton(false);
        selectedCards.clear();
    }

    public void endGame() {
        final Game.Result result = Game.getInstance().getResult();
        saveFile.delete();
        File parent = propertiesFile.getParentFile();
        propertiesFile.delete();
        parent.delete();

        String message = getString(R.string.game_over) + "\n\n";
        if (Game.getInstance().getStartingPlayerCount() == Game.getInstance().getTeams()) {
            message += result.getWinner().getName() + " " + getString(R.string.win);
        } else {
            message += getString(R.string.team) + " " + ((result.getWinner().getStartingTurn() % Game
                    .getInstance().getTeams()) + 1) + " " + getString(R.string.win);
        }
        Game.getInstance().reset();
        pagerAdapter.notifyDataSetChanged();

        ViewGroup gameFrame = findViewById(R.id.gameScreenFrame);

        @SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.dialog_new_turn, null);
        ((TextView) v.findViewById(R.id.txtPlayer)).setText(message);
        v.findViewById(R.id.btnAutoPlay).setVisibility(View.GONE);
        Button playAgain = v.findViewById(R.id.btnStartTurn);
        playAgain.setVisibility(View.VISIBLE);
        playAgain.setText(R.string.play_again);
        playAgain.setOnClickListener(this::onPlayAgainClicked);

        gameFrame.addView(v);

        if (autoplay) {
            // remove new turn dialog
            gameFrame.removeViewAt(gameFrame.getChildCount() - 2);
        }
        configureLog(v);
        Game.getInstance().reset();
    }
    public void activateAbilitySelection(int maxSelectable, boolean selectionMustBeMax, int maxValue, EnumSet<Card.Suit> suits) {
        activateAbilitySelection(maxSelectable, selectionMustBeMax, maxValue, suits, true);
    }

    public void activateAbilitySelection(int maxSelectable, boolean selectionMustBeMax, int maxValue, EnumSet<Card.Suit> suits, boolean hand) {
        // clear selections
        clearSelections();

        // set selection settings
        setPlayableSelectionOn(false); // switch to ability selections
        setMaxSelectable(maxSelectable); // choose 2 cards
        setSelectionMustBeMax(selectionMustBeMax); // both cards must be chosen before Confirm

        HandFragment handFragment = (HandFragment) fm.findFragmentByTag(HAND_FRAGMENT_TAG);
        handFragment.getAdapter().clearSelection();

        handFragment.getAdapter().setSelectable(hand);
        if (hand) {
            handFragment.setSelectableCards(maxSelectable, maxValue, suits);
        } else {
            lastBoard = (BoardFragment) pagerAdapter.getFragment(getBoardShown());
            lastBoard.setSelectionMode(true, Board.FULL_ROW, maxValue, suits);
        }

        // set confirm ability listener
        setConfirmAbilityListener(true);
    }

    public void deactivateAbilitySelection() {
        // clear selections
        clearSelections();

        // set selection defaults
        setMaxSelectable(1);
        setPlayableSelectionOn(true);

        setConfirmAbilityListener(false);

        // show selectable cards
        ((HandFragment) getSupportFragmentManager().findFragmentByTag(GameScreen.HAND_FRAGMENT_TAG)).updateSelectables();

        if (lastBoard != null) {
            lastBoard.setSelectionMode(false, Board.FULL_ROW, 0, EnumSet.noneOf(Card.Suit.class));
            lastBoard = null;
        }
    }

    public void lockForAbility(boolean locked, String message, @Nullable OnAbilityConfirmListener listener) {
        pager.setPagingEnabled(locked);

        SpinnerFragment spinnerFragment = (SpinnerFragment) fm.findFragmentByTag(SPINNER_FRAGMENT_TAG);
        if (spinnerFragment != null) {
            spinnerFragment.setEnabled(locked);
        }

        AbilityFragment abilityFragment = (AbilityFragment) fm.findFragmentByTag(ABILITY_FRAGMENT_TAG);
        if (abilityFragment != null) {
            abilityFragment.disableCancellation(message);
        }

        findViewById(R.id.btnEndTurn).setEnabled(locked);
        findViewById(R.id.btnBoardRight).setVisibility(locked ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.btnBoardLeft).setVisibility(locked ? View.VISIBLE : View.INVISIBLE);
        onAbilityConfirmListener = listener;
    }

    public interface OnAbilityConfirmListener {
        void onConfirm();
    }

    /**
     * This Listener sets the screen back to the desired UI whenever it is changed
     */
    private class UIListener implements View.OnSystemUiVisibilityChangeListener {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            getWindow().getDecorView().setSystemUiVisibility(UI);
        }
    }

    /**
     * This class holds the action to be taken when the Confirm Button is clicked
     * to play a selected card in hand
     * precondition: selectedCards.size() = 1
     */
    private class ConfirmButtonPlayListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            playCard();
        }
    }

    /**
     * This class holds the action to be taken when the confirm button
     * is pressed to carry out an attack
     * Preconditions: GameScreen is in attack mode, selectedCards is not empty or null
     */
    private class ConfirmButtonAttackListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Game.getInstance().getPlayer().attack(Game.getInstance().getPlayer(GameScreen.this
                    .getBoardShown()));

            // get the board for the current turn
            Board board = Game.getInstance().getBoard();

            for (Card card : selectedCards) {
                // update data: discard the selected spades on the board
                board.discard(card);
                //Game.discard(card);
            }

            // get the board that is on screen
            board = Game.getInstance().getBoard(getBoardShown());

            for (Card card : attack.getDiamondsDestroyed()) {
                board.discard(card);
            }

            for (Card card : attack.getHeartsDestroyed()) {
                board.discard(card);
            }

            Game.getInstance().log(Game.getInstance().getPlayer().getName() + (attack.isBypass()
                    ? " " + getString(R.string.bypassed) : " " + getString(R.string.attacked)) + " " + Game.getInstance().getPlayer(GameScreen
                    .this.getBoardShown()).getName() + " " + getString(R.string.with) + " " + selectedCards, Game.Log.TAG_ATTACK);

            if (attack.getDiamondsDestroyed().getSize() + attack.getHeartsDestroyed().getSize() > 0) {
                Game.getInstance().log(Game.getInstance().getPlayer().getName() + " " + getString(R.string.destroyed) + " " +
                        attack.getDiamondsDestroyed() + attack.getHeartsDestroyed(), Game.Log.TAG_ATTACK);
            }

            // update the Card Fragment (discard pile)
            setDiscardPileDisplay(false);
            ((CardFragment) fm.findFragmentByTag(CARD_FRAGMENT_TAG)).update();

            ((InfoAttackFragment) fm.findFragmentByTag(ATTACK_INFO_FRAGMENT_TAG)).update(0, 0, 0);

            if (attack.isKill()) {
                Player eliminated = Game.getInstance().getPlayer(GameScreen.this.getBoardShown());

                Game.getInstance().eliminate(GameScreen.this.getBoardShown(), attack.isBypass());

                setAttackMode(false, false);

                if (Game.getInstance().isGame()) {
                    endGame();
                    return;
                } else {
                    Toast.makeText(GameScreen.this, eliminated.getName() + " " + getString(R.string.eliminated), Toast.LENGTH_SHORT).show();
                    lastBoard = null;
                    pagerAdapter.invalidate();
                    pagerAdapter.notifyDataSetChanged();
                    if (GameScreen.this.getBoardShown() == Game.getInstance().getTurn()) {
                        pager.getListener().onPageSelected(Game.getInstance().getTurn());
                    } else {
                        pager.setCurrentItem(Game.getInstance().getTurn(), true);
                    }
                }
            }

            // clear before updating board
            clearSelections();
            attack = null;

            // update current board
            if (lastBoard != null) {
                lastBoard.transition(true, false);
            }

            // remove spades from turn board
            BoardFragment turnBoard = (BoardFragment) pagerAdapter.getFragment(Game.getInstance().getTurn());
            if (turnBoard != null) {
                turnBoard.update();
            }

            // update the board and spinner
            updateSpinner();

            clearSelections();

            setConfirmButton(false);
        }
    }

    private class ConfirmButtonAbilityListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            boolean resolve = true;
            try {
                if (onAbilityConfirmListener == null) {
                    resolve = activeAbility.resolve();
                } else {
                    onAbilityConfirmListener.onConfirm();
                    onAbilityConfirmListener = null;
                }
            }
            catch (Exception e) {
                if (e instanceof EndException) {
                    endTurn();
                } else {
                    throw e;
                }
            }
            if (resolve) {
                setAbilityDisplay(null);
            }
            setConfirmButton(false);
        }
    }

    /**
     * This holds the action to be taken when the end turn button is clicked
     * The button must be clicked twice within 1 second in order to officially end the turn
     */
    private class EndTurnButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // get button
            final Button endTurn = (Button) v;

            // if second tap is within 1 second
            if (!secureEndTurn || endTurn.getText().equals(getString(R.string.tap_again))) {
                endTurn();

                // reset text
                endTurn.setText(getString(R.string.end_turn));
            } else {
                // wait for second tap
                endTurn.setText(getString(R.string.tap_again));

                // revert text after 1 second
                new Handler().postDelayed(() -> endTurn.setText(getString(R.string.end_turn)), 1000);
            }
        }
    }

    /**
     * Holds the action to be taken when the attack button is clicked
     */
    private class AttackButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // clear selections
            selectedCards.clear();

            // get button
            Button attack = (Button) v;

            // toggle attack mode
            if (attackMode) {
                setAttackMode(false, true);
                attack.setText(getString(R.string.attack));
            } else {
                setAttackMode(true, true);
                attack.setText(getString(R.string.cancel));
            }

            updateSpinner();
        }
    }

    /**
     * The action when a new board is shown
     */
    private class BoardPagerListener extends ViewPager.SimpleOnPageChangeListener {
        private int currentPosition;
        private int scrollState;

        public void onPageScrollStateChanged(final int state) {
            handleScrollState(state);
            scrollState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        private void handleScrollState(final int state) {
            View arrows = findViewById(R.id.boardArrows);
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                if (arrows.getVisibility() != View.VISIBLE) {
                    arrows.setVisibility(View.VISIBLE);
                }
                setNextItemIfNeeded();
            } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                arrows.setVisibility(View.INVISIBLE);
            } else if (state == ViewPager.SCROLL_STATE_SETTLING) {
                if (arrows.getVisibility() != View.VISIBLE) {
                    arrows.setVisibility(View.VISIBLE);
                }
            }
        }

        private void setNextItemIfNeeded() {
            if (scrollState != ViewPager.SCROLL_STATE_SETTLING) {
                int lastPosition = pagerAdapter.getRealCount() - 1;
                if (currentPosition == 0) {
                    pager.setCurrentItem(lastPosition, false);//attackMode);
                } else if (currentPosition == lastPosition) {
                    pager.setCurrentItem(0, false);//attackMode);
                }
            }
        }

        @Override
        public void onPageSelected(int position) {
            int realPosition = position % pagerAdapter.getRealCount();

            currentPosition = realPosition;

            // if player can attack
            if (Game.getInstance().getRound() >= Game.ATTACK_ROUND) {
                Button attack = findViewById(R.id.btnAttack);
                setAttackMode(false, false);
                attack.setText(getString(R.string.attack));

                attack.setEnabled(false);
                attack.setVisibility(View.GONE);

                // if board shown is an opponent
                if (Game.getInstance().getPlayer(realPosition).isAttackableBy(Game.getInstance()
                        .getPlayer()) && !autoplay) {
                    // enable attack button
                    findViewById(R.id.btnAttack).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnAttack).setEnabled(true);
                }
            }

            ft = fm.beginTransaction();

            // swap the hand and card Fragments
            if (realPosition == Game.getInstance().getTurn() && !autoplay) {
                ft.replace(R.id.frameCard, new CardFragment(), CARD_FRAGMENT_TAG);
                ft.replace(R.id.frameHand, HandFragment.newInstance(realPosition),
                        HAND_FRAGMENT_TAG);
            } else {
                ft.replace(R.id.frameCard, HandFragment.newInstance(realPosition),
                        HAND_FRAGMENT_TAG);
                ft.replace(R.id.frameHand, new CardFragment(), CARD_FRAGMENT_TAG);
            }
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();

            if (abilityShown) {
                setAbilityDisplay(null);
                if (activeAbility != null) {
                    activeAbility.deactivate();
                }
            } else {
                updateSpinnerPosition(position);
            }

            clearSelections();
            setPlayableSelectionOn(true);
            setMaxSelectable(1);

            setConfirmButton(false);

            pager.setBackground(teamBackgrounds.get(Game.getInstance().getPlayer(position)
                    .getStartingTurn() % Game.getInstance().getTeams()));

            discardPileShown = false;
        }
    }

    /**
     * This adapter is built to display boards.
     *
     * The board relies on features of ViewPager and FragmentStatePagerAdapter that do not have
     * clear alternatives in ViewPager2 and FragmentStateAdapter.
     */
    @SuppressWarnings("deprecation")
    public class BoardPagerAdapter extends FragmentStatePagerAdapter {
        /**
         * holds the Fragments that this object creates
         */
        private final SparseArray<Fragment> fragments = new SparseArray<>();

        public BoardPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        private boolean invalidated = false;

        public int getItemPosition(@NonNull Object object) {
            // if attack mode has changed, prepare for update
            if (object instanceof BoardFragment && attackMode || invalidated) {
                return POSITION_NONE;
            }
            return POSITION_UNCHANGED;
        }

        void invalidate() {
            invalidated = true;
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            BoardFragment boardFragment = BoardFragment.newInstance(position % getRealCount(), attackMode && position == getBoardShown());
            if (attackMode && position == getBoardShown()) {
                lastBoard = boardFragment;
            }
            return boardFragment;
        }

        @Override
        public int getCount() {
            return Game.getInstance().getPlayerCount();
        }

        public int getRealCount() {
            return Game.getInstance().getPlayerCount();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NotNull ViewGroup container, int position) {
            // add fragment to list
            Fragment f = (Fragment) super.instantiateItem(container, position);
            fragments.put(position, f);
            return f;
        }

        @Override
        public void destroyItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
            // remove fragment from list
            fragments.remove(position);
            super.destroyItem(container, position % getRealCount(), object);
        }

        /**
         * @param key the position of the fragment to obtain
         * @return the fragment at the given position
         */
        public Fragment getFragment(int key) {
            return fragments.get(key);
        }

        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            invalidated = false;
        }

        public void finishUpdate(@NotNull ViewGroup container) {
            try {
                super.finishUpdate(container);
            }
            catch (NullPointerException e) {
                System.out.println("NullPointerException caught in BoardPagerAdapter.finishUpdate");
            }
        }
    }

    /**
     * do nothing on back pressed
     */
    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(GameScreen.UI);
    }

    public static class DialogBuilder {
        private final ViewGroup gameFrame;
        private final View dialog;
        private final LinearLayout dialogLayout;
        private final TextView title;
        private final TextView message;
        private final Button btnPositive;
        private final Button btnNegative;
        private final GameScreen gameScreen;
        private OnDismissListener onDismissListener;
        private View.OnClickListener positiveOnClickListener;
        private View.OnClickListener negativeOnClickListener;

        private static int popups;

        public DialogBuilder(GameScreen gameScreen) {
            this.gameScreen = gameScreen;
            gameFrame = gameScreen.findViewById(R.id.gameScreenFrame);
            dialog = gameScreen.getLayoutInflater().inflate(R.layout.dialog_general, gameFrame, false);
            dialogLayout = dialog.findViewById(R.id.layoutDialog);
            title = dialogLayout.findViewById(R.id.txtTitle);
            message = dialogLayout.findViewById(R.id.txtMessage);
            btnPositive = dialogLayout.findViewById(R.id.btnPositive);
            btnNegative = dialogLayout.findViewById(R.id.btnNegative);

            btnPositive.setOnClickListener(v -> {
                dismiss();
                if (positiveOnClickListener != null) {
                    positiveOnClickListener.onClick(v);
                }
            });

            btnNegative.setOnClickListener(v -> {
                dismiss();
                if (negativeOnClickListener != null) {
                    negativeOnClickListener.onClick(v);
                }
            });

            dialog.setOnClickListener(v -> dismiss());
        }

        public DialogBuilder setTitle(CharSequence title) {
            this.title.setText(title);
            return this;
        }

        public DialogBuilder setMessage(CharSequence message) {
            this.message.setText(message);
            return this;
        }

        public DialogBuilder setPositiveButton(CharSequence text, View.OnClickListener listener) {
            btnPositive.setText(text);
            positiveOnClickListener = listener;
            btnPositive.setVisibility(View.VISIBLE);
            return this;
        }

        public DialogBuilder setNegativeButton(CharSequence text, View.OnClickListener listener) {
            btnNegative.setText(text);
            negativeOnClickListener = listener;
            btnNegative.setVisibility(View.VISIBLE);
            return this;
        }

        public DialogBuilder setFullScreen(boolean fullScreen) {
            FrameLayout.LayoutParams params;

            if (fullScreen) {
                params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                params.setMargins(0, 0, 0, 0);
            } else {
                params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
                int margin = (int) gameScreen.getResources().getDimension(R.dimen.default_margin);
                params.setMargins(margin, margin, margin, margin);
            }
            LinearLayout layout = dialog.findViewById(R.id.layoutDialog);
            layout.setLayoutParams(params);

            return this;
        }

        public DialogBuilder setCancelable(boolean cancelable) {
            if (cancelable) {
                dialog.setOnClickListener(v -> dismiss());
            } else {
                dialog.setOnClickListener(null);
            }
            return this;
        }

        public DialogBuilder setOnDismissListener(OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
            return this;
        }

        public void dismiss() {
            gameFrame.removeView(dialog);
            if (onDismissListener != null) {
                onDismissListener.onDismiss();
            }
        }

        public void show() {
            gameFrame.addView(dialog);
        }

        public DialogBuilder setRadioGroup(CharSequence[] strings, RadioGroup.OnCheckedChangeListener listener) {
            RadioGroup radioGroup = new RadioGroup(gameScreen);

            for (int i = 0; i < strings.length; i++) {
                RadioButton radioButton = new RadioButton(gameScreen);
                radioButton.setId(i);
                radioButton.setText(strings[i]);
                radioGroup.addView(radioButton, new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            }

            radioGroup.check(0);
            radioGroup.setOnCheckedChangeListener(listener);

            dialogLayout.addView(radioGroup, dialogLayout.getChildCount() - 2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            return this;
        }

        public interface OnDismissListener {
            void onDismiss();
        }

        public static void addPopup(final GameScreen gameScreen, View v, boolean cancelable) {
            ViewGroup frame = gameScreen.findViewById(R.id.gameScreenFrame);
            ViewGroup overlay = (ViewGroup) gameScreen.getLayoutInflater().inflate(R.layout.dialog_overlay, frame, false);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            int margin = (int) gameScreen.getResources().getDimension(R.dimen.default_margin);
            params.setMargins(margin, margin, margin, margin);
            v.setLayoutParams(params);

            if (cancelable) {
                overlay.setOnClickListener(v1 -> removeTopPopup(gameScreen));
            }

            overlay.addView(v);
            frame.addView(overlay);

            popups++;
        }

        public static void removeTopPopup(GameScreen gameScreen) {
            if (popups > 0) {
                ViewGroup frame = gameScreen.findViewById(R.id.gameScreenFrame);
                frame.removeViewAt(frame.getChildCount() - 1);
                popups--;
            }
        }

        private static void reset() {
            popups = 0;
        }
    }
}