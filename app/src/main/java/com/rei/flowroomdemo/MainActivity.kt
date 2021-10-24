package com.rei.flowroomdemo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.Room
import com.afollestad.recyclical.datasource.dataSourceOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.rei.flowroomdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

import androidx.lifecycle.Observer

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File

const val URL_SQLITE =
    "https://store3.gofile.io/download/a4f6f10d-02c0-4b6b-95cd-4c5290b8bb3a/room.sqlite"

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
    val sourceData = listOf(
        CartData(1, "Wortel"), CartData(2, "Toge"), CartData(3, "Micin"),
        CartData(4, "Gorengan"), CartData(5, "Ayam"), CartData(6, "Mustart")
    )
    val backup = mutableListOf<CartData>()
    val data = dataSourceOf()

    val vm by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }
    val cache by lazy {
        File(cacheDir, "room.sqlite")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rvData.setup {
            withDataSource(data)
            withLayoutManager(grid)
            withItem<CartData, CartViewHolder>(R.layout.adapter_cart) {
                onBind(::CartViewHolder) { _, item ->
                    populateData(this, item.toLinear())
                }
            }
            withItem<CartDataLinear, CartViewHolder>(R.layout.adapter_cart_linear) {
                onBind(::CartViewHolder) { _, item ->
                    populateData(this, item)
                }
            }

        }
        binding.cartIcon.setOnClickListener {
            if (grid.spanCount == 1) {
                grid.spanCount = 2
            } else {
                grid.spanCount = 1
            }
            redrawLayout(backup)
        }
        vm.getFile(
            provideAPI(),
            URL_SQLITE,
            cache
        )
        vm.message.observe(this, Observer {
            showToast(it)
            if (cache.exists()) {
                lifecycleScope.launch {
                    localDB.cartDao().getCart().collect { cart ->
                        binding.cartCount.text = cart.size.toString()
                        val list = mutableListOf<CartData>()
                        sourceData.forEach { menu ->
                            cart.find {
                                it.id == menu.id
                            }?.apply {
                                list.add(menu.copy(qty = this.qty))
                            } ?: run {
                                list.add(menu)
                            }
                        }
                        backup.clear()
                        backup.addAll(list)
                        redrawLayout(list)
                    }
                }
            }
        })


        binding.scan.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }
    }

    private fun populateData(cartViewHolder: CartViewHolder, item: CartDataLinear) {
        cartViewHolder.apply {
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


    fun redrawLayout(list: MutableList<CartData>) {
        data.clear()
        if (grid.spanCount == 1) {
            list.map {
                it.toLinear()
            }.apply {
                data.addAll(this)
            }
        } else {
            data.addAll(list)
        }
    }

    fun provideDatabase(context: Context) =
//        Room.databaseBuilder(
//            context,
//            LocalDB::class.java,
//            "room.db"
//        ).createFromFile(cache).fallbackToDestructiveMigration().build()
        Room.databaseBuilder(
            context,
            LocalDB::class.java,
            "room.db"
        ).createFromAsset("room.sqlite").fallbackToDestructiveMigration().build()

    fun provideOkHttp() = OkHttpClient.Builder().build()

    fun provideRetrofit() =
        Retrofit.Builder().client(provideOkHttp()).baseUrl("http://www.africau.edu").build()

    fun provideAPI() = provideRetrofit().create(Repository::class.java)
}