package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.widget.Toast
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private  var myDatabindingIdeResource = DataBindingIdlingResource()




    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }




//    complete TODO: add End to End testing to the app


   // ideling resource tell Espresso that app is busy or not
    // this is needed operations are not scheduled in the main Looper
    @Before
    fun registerIdLingResource(){
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(myDatabindingIdeResource)
    }

   // Unregister the Idling Resource so as not to cause a memory failure
    @After
    fun unRegistertIdlingResource(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(myDatabindingIdeResource)
    }



    //This function use to test reminderListData
    @Test
    fun reminderList_DataNotFound(){
        //start up taken screen
        val activity = ActivityScenario.launch(RemindersActivity::class.java)
        // add screen in binding
        myDatabindingIdeResource.monitorActivity(activity)

        //vreify item is displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        activity.close()

    }


    //this function use to now buttonSnackBar
    @Test
    fun saveReminder_showSnackBar_noTitleFound() = runBlocking{

        //start up taken screen
        val activity = ActivityScenario.launch(RemindersActivity::class.java)
        // add screen in binding
        myDatabindingIdeResource.monitorActivity(activity)

        // click  to navgation to saveReminder screen
        onView(withId(R.id.addReminderFAB)).perform(click())
        //click saveReminder button to check from snakBat
        onView(withId(R.id.saveReminder)).perform(click())

        //verify snackBar value  is displayed
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))

        //make sure activity is close after finished test
        activity.close()

        }

    @Test
    fun saveReminder_showSnackBar_nolocationFound() = runBlocking{

        //start up taken screen
        val activity = ActivityScenario.launch(RemindersActivity::class.java)
        // add screen in binding
        myDatabindingIdeResource.monitorActivity(activity)

        // click  to navgation to saveReminder screen
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Add title to saveReminder to display in list
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("newReminder"),
            closeSoftKeyboard()
        )
        // Add description to saveReminder to display in list
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("new"),
            closeSoftKeyboard()
        )
        // add to Nav to reminderList
        onView(withId(R.id.saveReminder)).perform(click())
        // check snackbar is display
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_select_location)))
        //make sure activity is close after finished test
        activity.close()

    }


    @Test
    fun selectLocationAndEnterTitle_NavigateToListFragment_DataFound(){
        //start up taken screen
        val activity = ActivityScenario.launch(RemindersActivity::class.java)
        myDatabindingIdeResource.monitorActivity(activity)


        // click  to navgation to saveReminder screen
        onView(withId(R.id.addReminderFAB)).perform(click())
        // Add title to saveReminder to display in list
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("newReminder"),
            closeSoftKeyboard()
        )
        // Add description to saveReminder to display in list
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("new"),
            closeSoftKeyboard()
        )

        // click  to navgation to select Location screen and add location
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        //check the data is displayed
        onView(withId(R.id.noDataTextView)).check(matches( not(isDisplayed())))

        //check is toast is displayed reminder saved
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(activityRule(activity).window.decorView)))).check(
            matches(isDisplayed())
        )
        //make sure activity is close after finished test
        activity.close()

    }


    private fun activityRule(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }





}

