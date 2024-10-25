package ru.mrak.mafiagame.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.mrak.mafiagame.fragments.TutorialSlideFragment

class TutorialPagerAdapter(
    fragment: Fragment,
    private val layouts: List<Int>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = layouts.size

    override fun createFragment(position: Int): Fragment {
        val fragment = TutorialSlideFragment()
        fragment.arguments = Bundle().apply {
            putInt("layout_id", layouts[position])
        }
        return fragment
    }
}
