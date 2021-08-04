package com.example.nanotypos.data

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModel : ViewModel() {
    private val _uri = MutableLiveData<Uri>()
    val uri: LiveData<Uri> = _uri


    private val _logoButtonSelected = MutableLiveData<Boolean>()
    val logoButtonSelected: LiveData<Boolean> = _logoButtonSelected

    fun setModelUri(targetUri: Uri){
        _uri.value = targetUri
    }

    fun getModelUri() : Uri? {
        return  _uri.value
    }

    fun toggleLogoButton() {
        _logoButtonSelected.value = !_logoButtonSelected.value!!
    }

    fun getLogoButton(): Boolean? {
       return _logoButtonSelected.value
    }


    init {
        _logoButtonSelected.value = true

    }


}

@BindingAdapter("resource")
fun setImageUri(view: ImageView, imageUri: Uri) {
    view.setImageURI(imageUri)
}



