package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // complete TODO: provide testing to the RemindersListViewModel and its live data objects

    //instance data from reminderDto to add in fakeDataStore
    val data = listOf( ReminderDTO("newReminder","home","Point",1.2,2.2),
        ReminderDTO("newReminder2","home2","Point2",1.2,2.2))





    // Execute each task concurrently using architecture components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //instance from viewModel to test function in RemindersListViewModel
    private lateinit var testViewModelReminderList: RemindersListViewModel

    //instance from fakeDataStore to add in ReminderListViewModel paramaters
    private lateinit var testDataSource :FakeDataSource

    //instance from application to add in  context in ReminderListViewModel Parameters
    private lateinit var appContext: Application


    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutinuRule = MainCoroutineRule()


    @Before
    fun initParametersInReminderListViewModel(){
        // We initialise the dataStore with  tasks
        testDataSource = FakeDataSource(data.toMutableList())

        //get appContext for application
        appContext = ApplicationProvider.getApplicationContext()

        testViewModelReminderList = RemindersListViewModel(appContext,testDataSource)

    }

    // stop koin after finish test
    @After
    fun clean(){
        stopKoin()
    }


    //this function use to test val showLoading in viewModel and now actual phased function in function loadReminder
    @Test
    fun loadReminder_showLoadingValue(){

        // Pause dispatcher so we can verify initial values
        mainCoroutinuRule.pauseDispatcher()

        // load data in viewModel
        testViewModelReminderList.loadReminders()

        //Then progress is shown verify initial values
        assertThat(testViewModelReminderList.showLoading.getOrAwaitValue(), Matchers.`is`(true))

        // after take a verify initial values make resumeDispatcher
        mainCoroutinuRule.resumeDispatcher()

        //Then progress is shown final value
        assertThat(testViewModelReminderList.showLoading.getOrAwaitValue(), Matchers.`is`(false))

    }

    // this function is use to test reminderList and know this list have data or not
    @Test
    fun loadReminder_returnNotnullValue_andNotEmptyList() = runBlockingTest{

        //Give - data in viewModel
        testViewModelReminderList.loadReminders()

        // Then - progress show value of list is not null and not emptyList
         assertThat(testViewModelReminderList.remindersList.getOrAwaitValue(), Matchers.not(emptyList()))
         assertThat(testViewModelReminderList.remindersList.getOrAwaitValue(), Matchers.notNullValue())


    }

    //this function is use to returnErrorMessage When loadReminder is fail download data in viewModel
    @Test
    fun loadReminder_snackbarMessageErorr(){

        //make dataStore return error
        testDataSource.showSnackbarReturnError(true)

        // when - load data into viewModel
        testViewModelReminderList.loadReminders()

        // then - an error message return
        assertThat(testViewModelReminderList.showSnackBar.getOrAwaitValue(),Matchers.`is`("Error in add data to  reminders"))
    }

    @Test
    fun loadReminder_noDataDisplayed(){

        // Pause dispatcher so we can verify initial values
        mainCoroutinuRule.pauseDispatcher()

        testDataSource.showSnackbarReturnError(true)


        // load data in viewModel
        testViewModelReminderList.loadReminders()

        //Then progress is shown verify initial values
        assertThat(testViewModelReminderList.showLoading.getOrAwaitValue(), Matchers.`is`(true))

        // after take a verify initial values make resumeDispatcher
        mainCoroutinuRule.resumeDispatcher()

        //Then progress is shown final value
        assertThat(testViewModelReminderList.showLoading.getOrAwaitValue(), Matchers.`is`(false))

    }









}