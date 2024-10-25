package ru.mrak.mafiagame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.mrak.mafiagame.R

class TutorialSlideFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = arguments?.getInt("layout_id") ?: R.layout.tutorial_screen_1
        return inflater.inflate(layoutId, container, false)
    }

}