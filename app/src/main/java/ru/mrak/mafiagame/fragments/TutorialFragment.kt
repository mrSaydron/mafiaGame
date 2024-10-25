package ru.mrak.mafiagame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import ru.mrak.mafiagame.R
import ru.mrak.mafiagame.adapter.TutorialPagerAdapter
import ru.mrak.mafiagame.service.DataService

class TutorialFragment : Fragment() {

    private lateinit var progessBar: ProgressBar
    private lateinit var viewPager: ViewPager2
    private lateinit var finishButton: Button
    private lateinit var adapter: TutorialPagerAdapter

    private val tutorialScreens = listOf(
        R.layout.tutorial_screen_1,
        R.layout.tutorial_screen_2,
        R.layout.tutorial_screen_3,
        R.layout.tutorial_screen_4,
        R.layout.tutorial_screen_5,
        R.layout.tutorial_screen_6,
        R.layout.tutorial_screen_7,
        R.layout.tutorial_screen_8,
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tutorial, container, false)

        if (DataService.tutorialShow == true) {
            findNavController().navigate(R.id.playerListFragment)
        }

        viewPager = view.findViewById(R.id.viewPager)
        finishButton = view.findViewById(R.id.finishButton)
        progessBar = view.findViewById(R.id.progressBar)

        adapter = TutorialPagerAdapter(this, tutorialScreens)
        viewPager.adapter = adapter

        progessBar.max = tutorialScreens.size - 2
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (viewPager.currentItem == tutorialScreens.size - 1) {
                    DataService.tutorialShow = true
                    findNavController().navigate(R.id.playerListFragment)
                }
                progessBar.progress = position
                super.onPageSelected(position)
            }

//            override fun onPageScrollStateChanged(state: Int) {
//                super.onPageScrollStateChanged(state)
//                if (viewPager.currentItem == tutorialScreens.size - 1) {
//                    findNavController().navigate(R.id.playerListFragment)
//                }
//            }
        })

        finishButton.setOnClickListener {
            DataService.tutorialShow = true
            findNavController().navigate(R.id.playerListFragment)
        }

        return view
    }

}