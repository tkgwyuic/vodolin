package com.tunesworks.vodolin.activity

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewPager
import android.support.v7.view.ActionMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.squareup.otto.Subscribe
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.VoDolin
import com.tunesworks.vodolin.event.ItemSelectionChangeEvent
import com.tunesworks.vodolin.event.RequestTabScrollEvent
import com.tunesworks.vodolin.event.ToDoEvent
import com.tunesworks.vodolin.fragment.ListFragment
import com.tunesworks.vodolin.fragment.PagerAdapter
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.model.itemColor
import com.tunesworks.vodolin.model.status
import com.tunesworks.vodolin.value.ItemColor
import com.tunesworks.vodolin.value.ToDoStatus
import com.tunesworks.vodolin.value.primary
import com.tunesworks.vodolin.value.primaryDark
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    companion object {
        val EXTRA_UUID = "EXTRA_UUID"
    }
    val REQUEST_CODE = 0


    val pagerAdapter by lazy { PagerAdapter(supportFragmentManager) }
    var actionMode: ActionMode? = null
    var isActionModeStarted = false

    // Subscribing
    @Subscribe fun tabScroll(event: RequestTabScrollEvent) {
        view_pager.currentItem = ItemColor.values().indexOf(ItemColor.valueOf(event.itemColorName))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val uuid = intent.getStringExtra(EXTRA_UUID)
        if (uuid != null) DetailActivity.IntentBuilder(this).setUUID(uuid).start()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register EventBus
        VoDolin.bus.register(this)

        // Set Toolbar
        toolbar.apply {
            title = "ToDo List"
            inflateMenu(R.menu.menu_main)

            // Set listener
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.search -> SearchActivity.IntentBuilder(this@MainActivity).start()
                    else -> return@setOnMenuItemClickListener false
                }
                true
            }
        }

        // Set AppBar background color
        appbar.setBackgroundColor(ItemColor.values()[0].color)

        // Set footer action listener
        footer_action.setOnClickListener { view ->
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Please Speech")
                }
                startActivityForResult(intent, REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Error: Activity Not Found!", Toast.LENGTH_SHORT).show()
            }
            // Hide modal shadow
            footer_edit.clearFocus()
        }

        //
        modal_shadow.setOnClickListener { finishInputMode() }

        // Set footer
        footer_edit.apply {
            imeOptions = EditorInfo.IME_ACTION_DONE

            // Change input mode
            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) startInputMode()
                else finishInputMode()
            }

            //
            setOnEditorActionListener { textView, actionId, keyEvent ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        var content = textView.text.toString().trim { it == ' ' || it == '　' }
                        if (content.length > 0) createToDo(content) // Create if not blank

                        textView.text = "" // Reset edit text
                        footer_edit.clearFocus() // Clear focus
                        return@setOnEditorActionListener  true
                    }
                }
                false
            }
        }

        // Set ViewPager
        view_pager.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 3

            // Animation
            var prevItemColor = ItemColor.DEFAULT
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    val itemColor = ItemColor.values()[position]

                    // Animation toolbar background color
                    ValueAnimator.ofObject(ArgbEvaluator(), prevItemColor.primary, itemColor.primary).apply {
                        addUpdateListener { appbar.setBackgroundColor(it.animatedValue as Int) }
                        duration = 500
                        interpolator = DecelerateInterpolator()
                        start()
                    }

                    // Animation statusbar color
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ValueAnimator.ofObject(ArgbEvaluator(), prevItemColor.primaryDark, itemColor.primaryDark).apply {
                            addUpdateListener { window.statusBarColor = it.animatedValue as Int }
                            duration = 500
                            interpolator = DecelerateInterpolator()
                            start()
                        }
                    }

                    // Save color
                    prevItemColor = itemColor

                    // Finish ActionMode
                    actionMode?.finish()
                }
                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            })
        }

        // Set ViewPager
        tabs.setupWithViewPager(view_pager)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister EventBus
        VoDolin.bus.unregister(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                SearchActivity.IntentBuilder(this).start()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((REQUEST_CODE == requestCode) && (RESULT_OK == resultCode)) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (results != null) {
                createToDo(results.first())
                results.forEach { Log.d(this@MainActivity.javaClass.name, "Recognizer Result: $it") }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (footer_edit.isFocused) finishInputMode()
        else finish()
    }

    override fun getSnackbarContainer(): CoordinatorLayout? {
        return coordinator
    }

    fun createToDo(content: String) {
        val todo = ToDo(
                content = content,
                itemColorName = ItemColor.values()[tabs.selectedTabPosition].toString()
        )

        Realm.getInstance(this).use { realm ->
            if (realm.isInTransaction) realm.commitTransaction()
            realm.executeTransaction { realm.copyToRealm(todo) }
        }

        VoDolin.bus.post(ToDoEvent.ChangeAll(todo.itemColorName))

        makeSnackbar("Create new ToDo!")?.apply {
            setActionTextColor(todo.itemColor.primary)
            setAction("EDIT", {
                DetailActivity.IntentBuilder.from(this@MainActivity).setUUID(todo.uuid).start()
            })
            show()
        }
    }

    fun startInputMode() {
        // Show modal shadow
        modal_shadow.visibility = View.VISIBLE
    }

    fun finishInputMode() {
        // Clear focus
        footer_edit.clearFocus()

        // Hide keyboard
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(modal_shadow.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        // Hide modal shadow
        modal_shadow.visibility = View.GONE
    }

    @Subscribe fun itemSelectionChanged(event: ItemSelectionChangeEvent) {
        val selectedItemCount = event.selectedItemCount
        val itemColor = event.itemColor
        if (selectedItemCount > 0) { // Selected item
            if (!isActionModeStarted) {
                actionMode = startSupportActionMode( object : ActionMode.Callback {
                    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                        isActionModeStarted = true
                        menu?.clear()
                        mode?.menuInflater?.inflate(R.menu.action_mode, menu)
                        return true
                    }

                    override fun onActionItemClicked(mode: ActionMode?, menuItem: MenuItem?): Boolean {
                        val pos = ItemColor.values().indexOf(itemColor)
                        val fragment = pagerAdapter.instantiateItem(view_pager, pos) as ListFragment
                        when (menuItem?.itemId) {
                            R.id.done ->   fragment.dismissSelectedItems { it.status = ToDoStatus.DONE }
                            R.id.failed -> fragment.dismissSelectedItems { it.status = ToDoStatus.FAILED }
                            R.id.delete -> fragment.dismissSelectedItems { it.removeFromRealm() }
                        }
                        mode?.finish()
                        return true
                    }

                    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                        mode?.title = "$selectedItemCount Selected"
                        return true
                    }

                    override fun onDestroyActionMode(mode: ActionMode?) {
                        val pos = ItemColor.values().indexOf(itemColor)
                        (pagerAdapter.instantiateItem(view_pager, pos) as ListFragment).todoAdapter.clearSelections()
                        isActionModeStarted = false
                    }
                })
            } else { // ActionMode already started
                actionMode?.title = "$selectedItemCount Selected"
            }
        } else { // Not selected
            actionMode?.finish()
        }
    }

