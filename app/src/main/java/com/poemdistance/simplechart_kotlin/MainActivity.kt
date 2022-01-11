package com.poemdistance.simplechart_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.poemdistance.simplechart_kotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    val jsonData = listOf("""
        [{"staDate":"01-09","profit":235,"activeOrderCount":36},{"staDate":"01-08","profit":244,"activeOrderCount":33},{"staDate":"01-07","profit":159,"activeOrderCount":29},{"staDate":"01-06","profit":452,"activeOrderCount":48},{"staDate":"01-05","profit":116,"activeOrderCount":19},{"staDate":"01-04","profit":131,"activeOrderCount":22},{"staDate":"01-03","profit":345,"activeOrderCount":32},{"staDate":"01-02","profit":277,"activeOrderCount":35},{"staDate":"01-01","profit":206,"activeOrderCount":28}]
    """.trimIndent(),"""
        [{"staDate":"12-31","profit":191,"activeOrderCount":26},{"staDate":"12-30","profit":177,"activeOrderCount":24},{"staDate":"12-29","profit":190,"activeOrderCount":18},{"staDate":"12-28","profit":33,"activeOrderCount":7},{"staDate":"12-27","profit":144,"activeOrderCount":13},{"staDate":"12-26","profit":45,"activeOrderCount":10},{"staDate":"12-25","profit":38,"activeOrderCount":7},{"staDate":"12-24","profit":111,"activeOrderCount":13},{"staDate":"12-23","profit":227,"activeOrderCount":28},{"staDate":"12-22","profit":105,"activeOrderCount":10},{"staDate":"12-21","profit":50,"activeOrderCount":7},{"staDate":"12-20","profit":123,"activeOrderCount":9},{"staDate":"12-19","profit":99,"activeOrderCount":10},{"staDate":"12-18","profit":84,"activeOrderCount":12},{"staDate":"12-17","profit":130,"activeOrderCount":7},{"staDate":"12-16","profit":40,"activeOrderCount":5},{"staDate":"12-15","profit":53,"activeOrderCount":7},{"staDate":"12-14","profit":220,"activeOrderCount":20},{"staDate":"12-13","profit":51,"activeOrderCount":11},{"staDate":"12-12","profit":56,"activeOrderCount":8},{"staDate":"12-11","profit":106,"activeOrderCount":20},{"staDate":"12-10","profit":158,"activeOrderCount":19},{"staDate":"12-09","profit":167,"activeOrderCount":17},{"staDate":"12-08","profit":163,"activeOrderCount":21},{"staDate":"12-07","profit":88,"activeOrderCount":16},{"staDate":"12-06","profit":54,"activeOrderCount":8},{"staDate":"12-05","profit":202,"activeOrderCount":27},{"staDate":"12-04","profit":55,"activeOrderCount":7},{"staDate":"12-03","profit":53,"activeOrderCount":7},{"staDate":"12-02","profit":118,"activeOrderCount":17},{"staDate":"12-01","profit":80,"activeOrderCount":12}]
    """.trimIndent())

    var dataIndex = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.barChart.updateData(jsonData[dataIndex % 2])
        binding.lineChart.updateData(jsonData[dataIndex % 2])
        binding.pieChart.updateData(jsonData[dataIndex % 2])

        binding.button.setOnClickListener {
            dataIndex++
            binding.barChart.updateData(jsonData[dataIndex % 2])
            binding.lineChart.updateData(jsonData[dataIndex % 2])
            binding.pieChart.updateData(jsonData[dataIndex % 2])
        }
        binding.lineChart.listener =  {
            Toast.makeText(this,it?:"",Toast.LENGTH_SHORT).show()
        }


    }
}