package com.rei.flowroomdemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.io.IOException

import android.util.Log

import java.io.FileOutputStream

import java.io.OutputStream

import java.io.InputStream

import java.io.File

import okhttp3.ResponseBody


class MainViewModel : ViewModel() {
    private val _message = MutableLiveData<String>()
    val message = _message
    fun update(localDB: LocalDB, id: Int, qty: Int) {
        viewModelScope.launch {
            if (qty > 0) {
                localDB.cartDao().insert(CartEntity(id = id, qty = qty))
            } else {
                localDB.cartDao().delete(id)
            }
        }
    }

    fun getFile(retrofit: Repository, url: String, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            retrofit.downloadFileWithDynamicUrlAsync(url)?.apply {
                writeResponseBodyToDisk(this, file).apply {
                    message.postValue("Done")
                }
            }
        }
    }

    private fun writeResponseBodyToDisk(body: ResponseBody, futureStudioIconFile: File): Boolean {
        return try {

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)
                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Log.d("Download", "file download: $fileSizeDownloaded of $fileSize")
                }
                outputStream.flush()
                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            false
        }
    }
}