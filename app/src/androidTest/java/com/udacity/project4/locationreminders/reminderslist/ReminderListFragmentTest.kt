package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
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
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest () {

    // Execute each task concurrently using architecture components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var reminderData: ReminderDataSource
    private lateinit var appContext: Application


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
        reminderData = get()

        //clear the data to start fresh
        runBlocking {
            reminderData.deleteAllReminders()
        }
    }

//    complete TODO: test the navigation of the fragments.
//    complete TODO: test the displayed data on the UI.
//    complete TODO: add testing for the error messages.

    @Test
    fun clickInReminderFap_navigateToSaveReminder(){


        //Given - fragment to navgiate to another fragment
        val freagment = launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)



        // to controller in this fragment
        val navController = mock(NavController::class.java)


        // add navgation to fragment
        freagment.onFragment {
            Navigation.setViewNavController(it.view!!,navController)
        }

        // WHEN - Click on the ReminderFap +
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to save reminder screen

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }


    @Test
    fun reminderList_DisplayedInUi() = runBlockingTest{
        runBlocking {

            // Given list to add in databasr
            val data = ReminderDTO("NewReminder", "home", "point", 1.2, 1.3)

            // add list in add base
            reminderData.saveReminder(data)

            // WHEN - ReminderList Fragment launched to display data
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

            //Then - check data diplay in the screen
            onView(withId(R.id.reminderssRecyclerView)).perform(
                // scrollTo will fail the test if no item matches.
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(data.location))
                )
            )

            onView(withId(R.id.reminderssRecyclerView)).perform(
                // scrollTo will fail the test if no item matches.
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(data.title))
                )
            )
        }
    }

    @Test
    fun reminderList_DisplayedInuI_noDataFound() = runBlockingTest {
        runBlocking {
            // Given list to add in databasr
            val data = ReminderDTO("NewReminder", "home", "point", 1.2, 1.3)

            // add list in add base
            reminderData.saveReminder(data)
            // remove data from database
            reminderData.deleteAllReminders()

            // WHEN - ReminderList Fragment launched to display
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

            //Then - check data  inNot diplay in screen
            onView(withId(R.id.noDataTextView)).check(
                ViewAssertions.matches(
                    isDisplayed()
                )
            )

        }
    }



}