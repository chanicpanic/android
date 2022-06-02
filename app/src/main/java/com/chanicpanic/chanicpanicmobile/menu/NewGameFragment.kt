/*
 * Copyright (c) chanicpanic 2022
 */
package com.chanicpanic.chanicpanicmobile.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.databinding.FragmentNewGameBinding
import com.chanicpanic.chanicpanicmobile.game.Game

/**
 * This activity displays options for a new game
 */
class NewGameFragment : Fragment() {

    private var _binding: FragmentNewGameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btn1v1.setOnClickListener(::onClick)
        binding.btn1v1v1.setOnClickListener(::onClick)
        binding.btn2v2.setOnClickListener(::onClick)
        binding.btnCustom.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_newGameFragment_to_gameSettingsFragment))
    }

    fun onClick(v: View) {
        // load a game with the preset settings
        when (v.id) {
            R.id.btn1v1 -> {
                Game.getInstance().playerCount = 2
                Game.getInstance().teams = 2
            }
            R.id.btn1v1v1 -> {
                Game.getInstance().playerCount = 3
                Game.getInstance().teams = 3
            }
            R.id.btn2v2 -> {
                Game.getInstance().playerCount = 4
                Game.getInstance().teams = 2
            }
        }
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        Game.getInstance().isTraderActive =
            preferences.getBoolean(getString(R.string.PREF_TRADER_KEY), true)
        Game.getInstance().isAllyActive =
            Game.getInstance().teams != Game.getInstance().startingPlayerCount && preferences.getBoolean(
                getString(R.string.PREF_ALLY_KEY),
                true
            )
        Game.getInstance().isPanicActive =
            preferences.getBoolean(getString(R.string.PREF_PANIC_KEY), false)
        Game.getInstance().abilities =
            preferences.getString(getString(R.string.PREF_SPECIAL_ABILITIES_KEY), "0")!!
                .toInt()
        Game.getInstance().isPresenceActive =
            preferences.getBoolean(getString(R.string.PREF_PRESENCE_KEY), false)
        findNavController().navigate(R.id.action_newGameFragment_to_playerSettingsFragment)
    }
}