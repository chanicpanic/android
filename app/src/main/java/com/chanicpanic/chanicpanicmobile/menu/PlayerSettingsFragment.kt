/*
 * Copyright (c) chanicpanic 2022
 */
package com.chanicpanic.chanicpanicmobile.menu

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.databinding.FragmentPlayerSettingsBinding
import com.chanicpanic.chanicpanicmobile.game.Game

/**
 * This activity displays a list of players and settings for them
 */
class PlayerSettingsFragment : Fragment() {

    private var _binding: FragmentPlayerSettingsBinding? = null
    private val binding get() = _binding!!

    /**
     * the time of the last click
     */
    private var click: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = FragmentPlayerSettingsBinding.inflate(layoutInflater, container, false)
       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Game.getInstance().loadPlayers()
        binding.recyclerPlayerSettings.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = PlayerSettingsAdapter()
            itemAnimator = null
        }
        binding.btnStart.setOnClickListener(::onStart)
        initializeColorsUsed()
    }

    /**
     * an adapter for this activity's recycler
     */
    private inner class PlayerSettingsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val v = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_player_settings, parent, false) as PlayerSettingsView
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ViewHolder).v.setPosition(position, this@PlayerSettingsFragment::notifyColorChange)
        }

        override fun getItemCount(): Int {
            return Game.getInstance().playerCount
        }

        inner class ViewHolder internal constructor(val v: PlayerSettingsView) :
            RecyclerView.ViewHolder(
                v
            )
    }

    /**
     * updates the adapter for a change in a team color
     *
     * @param index the index of the color changed
     */
    fun notifyColorChange(index: Int) {
        if (Game.getInstance().startingPlayerCount != Game.getInstance().teams) {
            val t = Game.getInstance().teams
            var i = index % t
            val z = Game.getInstance().startingPlayerCount
            while (i < z) {
                binding.recyclerPlayerSettings.adapter!!.notifyItemChanged(i)
                i += t
            }
        }
    }

    /**
     * click event to start
     *
     * @param v the View clicked
     */
    fun onStart(v: View?) {
        // start the GameScreen may take extra time
        // make sure this is not double-tapped
        if (System.currentTimeMillis() - click > 1000) {
            click = System.currentTimeMillis()
            var issue = false

            // check all player settings for any input issues
            var i = 0
            val childCount = binding.recyclerPlayerSettings.childCount
            while (i < childCount) {
                val holder = binding.recyclerPlayerSettings.getChildViewHolder(
                    binding.recyclerPlayerSettings.getChildAt(i)
                ) as PlayerSettingsAdapter.ViewHolder
                if (!holder.v.update()) {
                    issue = true
                }
                i++
            }
            if (issue) {
                Toast.makeText(requireContext(), "Names must be at least 1 character", Toast.LENGTH_SHORT)
                    .show()
            } else {
                try {
                    // hide keyboard
                    val focused = requireActivity().currentFocus
                    if (focused != null) {
                        (requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                            focused.windowToken,
                            0
                        )
                    }

                    // start game
                    findNavController().navigate(R.id.action_playerSettingsFragment_to_gameScreen)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error creating game", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        /**
         * holds which team colors are being used
         */
        private val teamColorsUsed = SparseBooleanArray()

        /**
         * sets the teamColors used to the first ones
         */
        private fun initializeColorsUsed() {
            teamColorsUsed.clear()
            var i = 0
            val z = Game.getInstance().teams
            while (i < z) {
                teamColorsUsed.put(i, true)
                i++
            }
        }

        /**
         * @param index the index of the color being changed
         * @return the index of the next available color
         */
        fun getNextTeamColorIndex(index: Int): Int {
            var idx = index
            teamColorsUsed.put(idx, false)
            var i = 0
            val z = GameScreen.TEAM_COLORS.size
            while (i < z - 1) {
                idx = ++idx % z
                if (!teamColorsUsed[idx, false]) {
                    teamColorsUsed.put(idx, true)
                    return idx
                }
                i++
            }
            return 0
        }
    }
}