package com.example.hydrowatch.ui.medals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hydrowatch.databinding.FragmentMedalsBinding

class MedalsFragment : Fragment() {

    private var _binding: FragmentMedalsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val medalsViewModel =
            ViewModelProvider(this).get(MedalsViewModel::class.java)

        _binding = FragmentMedalsBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textMedals
//        medalsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}