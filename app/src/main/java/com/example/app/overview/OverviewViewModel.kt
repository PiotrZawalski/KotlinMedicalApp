package com.example.app.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.network.ImageApi
import com.example.app.network.MedicalImage
import kotlinx.coroutines.launch

enum class ImageApiStatus { LOADING, ERROR, DONE }

class OverviewViewModel : ViewModel() {

    private val _status = MutableLiveData<ImageApiStatus>()
    val status: LiveData<ImageApiStatus> = _status
    private val _photos = MutableLiveData<List<MedicalImage>>()
    val photos: LiveData<List<MedicalImage>> = _photos

    init {
        getRtgPhotos()
    }

    private fun getRtgPhotos() {

        viewModelScope.launch {
            _status.value = ImageApiStatus.LOADING
            try {
                _photos.value = ImageApi.retrofitService.getPhotos()
                _status.value = ImageApiStatus.DONE
            } catch (e: Exception) {
                _status.value = ImageApiStatus.ERROR
                _photos.value = listOf()
            }
        }
    }
}
