package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//   complete TODO: Add testing implementation to the RemindersDao.kt

    // Execute each task concurrently using architecture components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //instane from database
    private lateinit var database: RemindersDatabase


    @Before
    fun initData(){
        // using in inMemoryDatabase because data stored here delete when finished  test
        database  = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    //to close database when finished test
    fun closeData()=database.close()


    //Test fun saveReminder in database and getReminderById
    @Test
    fun saveReminderAndgetReminderById() = runBlockingTest {
        // Given data to insert in database
        val data = ReminderDTO("newReminder","home","Point",1.2,2.2)
        //call saveReminder to insert Item in database
        database.reminderDao().saveReminder(data)

        //When - get data by id from database
        val load = database.reminderDao().getReminderById(data.id)

        // THEN - The loaded data contains the expected values.
        assertThat(load, Matchers.notNullValue())
        assertThat(load?.id, `is`(data.id))
        assertThat(load?.title, `is`(data.title))
        assertThat(load?.description, `is`(data.description))
        assertThat(load?.latitude, `is`(data.latitude))
        assertThat(load?.longitude, `is`(data.longitude))
        assertThat(load?.location, `is`(data.location))
    }


    //Test fun saveReminder in database and getAllReminderFromData
    @Test
    fun saveReminderAndgetReminde() = runBlockingTest {
        // Given data to insert in database
        val data1 = ReminderDTO("newReminder1","Reminder1","point",1.2,2.2)
        val data2 = ReminderDTO("newReminder2","Reminder2","point",1.2,2.2)

        //call saveReminder to insert Item in database
        database.reminderDao().saveReminder(data1)
        database.reminderDao().saveReminder(data2)


        //When - get allData from database
        val load = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(load,Matchers.notNullValue())
        assertThat(load.size, `is`(2))

    }

    //fun TestDeleteAllReminderFromData
    @Test
    fun saveReminderAndDeleteAllReminder() = runBlockingTest {

        // Given data to insert in database
        val data1 = ReminderDTO("newReminder1","Reminder1","point",1.2,2.2)
        val data2 = ReminderDTO("newReminder2","Reminder2","point",1.2,2.2)

        //call saveReminder to insert Item in database
        database.reminderDao().saveReminder(data1)
        database.reminderDao().saveReminder(data2)


        //call deleteAllReminders to Delete Item from database
        database.reminderDao().deleteAllReminders()

        //When - get allData from database
        val load = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(load, `is`(emptyList()))
        assertThat(load.size, `is`(0))

    }

}