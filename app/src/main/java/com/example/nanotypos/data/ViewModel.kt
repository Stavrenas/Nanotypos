package com.example.nanotypos.data

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.Barcode

class ViewModel : ViewModel() {
    private val _uri = MutableLiveData<Uri>()
    val uri: LiveData<Uri> = _uri


    private val _logoButtonSelected = MutableLiveData<Boolean>()
    val logoButtonSelected: LiveData<Boolean> = _logoButtonSelected

    private val _barcode = MutableLiveData<Barcode>()
    val barcode: LiveData<Barcode> = _barcode


    private val _text=MutableLiveData<String>()
    val text: LiveData<String> = _text

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

    fun getBarcode(): Barcode? {
        return  _barcode.value
    }

    fun setBarcode(bar: Barcode){
        _barcode.value = bar
    }

    fun setTextValue(str: String){
        _text.value = str
    }

    fun getUrl(): String {
        return _barcode.value?.url!!.url
    }

    fun getTextValue(): String? {
        return _text.value
    }


    init {
        _logoButtonSelected.value = true

    }


}

@BindingAdapter("resource")
fun setImageUri(view: ImageView, imageUri: Uri) {
    view.setImageURI(imageUri)
}



