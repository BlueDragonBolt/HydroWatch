package com.example.hydrowatch.ui.waterCounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hydrowatch.databinding.FragmentWaterCounterBinding

class WaterCounterFragment : Fragment() {

    private var _binding: FragmentWaterCounterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val waterCounterViewModel =
            ViewModelProvider(requireActivity())[WaterCounterViewModel::class.java]

        _binding = FragmentWaterCounterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textWaterCounter
        waterCounterViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        waterCounterViewModel.counter.observe(viewLifecycleOwner) {
            textView.text = "You've used ${it}L of water"
        }
        val counterButton = binding.counterButton
        counterButton.text = "Use more!"
        counterButton.setOnClickListener {
            // slideshowViewModel.setText("a${slideshowViewModel.counter.value}")
            waterCounterViewModel.incrementCounter()
            if (waterCounterViewModel.counter.value!! % 5 != 0) {
                counterButton.text = listOf("Splish", "Splash", "Gulp", "Blob", "Blib").random()
            } else {
                counterButton.text = listOf("Splish", "Splash", "Gulp", "Blob", "Blib", "Oh, sure, just spill it on the floor, why don't you").random()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}