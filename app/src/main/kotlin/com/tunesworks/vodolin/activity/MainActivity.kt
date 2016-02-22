package com.tunesworks.vodolin.activity

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TabLayout
import android.support.v7.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.VoDolin
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

class MainActivity : BaseActivity(), ListFragment.OnItemSelectionChangeListener {
    val REQUEST_CODE = 0

    val pagerAdapter by lazy { PagerAdapter(supportFragmentManager) }
    var actionMode: ActionMode? = null
    var isActionModeStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "ToDo List"

        appbar.setBackgroundColor(ItemColor.values()[0].color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.statusBarColor = ItemColor.values()[0].color

        footer_action.setOnClickListener { view ->
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Please Speech")
            }
            startActivityForResult(intent, REQUEST_CODE)

            modal_shadow.performClick()
        }

        modal_shadow.setOnClickListener { coordinator.requestFocus() }

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
                        if (content.length > 0) createToDo(content)

                        textView.text = ""
                        coordinator.requestFocus()
                        return@setOnEditorActionListener  true
                    }
                }
                false
            }
        }

        view_pager.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 3
        }

        tabs.apply {
            var prevItemColor = ItemColor.DEFAULT

            setupWithViewPager(view_pager)

            setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab ?: return

                    val itemColor = ItemColor.values()[tab.position]

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

                    // Scroll view pager
                    view_pager.currentItem = tab.position

                    // Finish ActionMode
                    actionMode?.finish()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((REQUEST_CODE == requestCode) && (RESULT_OK == resultCode)) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (results != null) createToDo(results.first())
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (footer_edit.isFocused) finishInputMode()
        else super.onBackPressed()
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

        VoDolin.observers.apply {
            setChanged()
            notifyObservers(ListFragment.ChangeToDoEvent(todo.itemColorName))
        }

        makeSnackbar("Create new ToDo!")?.apply {
            setActionTextColor(todo.itemColor.primary)
            setAction("EDIT", {
                // ToDo: start edit activity
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
        // Change focus from edit text
        coordinator.requestFocus()

        // Hide keyboard
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(modal_shadow.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        // Hide modal shadow
        modal_shadow.visibility = View.GONE
    }

    override fun onItemSelectionChanged(selectedItemCount: Int, itemColor: ItemColor) {
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
                            R.id.done -> {
                                fragment.dismissSelectedItems { it.status = ToDoStatus.DONE }
                            }
                            R.id.failed -> {
                                fragment.dismissSelectedItems { it.status = ToDoStatus.FAILED }
                            }
                            R.id.delete -> {
                                fragment.dismissSelectedItems { it.removeFromRealm() }
                            }
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
}
