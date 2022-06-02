/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.settings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.databinding.PreferenceCardSkinBinding

class CardSkinPreference : DialogPreference{
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        dialogLayoutResource = R.layout.preference_card_skin
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        setColors(DEFAULT_TOP to DEFAULT_BOTTOM)
    }

    fun setColors(colors: Pair<Long, Long>) {
        persistLong(colors.first shl 32 or (colors.second and COLOR_MASK))
    }

    fun getColors(): Pair<Long, Long> {
        val packed = getPersistedLong(DEFAULT_VALUE)
        val top = (packed ushr 32) and COLOR_MASK
        val bottom = packed and COLOR_MASK
        return top to bottom
    }

    companion object {
        private const val COLOR_MASK = 0x00000000FFFFFFFFL

        const val DEFAULT_TOP = 0xff6d40acL
        const val DEFAULT_BOTTOM = 0xff9e68c4L
        const val DEFAULT_VALUE = DEFAULT_TOP shl 32 or DEFAULT_BOTTOM
    }
}

class CardSkinPreferenceDialog : PreferenceDialogFragmentCompat() {

    private var topColor = CardSkinPreference.DEFAULT_TOP

    private var bottomColor = CardSkinPreference.DEFAULT_BOTTOM

    private var _binding: PreferenceCardSkinBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialogView(context: Context?): View {
        _binding = PreferenceCardSkinBinding.inflate(layoutInflater, null, false)
        return binding.root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)

        with(view!!) {
            val colors = (preference as CardSkinPreference).getColors()
            topColor = colors.first
            bottomColor = colors.second

            val preview = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(topColor.toInt(), bottomColor.toInt())).apply {
                setStroke((3 * context.resources.displayMetrics.density + .5f).toInt(), Color.BLACK)
            }

            binding.cardPreview.background = preview

            binding.btnToggle.setBackgroundColor(topColor.toInt())
            binding.btnToggle.setTextColor(invertColor(topColor))

            binding.colorPicker.setColor(topColor.toInt(), false)

            binding.btnDefault.setOnClickListener {
                topColor = CardSkinPreference.DEFAULT_TOP
                bottomColor = CardSkinPreference.DEFAULT_BOTTOM
                val color = if (binding.btnToggle.isChecked) bottomColor.toInt() else topColor.toInt()
                binding.colorPicker.setColor(color, false)
                binding.btnToggle.apply {
                    setBackgroundColor(color)
                    setTextColor(invertColor(color.toLong()))
                }
                preview.colors = intArrayOf(topColor.toInt(), bottomColor.toInt())
            }

            val colorListener = { color: Int ->
                binding.btnToggle.apply {
                    setBackgroundColor(color)
                    setTextColor(invertColor(color.toLong()))
                    if (isChecked) {
                        bottomColor = color.toLong()
                    } else {
                        topColor = color.toLong()
                    }
                }
                preview.colors = intArrayOf(topColor.toInt(), bottomColor.toInt())
            }

            binding.colorPicker.apply {
                addOnColorChangedListener { colorListener(it) }
                addOnColorSelectedListener { colorListener(it) }
            }

            binding.btnToggle.setOnCheckedChangeListener { _, isChecked ->
                with(binding.btnToggle) {
                    val color = if (isChecked) bottomColor else topColor
                    binding.colorPicker.setColor(color.toInt(), false)
                    setBackgroundColor(color.toInt())
                    setTextColor(invertColor(color))
                }
            }

        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            (preference as CardSkinPreference).setColors(topColor to bottomColor)
        }
    }

    private fun invertColor(color: Long): Int {
        return ((color and 0xFF000000) or (color.inv() and 0x00FFFFFF)).toInt()
    }

    companion object {
        fun newInstance(key: String) = CardSkinPreferenceDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_KEY, key)
            }
        }
    }
}