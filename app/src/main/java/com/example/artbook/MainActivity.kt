package com.example.artbook

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbconnect()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_art, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_art_item) {
            val intent = Intent(this, ProfilActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        dbconnect()
        super.onResume()
    }

    fun dbconnect() {

        val artNameList = ArrayList<String>()
        val artIdList = ArrayList<Int>()
        val lv = findViewById<ListView>(R.id.listview)
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, artNameList)
        lv.adapter = arrayAdapter

        try {
            val database = openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null)
            val cursor = database.rawQuery("SELECT *FROM arts", null)

            val artNamex = cursor.getColumnIndex("artname")
            val artIdx = cursor.getColumnIndex("id")
            while (cursor.moveToNext()) {
                artNameList.add(cursor.getString(artNamex))
                artIdList.add(cursor.getInt(artIdx))

            }
            arrayAdapter.notifyDataSetChanged()
            cursor.close()

        } catch (ex: Exception) {
            println(ex.printStackTrace())
        }
        lv.onItemClickListener=AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent=Intent(this,ProfilActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",artIdList[position])
            startActivity(intent)
        }

    }
}