//    override fun onItemSelectionChanged(selectedItemCount: Int, itemColor: ItemColor) {
//        if (selectedItemCount > 0) { // Selected item
//            if (!isActionModeStarted) {
//                actionMode = startSupportActionMode( object : ActionMode.Callback {
//                    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//                        isActionModeStarted = true
//                        menu?.clear()
//                        mode?.menuInflater?.inflate(R.menu.action_mode, menu)
//                        return true
//                    }
//
//                    override fun onActionItemClicked(mode: ActionMode?, menuItem: MenuItem?): Boolean {
//                        val pos = ItemColor.values().indexOf(itemColor)
//                        val fragment = pagerAdapter.instantiateItem(view_pager, pos) as ListFragment
//                        when (menuItem?.itemId) {
//                            R.id.done ->   fragment.dismissSelectedItems { it.status = ToDoStatus.DONE }
//                            R.id.failed -> fragment.dismissSelectedItems { it.status = ToDoStatus.FAILED }
//                            R.id.delete -> fragment.dismissSelectedItems { it.removeFromRealm() }
//                        }
//                        mode?.finish()
//                        return true
//                    }
//
//                    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//                        mode?.title = "$selectedItemCount Selected"
//                        return true
//                    }
//
//                    override fun onDestroyActionMode(mode: ActionMode?) {
//                        val pos = ItemColor.values().indexOf(itemColor)
//                        (pagerAdapter.instantiateItem(view_pager, pos) as ListFragment).todoAdapter.clearSelections()
//                        isActionModeStarted = false
//                    }
//                })
//            } else { // ActionMode already started
//                actionMode?.title = "$selectedItemCount Selected"
//            }
//        } else { // Not selected
//            actionMode?.finish()
//        }
//    }

    open class IntentBuilder(val context: Context) {
        val intent = Intent(context, MainActivity::class.java)

        fun setUUID(uuid: String): IntentBuilder {
            intent.putExtra(EXTRA_UUID, uuid)
            return this
        }

        fun build() = intent
        fun start() { context.startActivity(intent) }
    }
}
