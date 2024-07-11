package me.ztiany.simple.bus.example

import androidx.lifecycle.EnhancedLiveData
import androidx.lifecycle.EnhancedMutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.base.foundation.livedata.SingleLiveData

class AViewModel : ViewModel() {

    private var count = 1

    private val _normalLiveData = MutableLiveData("NormalLiveData-1")
    private val _singleLiveData = SingleLiveData("SingleLiveData-2")
    private val _enhancedLiveData = EnhancedMutableLiveData(false, "EnhancedLiveData-1")
    private val _singleEnhancedLiveData = EnhancedMutableLiveData(true, "SingleEnhancedLiveData-1")

    val normalLiveData: LiveData<String>
        get() = _normalLiveData
    val singleLiveData: LiveData<String>
        get() = _singleLiveData
    val enhancedLiveData: EnhancedLiveData<String>
        get() = _enhancedLiveData
    val singleEnhancedLiveData: EnhancedLiveData<String>
        get() = _singleEnhancedLiveData

    fun updateLiveData() {
        count++
        _normalLiveData.value = "NormalLiveData-$count"
        _singleLiveData.value = "SingleLiveData-$count"
        _enhancedLiveData.value = "EnhancedLiveData-$count"
        _singleEnhancedLiveData.value = "SingleEnhancedLiveData-$count"
    }

}