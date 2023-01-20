package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.error
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//   complete TODO: Add testing implementation to the RemindersLocalRepository.kt

    // Execute each task concurrently using architecture components.
    @get:Rule
    var instanceExecutorRule = InstantTaskExecutorRule()


    private lateinit var testLocalDataRepository: RemindersLocalRepository

    private lateinit var database: RemindersDatabase


    @Before
    fun InitRepositry(){

        // using in inMemoryDatabase because data stored here delete when finished  test
        database  = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        //instance from RemindersLocalRepository to get reminderDao and mainThred
        testLocalDataRepository = RemindersLocalRepository(
            database.reminderDao(),Dispatchers.Main
        )
    }

    @After
    fun close () = database.close()


    //Test result of savedReminder data in database and get about id
    @Test
    fun saveReminderTask_retrievesTaskbyID() = runBlocking{

        //Given- new data
        val data = ReminderDTO("newReminder","home","Point",1.2,2.2)

        // save data in database
      testLocalDataRepository.saveReminder(data)

        // When - task retrieves by id
        val load = testLocalDataRepository.getReminder(data.id)


        // Then - retrievesTask by ID
        assertThat(load.succeeded , `is`(true) )
        load as Result.Success
        assertThat(load.data.id, `is`(data.id))
        assertThat(load.data.title , `is`(data.title))
        assertThat(load.data.description , `is`(data.description))
        assertThat(load.data.location , `is`(data.location))

    }

    //Test result of getReminderByID when not saved data to return error
    @Test
    fun getReminderByID_ReturnError() = runBlocking{

        //Given- new data
        val data = ReminderDTO("newReminder","home","Point",1.2,2.2)

        // When - task retrieves by id
        val reminder = testLocalDataRepository.getReminder(data.id)

        //Then - return erorr Because dont find id
        assertThat(reminder.error , `is`(false) )
        reminder as Result.Error
        //Then - message from return erorr
        assertThat(reminder.message, `is`("Reminder not found!"))

    }


    //Test result of DeleteAllReminder From database
    @Test
    fun DeleteAllReminder_empityList() = runBlocking{

        //Given- new data
        val data1 = ReminderDTO("newReminder1","Reminder1","point",1.2,2.2)
        val data2 = ReminderDTO("newReminder2","Reminder2","point",1.2,2.2)

        // saved in database
        testLocalDataRepository.saveReminder(data1)
        testLocalDataRepository.saveReminder(data2)

        //When delete all data from database
      testLocalDataRepository.deleteAllReminders()

        // Then - return emptyList
        val load = testLocalDataRepository.getReminders()

        // Then - return succeeded if data is empty
        assertThat(load.succeeded , `is`(true) )
        load as Result.Success

        assertThat(load.data, `is`(emptyList()))

    }







}