package me.ztiany.simple.bus.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import timber.log.Timber

class AFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(AViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_a, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.update).setOnClickListener {
            viewModel.updateLiveData()
        }

        view.findViewById<View>(R.id.jump_and_update).setOnClickListener {
            requireActivity()
                .supportFragmentManager
                .commit {
                    add<BFragment>(R.id.act_frag_container)
                        .setMaxLifecycle(this@AFragment, Lifecycle.State.STARTED)
                        .hide(this@AFragment)
                        .addToBackStack("BFragment")
                }
            lifecycleScope.launchWhenStarted {
                delay(2000)
                viewModel.updateLiveData()
            }
        }
    }

    private fun subscribeViewModel() {
        viewModel.normalLiveData.observe(this) {
            Timber.d("normalLiveData: $it")
        }

        viewModel.singleLiveData.observe(this) {
            Timber.d("singleLiveData: $it")
        }

        viewModel.enhancedLiveData.observe(this) {
            Timber.d("enhancedLiveData: $it")
        }

        viewModel.singleEnhancedLiveData.observe(this) {
            Timber.d("singleEnhancedLiveData: $it")
        }

        viewModel.enhancedLiveData.observe(this, Lifecycle.State.RESUMED) {
            Timber.d("enhancedLiveData-RESUMED: $it")
        }

        viewModel.singleEnhancedLiveData.observe(this, Lifecycle.State.RESUMED) {
            Timber.d("singleEnhancedLiveData-RESUMED: $it")
        }
    }

}