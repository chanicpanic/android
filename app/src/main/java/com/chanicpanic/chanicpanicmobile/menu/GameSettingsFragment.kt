/*
 * Copyright (c) chanicpanic 2022
 */
package com.chanicpanic.chanicpanicmobile.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.databinding.FragmentGameSettingsBinding
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.game.isPrime

class GameSettingsFragment : Fragment() {
    private lateinit var binding: FragmentGameSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_game_settings, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val model: GameSettingsModel by viewModels()
        binding.settings = model
        binding.fragment = this
        binding.spnPlayers.adapter = ArrayAdapter(requireContext(), R.layout.textview_list, players)
        binding.spnAbilities.adapter = ArrayAdapter(requireContext(), R.layout.textview_list, abilities)
    }

    fun onPlayersSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {
        view?.let {
            val startingPlayerCount = (view as TextView).text.toString().toInt()
            binding.settings!!.startingPlayerCount = startingPlayerCount
            if (!isPrime(startingPlayerCount)) {
                when {
                    startingPlayerCount > MAX_FREE_FOR_ALL -> {
                        // teams are mandatory
                        binding.swtTeams.isChecked = true
                        binding.swtTeams.isEnabled = false
                        setTeamsSpinner()
                    }
                    binding.swtTeams.isChecked -> {
                        // teams are optional
                        binding.swtTeams.isEnabled = true
                        setTeamsSpinner()
                    }
                    else -> {
                        // teams are optional
                        binding.swtTeams.isEnabled = true
                        binding.settings!!.teams = startingPlayerCount
                    }
                }
            } else {
                // teams are disallowed
                binding.settings!!.teams = startingPlayerCount
                binding.swtTeams.apply {
                    isChecked = false
                    isEnabled = false
                }
                binding.spnTeams.apply {
                    adapter = null
                    isEnabled = false
                }
            }
        }
    }

    fun onTeamsChecked(button: CompoundButton, isChecked: Boolean) {
        if (isChecked) {
            if (!isPrime(binding.settings!!.startingPlayerCount)) {
                setTeamsSpinner()
            }
            binding.spnTeams.isEnabled = true
        } else {
            binding.settings!!.teams = binding.settings!!.startingPlayerCount
            binding.spnTeams.adapter = null
            binding.spnTeams.isEnabled = false
        }
    }

    private fun setTeamsSpinner() {
        teams.clear()
        for (i in 2..binding.settings!!.startingPlayerCount / 2) {
            if (binding.settings!!.startingPlayerCount % i == 0) {
                teams.add(i.toString())
            }
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.textview_list, teams)
        binding.spnTeams.adapter = adapter
        binding.settings!!.teams = teams[0].toInt()
    }

    fun onTeamsSelected(
            parent: AdapterView<*>?,
            view: View,
            position: Int,
            id: Long
    ) {
        binding.settings!!.teams = (view as TextView).text.toString().toInt()
    }

    fun onAbilitiesSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {
        view?.let {
            binding.settings!!.abilities = (view as TextView).text.toString().toInt()
        }
    }

    fun onAllyChecked(button: CompoundButton, isChecked: Boolean) {
        binding.settings!!.isAllyActive = isChecked
    }

    fun start(view: View?) {
        Game.getInstance().apply {
            startingPlayerCount = binding.settings!!.startingPlayerCount
            teams = binding.settings!!.teams
            abilities = binding.settings!!.abilities
            isTraderActive = binding.settings!!.isTraderActive.get()
            isAllyActive = binding.settings!!.isAllyActive
            isPanicActive = binding.settings!!.isPanicActive.get()
            isPresenceActive = binding.settings!!.isPresenceActive.get()
        }
        findNavController().navigate(R.id.action_gameSettingsFragment_to_playerSettingsFragment)
    }

    companion object {
        private val players = arrayOf("2", "3", "4", "5", "6", "7", "8", "9", "10")
        private val abilities = arrayOf("0", "1", "2", "3")
        private val teams = ArrayList<String>()
        private const val MAX_FREE_FOR_ALL = 7
    }
}

class GameSettingsModel : ViewModel() {
    var startingPlayerCount: Int = 2
    var teams: Int = 2
    var abilities: Int = 0
    var isTraderActive: ObservableBoolean = ObservableBoolean(true)
    var isAllyActive: Boolean = false
    var isPanicActive: ObservableBoolean = ObservableBoolean(false)
    var isPresenceActive: ObservableBoolean = ObservableBoolean(false)
}