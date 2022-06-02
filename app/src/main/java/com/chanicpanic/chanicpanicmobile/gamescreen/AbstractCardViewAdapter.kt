/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.gamescreen

import android.util.SparseArray
import android.util.SparseBooleanArray
import java.util.*

abstract class AbstractCardViewAdapter<ViewHolder : androidx.recyclerview.widget.RecyclerView.ViewHolder>(count: Int) : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

    private val properties: SparseArray<SparseBooleanArray> = SparseArray()

    init {
        for (i in 0 until count) {
            properties.put(i, SparseBooleanArray())
        }
    }

    /**
     *
     * @return the total number of selected items in the list
     */
    fun countOf(property: Int): Int {
        return itemsWith(property).size
    }

    /**
     *
     * @return an ArrayList containing the indices of all selected items
     */
    fun itemsWith(property: Int): List<Int> {
        val prop = properties.get(property)
        val items = ArrayList<Int>(prop.size())
        for (i in 0 until prop.size()) {
            if (prop.get(prop.keyAt(i))) {
                items.add(prop.keyAt(i))
            }
        }
        return items
    }

    /**
     * deselects all items in the list
     */
    fun clearProperty(property: Int) {
        properties.get(property).clear()
    }

    /**
     *
     * @param position the index of the item to examine
     * @return true if the item at the given index is selected, false otherwise
     */
    @JvmOverloads
    fun hasProperty(position: Int, property: Int, default: Boolean = false): Boolean {
        val prop = properties.get(property)
        if (prop.indexOfKey(position) < 0) {
            prop.put(position, default)
            return default
        }
        return prop.get(position, default)
    }

    /**
     * changes the selection state of the item at the given index
     * @param position the index of the item to change
     */
    fun toggle(position: Int, property: Int) {
        val prop = properties.get(property)
        prop.put(position, !prop.get(position))
    }

    fun setProperty(position: Int, property: Int, value: Boolean) {
        val prop = properties.get(property)
        prop.put(position, value)
    }

    fun delete(position: Int) {
        properties.get(CardGroupView.CardViewAdapter.SELECTED).delete(position)
        properties.get(CardGroupView.CardViewAdapter.SELECTABLE).delete(position)
        properties.get(CardGroupView.CardViewAdapter.CLICKABLE).delete(position)
    }
}

