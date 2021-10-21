package com.rei.flowroomdemo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.afollestad.recyclical.datasource.dataSourceTypedOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.rei.flowroomdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    var isGrid = true
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    val grid by lazy {
        GridLayoutManager(this@MainActivity, 2)
    }
    private val localDB by lazy {
        provideDatabase(this)
    }
    val data = dataSourceTypedOf(
        CartData(1, "Wortel"), CartData(2, "Toge"), CartData(3, "Micin"),
        CartData(4, "Gorengan"), CartData(5, "Ayam"), CartData(6, "Mustart")
    )

    val vm by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rvData.setup {
            withDataSource(data)
            withLayoutManager(grid)
            withItem<CartData, CartViewHolder>(R.layout.adapter_cart) {
                onBind(::CartViewHolder) { _, item ->
                    name.text = item.name
                    qty.text = item.qty.toString()
                    plus.setOnClickListener {
                        vm.update(localDB, item.id, item.qty + 1)
                    }
                    minus.setOnClickListener {
                        vm.update(localDB, item.id, item.qty - 1)
                    }
                }
            }
        }
        binding.cartIcon.setOnClickListener {
            if (grid.spanCount == 1) {
                grid.spanCount = 2
            } else {
                grid.spanCount = 1
            }
        }
        lifecycleScope.launch {
            localDB.cartDao().getCart().collect { cart ->
                val list = mutableListOf<CartData>()
                data.forEach { menu ->
                    cart.find {
                        it.id == menu.id
                    }?.apply {
                        list.add(menu.copy(qty = this.qty))
                    } ?: run {
                        list.add(menu.copy(qty = 0))
                    }
                }
                data.clear()
                data.addAll(list)
                binding.cartCount.text = cart.size.toString()
            }
        }
    }

    fun provideDatabase(context: Context) =
        Room.databaseBuilder(
            context,
            LocalDB::class.java,
            "transdb"
        ).build()
}
