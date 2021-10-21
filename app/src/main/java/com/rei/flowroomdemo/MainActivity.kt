package com.rei.flowroomdemo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.afollestad.recyclical.datasource.dataSourceOf
import com.afollestad.recyclical.datasource.dataSourceTypedOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.google.zxing.integration.android.IntentIntegrator
import com.rei.flowroomdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.widget.Toast

import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.CaptureActivity
import android.view.View

import com.journeyapps.barcodescanner.DecoratedBarcodeView


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
            withItem<CartDataLinear, CartViewHolder>(R.layout.adapter_cart_linear) {
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
            redrawLayout(backup)
        }
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
        binding.scan.setOnClickListener {
            IntentIntegrator(this).apply {
                captureActivity = PortraitActivity::class.java
                setPrompt("Scan Kartu Bermain Anda")
                setBeepEnabled(true)
                setOrientationLocked(false)
                setBarcodeImageEnabled(true)
                initiateScan()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
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
        Room.databaseBuilder(
            context,
            LocalDB::class.java,
            "transdb"
        ).build()
}

class PortraitActivity() : CaptureActivity() {
    override fun initializeContent(): DecoratedBarcodeView? {
        setContentView(R.layout.activity_portrait)
        return findViewById<View>(R.id.zxing_barcode_scanner) as DecoratedBarcodeView
    }
}
