package com.rei.flowroomdemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _cartData = MutableLiveData<List<CartEntity>>()
    val cartData = _cartData
    fun update(localDB: LocalDB, id: Int, qty: Int) {
        viewModelScope.launch {
            if (qty > 0) {
                localDB.cartDao().insert(CartEntity(id = id, qty = qty))
            } else {
                localDB.cartDao().delete(id)
            }
        }
    }

    suspend fun getCart(localDB: LocalDB) {
        viewModelScope.launch {
            localDB.cartDao().getCart().collect {
                _cartData.postValue(it)
            }
        }
    }
}