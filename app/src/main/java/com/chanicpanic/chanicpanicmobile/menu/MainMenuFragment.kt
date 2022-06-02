/*
 * Copyright (c) chanicpanic 2022
 */
package com.chanicpanic.chanicpanicmobile.menu

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.databinding.FragmentMainMenuBinding
import com.chanicpanic.chanicpanicmobile.game.Game
import java.util.*

class MainMenuFragment : Fragment() {
    private var animatingMenu = false
    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLoadGame.isEnabled = (requireContext().getDir("saves", MODE_PRIVATE).list()?.size ?: 0) > 0
        binding.btnNewGame.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mainMenuFragment_to_newGameFragment))
        binding.btnLoadGame.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mainMenuFragment_to_loadGameFragment))
        binding.btnInstructions.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mainMenuFragment_to_instructionsFragment))
        binding.btnSettings.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mainMenuFragment_to_settingsActivity))
    }

    override fun onResume() {
        super.onResume()
        binding.btnLoadGame.isEnabled = (requireContext().getDir("saves", MODE_PRIVATE).list()?.size ?: 0) > 0
    }

    override fun onStart() {
        super.onStart()
        val model: AnimationViewModel by activityViewModels()
        if (model.shouldAnimate) {
            model.shouldAnimate = false
            animateMenu()
        }
        animateAbilitySpotlight()
    }

    private fun animateMenu() {
        animatingMenu = true
        val INITIAL_DELAY = 2000
        val interpolator = FastOutSlowInInterpolator()

        val r = Runnable {
            binding.gameName.animate()
                .translationY(0f)
                .setDuration(1000).interpolator = interpolator
        }

        binding.gameName.alpha = 0f
        binding.gameName.y = resources.displayMetrics.heightPixels * .45f
        binding.gameName.animate()
            .alpha(1f).duration = 1000
        Handler().postDelayed(r, INITIAL_DELAY.toLong())

        binding.btnNewGame.alpha = 0f
        binding.btnNewGame.scaleX = .1f
        binding.btnNewGame.animate()
            .setStartDelay((INITIAL_DELAY + 500).toLong())
            .alpha(1f)
            .scaleX(1f)
            .setDuration(1000)
            .setInterpolator(interpolator)
            .start()

        binding.btnLoadGame.alpha = 0f
        binding.btnLoadGame.scaleX = .1f
        binding.btnLoadGame.animate()
            .setStartDelay((INITIAL_DELAY + 1000).toLong())
            .alpha(1f)
            .scaleX(1f)
            .setDuration(1000)
            .setInterpolator(interpolator)
            .start()

        binding.btnInstructions.alpha = 0f
        binding.btnInstructions.scaleX = .1f
        binding.btnInstructions.animate()
            .setStartDelay((INITIAL_DELAY + 1500).toLong())
            .alpha(1f)
            .scaleX(1f)
            .setDuration(1000)
            .setInterpolator(interpolator)
            .start()

        binding.btnSettings.alpha = 0f
        binding.btnSettings.scaleX = .1f
        binding.btnSettings.animate()
            .setStartDelay((INITIAL_DELAY + 2000).toLong())
            .alpha(1f)
            .scaleX(1f)
            .setDuration(1000)
            .setInterpolator(interpolator)
            .withEndAction {
                animatingMenu = false
                animateAbilitySpotlight()
            }
            .start()
    }

    private fun animateAbilitySpotlight() {
        if (!animatingMenu) {
            binding.layoutAbilitySpotlight.visibility = View.VISIBLE
            val nameBase = R.string.ability_name_ally
            val descriptionBase = R.string.ability_description_ally
            val commentBase = R.string.ability_commentary_ally

            val selection = Random().nextInt(Game.ABILITIES_ID_NAMES.size)
            val ability = Game.ABILITIES_ID_NAMES[selection]
            binding.txtSpotlight.alpha = 0f
            binding.txtSpotlight.animate().alphaBy(1f)
                .setDuration(1000)
                .setStartDelay(500)
                .start()

            //            spotlight.setText(getString(nameBase + selection));
            binding.txtSpotlightName.text =
                getString(resources.getIdentifier("ability_name_$ability", "string", requireContext().packageName))
            binding.txtSpotlightName.translationX = -50f
            binding.txtSpotlightName.alpha = 0f
            binding.txtSpotlightName.animate().alphaBy(1f)
                .translationX(0f)
                .setDuration(1000)
                .setStartDelay(1000)
                .start()

            binding.txtSpotlightDescription.translationY = 50f
            binding.txtSpotlightDescription.alpha = 0f
            //            spotlight.setText(descriptionBase + selection);
            binding.txtSpotlightDescription.text =
                getString(
                    resources.getIdentifier(
                        "ability_description_$ability",
                        "string",
                        requireContext().packageName
                    )
                )
            binding.txtSpotlightDescription.animate().alphaBy(1f)
                .translationY(0f)
                .setDuration(1000)
                .setStartDelay(1000)
                .start()

            binding.txtSpotlightComment.translationY = 50f
            binding.txtSpotlightComment.alpha = 0f
            //            spotlight.setText(commentBase + selection);
            binding.txtSpotlightComment.text =
                getString(
                    resources.getIdentifier(
                        "ability_commentary_$ability",
                        "string",
                        requireContext().packageName
                    )
                )
            binding.txtSpotlightComment.animate().alphaBy(1f)
                .translationY(0f)
                .setDuration(1000)
                .setStartDelay(1500)
                .start()
        }
    }
}

class AnimationViewModel : ViewModel() {
    var shouldAnimate = true
}
