package com.example.gbbank.ui.home

import android.animation.ValueAnimator
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gbbank.MainActivity
import com.example.gbbank.databinding.FragmentHomeBinding
import com.example.gbbank.extensions.showSnackBar
import com.example.gbbank.ui.base.BaseFragment
import com.example.gbbank.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()
    private var lastBalance: Double? = 0.0

    override fun start() {
        val activity = requireActivity() as? MainActivity
        activity?.showToolBar()
        realTimeCallBack()
        listener()
    }

    private fun listener() {
        binding.btnDeposit.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToDepositFragment(
                lastBalance!!.toFloat()
            ))
        }
    }


    private fun realTimeCallBack() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.realTimeResponse.collect {
                when(it) {
                    is Resource.Success -> {
                        enableButtons()
                        binding.animLoading.isVisible = false
                        binding.tvName.text = it.data?.firstName
                        lastBalance = it.data?.balance
                        balanceAnim(it.data?.balance!!.toFloat(), it.data.balance.toString().length)
                        binding.tvFullName.text = it.data.firstName?.plus(" ").plus(it.data.lastName)
                    }
                    is Resource.Error -> {
                        binding.animLoading.isVisible = false
                        view?.showSnackBar(it.errorMessage.toString())
                    }
                    is Resource.Loading -> {
                        binding.animLoading.isVisible = true
                        binding.animLoading.playAnimation()
                        disableButtons()
                    }
                }
            }
        }
    }

    private fun balanceAnim(balance: Float, length: Int) {
        val animator = ValueAnimator.ofFloat(0.0F, balance)
        animator.setDuration(900)
        animator.addUpdateListener { animation ->
            val number = animation.animatedValue.toString().take(length)
            binding.tvBalance.setText(number)
        }
        animator.start()
    }

    private fun disableButtons() {
        with(binding) {
            btnDeposit.isEnabled = false
            btnDeposit.isClickable = false
        }
    }

    private fun enableButtons() {
        with(binding) {
            btnDeposit.isEnabled = true
            btnDeposit.isClickable = true
        }
    }

}