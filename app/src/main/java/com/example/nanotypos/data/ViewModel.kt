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

    fun setModelUri(targetUri: Uri){
        _uri.value = targetUri
    }

    fun getModelUri() : Uri? {
        return  _uri.value
    }

    init {

    }


}

@BindingAdapter("resource")
fun setImageUri(view: ImageView, imageUri: Uri) {
    view.setImageURI(imageUri)
}



