package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //complete TODO: provide testing to the SaveReminderView and its live data objects


    // Execute each task concurrently using architecture components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //instance from viewModel to test function in RemindersListViewModel
    private lateinit var testSaveReminderViewModel: SaveReminderViewModel

    //instance from fakeDataStore to add in ReminderListViewModel paramaters
    private lateinit var testDataSource : FakeDataSource

    //instance from application to add in  context in ReminderListViewModel Parameters
    private lateinit var appContext: Application


    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutinuRule = MainCoroutineRule()

    @Before
    fun initParametersInReminderListViewModel(){
        // We initialise the dataStore with  tasks
        testDataSource = FakeDataSource()

        //get appContext for application
        appContext = ApplicationProvider.getApplicationContext()

        testSaveReminderViewModel = SaveReminderViewModel(appContext,testDataSource)

    }

    // stop koin after finish test
    @After
    fun clean(){
        stopKoin()
    }

    @Test
    fun saveReminder_showLoding_ReturnValueBoolean() {

        //Given- Data to add in saveReminder
        val data =  ReminderDataItem("newReminder2","home2","Point2",1.2,2.2)

        // Pause dispatcher so we can verify initial values
        mainCoroutinuRule.pauseDispatcher()

        // load data in viewModel
        testSaveReminderViewModel.saveReminder(data)

        //Then progress is shown verify initial values
    assertThat(
            testSaveReminderViewModel.showLoading.getOrAwaitValue(),
           `is`(true)
        )

        // after take a verify initial values make resumeDispatcher
        mainCoroutinuRule.resumeDispatcher()

        //Then progress is shown final value
        assertThat(
            testSaveReminderViewModel.showLoading.getOrAwaitValue(),
            `is`(false)
        )

    }


    @Test
    fun saveReminder_ShowToast_returnMessageSaved() {

        //Given data to add in database
        val data =  ReminderDataItem("newReminder2","home2","Point2",1.2,2.2)

        // load data in viewModel
        testSaveReminderViewModel.saveReminder(data)

        //Then progress is shown verify initial values
        assertThat(
            testSaveReminderViewModel.showToast.getOrAwaitValue(),
            `is`("Reminder Saved !")
        )
    }

    @Test
    fun validateEnteredData_titleAndLocationIsNotEmpty_retuenBooleanValue(){
        //Given data to add in database and add in validateEnteredData
        val data =  ReminderDataItem("newReminder2","home2","Point2",1.2,2.2)

        //Then progress is shown return value from validateEnteredData when have data
        assertThat( testSaveReminderViewModel.validateEnteredData(data), `is`(true))


    }

    @Test
    fun validateEnteredData_titleIsEmpty_retuenBooleanAndMessageSnakeBar(){

        //Given data to add in database and add in validateEnteredData
        // and given title is empty
        val data =  ReminderDataItem("","new","home2",1.2,2.2)


        //Then progress is shown return value from validateEnteredData when have data
        // but title is empty return message and Boolean value
        assertThat(testSaveReminderViewModel.validateEnteredData(data), `is`(false))
        assertThat(testSaveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))


    }














}