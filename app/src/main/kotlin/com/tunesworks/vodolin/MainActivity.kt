package com.tunesworks.vodolin

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import com.tunesworks.vodolin.fragment.ListFragment
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.value.ItemColor
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.util.*

class MainActivity : AppCompatActivity() {
    val REQUEST_CODE = 0

    val dataList = ArrayList<String>()

    val pagerAdapter by lazy { PagerAdapter(supportFragmentManager) }
    //val toolbar      by lazy { findViewById(R.id.toolbar) as Toolbar }
    //val fab          by lazy { findViewById(R.id.fab) as FloatingActionButton }
    //val tabs         by lazy { findViewById(R.id.tabs) as TabLayout }
    //val viewPager    by lazy { findViewById(R.id.view_pager) as ViewPager }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

//        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity).apply {
//            orientation = LinearLayoutManager.VERTICAL
//        }
//        recyclerView.adapter = adapter

        fab.setOnClickListener { view ->
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Please Speech")
            }
            startActivityForResult(intent, REQUEST_CODE)
        }

        view_pager.adapter = pagerAdapter
        tabs.setupWithViewPager(view_pager)

        tabs.apply {
            var prevColor = ItemColor.DEFAULT.color
            setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab ?: return

                    // Set selected tab indicator color
                    val color = ItemColor.values()[tab.position].color

                    ValueAnimator.ofObject(ArgbEvaluator(), prevColor, color).apply {
                        addUpdateListener { appbar.setBackgroundColor(it.animatedValue as Int) }
                        duration = 500
                        interpolator = DecelerateInterpolator()
                        start()
                    }

                    ValueAnimator.ofObject(ArgbEvaluator(), prevColor, color).apply {
                        addUpdateListener { window.statusBarColor = it.animatedValue as Int }
                        duration = 500
                        interpolator = DecelerateInterpolator()
                        start()
                    }

                    tabs.setSelectedTabIndicatorColor(color)
                    prevColor = color

                    view_pager.currentItem = tab.position
                }
            })

            // Init appbar color
            getTabAt(selectedTabPosition)?.select()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((REQUEST_CODE == requestCode) && (RESULT_OK == resultCode)) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return
            Toast.makeText(this, results.first(), Toast.LENGTH_LONG).show()

            val todo = ToDo(
                    content = results.first(),
                    itemColorName = ItemColor.values()[tabs.selectedTabPosition].toString()
            )

            Realm.getInstance(this).use { realm ->
                realm.executeTransaction {
                    realm.copyToRealm(todo)
                }
            }

            VoDolin.observers.apply {
                setChanged()
                notifyObservers( ListFragment.ChangeToDoEvent(todo.itemColorName) )
            }

            //dataList.add(0, results.first())
            //adapter.notifyItemInserted(0)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
