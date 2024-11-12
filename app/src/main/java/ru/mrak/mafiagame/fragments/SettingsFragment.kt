package ru.mrak.mafiagame.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.widget.SwitchCompat
import androidx.navigation.fragment.findNavController
import ru.mrak.mafiagame.MainActivity.Companion.APP
import ru.mrak.mafiagame.R
import ru.mrak.mafiagame.service.DataService

class SettingsFragment : Fragment() {

    private lateinit var backButton: Button
    private lateinit var tutorialButton: Button

    private lateinit var languageSpinner: Spinner
    private lateinit var doctorSelfHealSwitch: SwitchCompat
    private lateinit var doctorHealSameSwitch: SwitchCompat
    private lateinit var revealRoleSwitch: SwitchCompat
    private lateinit var showSavedPlayerSwitch: SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        backButton = view.findViewById(R.id.backButton)
        languageSpinner = view.findViewById(R.id.languageSpinner)
        doctorSelfHealSwitch = view.findViewById(R.id.doctorSelfHealSwitch)
        doctorHealSameSwitch = view.findViewById(R.id.doctorHealSameSwitch)
        revealRoleSwitch = view.findViewById(R.id.revealRoleSwitch)
        tutorialButton = view.findViewById(R.id.tutorialButton)
        showSavedPlayerSwitch = view.findViewById(R.id.showSavedPlayerSwitch)

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLanguage = if (position == 0) "en" else "ru"
                // Сохраняем язык в SharedPreferences
                val prefs = requireContext().getSharedPreferences("mafia_game", Context.MODE_PRIVATE)
                prefs.edit().putString("language", selectedLanguage).apply()

                // Обновляем язык для TTS или других частей приложения
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        languageSpinner.visibility = View.GONE

        tutorialButton.setOnClickListener {
            DataService.tutorialShow = false
            findNavController().navigate(R.id.tutorialFragment)
        }

        val settings = DataService.settings!!

        doctorSelfHealSwitch.isChecked = settings.doctorSelfHeal
        doctorHealSameSwitch.isChecked = settings.doctorHealSame
        revealRoleSwitch.isChecked = settings.revealRole

        doctorSelfHealSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.doctorSelfHeal = isChecked
            DataService.settings = settings
        }

        doctorHealSameSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.doctorHealSame = isChecked
            DataService.settings = settings
        }

        revealRoleSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.revealRole = isChecked
            DataService.settings = settings
        }

        showSavedPlayerSwitch.isChecked = settings.showSavedPlayer
        showSavedPlayerSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.showSavedPlayer = isChecked
            DataService.settings = settings
        }

        return view
    }
}
