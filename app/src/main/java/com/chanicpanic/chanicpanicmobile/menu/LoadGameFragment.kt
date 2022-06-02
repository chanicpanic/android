/*
 * Copyright (c) chanicpanic 2022
 */
package com.chanicpanic.chanicpanicmobile.menu

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.databinding.FragmentLoadGameBinding
import java.io.File
import java.util.*

/**
 * This activity displays a list of game to load/delete
 */
class LoadGameFragment : Fragment() {
    /**
     * a list of saved games
     */
    private val files: MutableList<File> = ArrayList()

    private var _binding: FragmentLoadGameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get save files and sort by last saved time
        requireContext().getDir("saves", MODE_PRIVATE).listFiles()?.also {
            files.addAll(it)
        }
        Collections.sort(files, FileComparator())

        // set up recyclerView
        binding.recyclerLoadGame.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.recyclerLoadGame.adapter = LoadGameAdapter()
    }

    /**
     * deletes a file
     *
     * @param file the file to delete
     */
    fun delete(file: File) {
        val index = files.indexOf(file)
        if (index > -1) {
            // remove file from list
            files.remove(file)

            // get sub files and delete them
            file.listFiles()?.also {
                for (file1 in it) {
                    file1.delete()
                }
            }

            //delete this file
            file.delete()

            // update recycler
            binding.recyclerLoadGame.adapter!!.notifyItemRemoved(index)

            // go back if no more saves exist
            if (files.isEmpty()) {
                findNavController().popBackStack()
            }
        }
    }

    /**
     * this is an adapter for the recyclerView in this activity
     */
    private inner class LoadGameAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val v = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_load_game, parent, false) as LoadGameView
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val file = files[position]
            (holder as ViewHolder).v.set(file)
            { _, _ -> delete(file) }
        }

        override fun getItemCount(): Int {
            return files.size
        }

        inner class ViewHolder internal constructor(val v: LoadGameView) : RecyclerView.ViewHolder(v)
    }

    /**
     * This comparator compares this game's save files based on when they were last modified
     */
    private class FileComparator : Comparator<File?> {
        override fun compare(o1: File?, o2: File?): Int {
            val difference = File(o1, "save").lastModified() - File(o2, "save").lastModified()
            if (difference == 0L) {
                return 0
            }
            return if (difference > 0) -1 else 1
        }
    }
}