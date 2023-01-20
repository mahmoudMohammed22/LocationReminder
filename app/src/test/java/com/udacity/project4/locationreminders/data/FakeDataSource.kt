package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.Result.Success

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var item : MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

//    complete TODO: Create a fake data source to act as a double to the real data source

    // this value use to check from fail load data in viewModel
    var snackbarReturnError = false

    //this funcrion is update Based on the success of downloading the data or not
    fun showSnackbarReturnError(value:Boolean){
        snackbarReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (snackbarReturnError){
            return Result.Error("Error in add data to  reminders")
        }
        // display insert data if success else return message error
        item?.let { return Success(ArrayList(it)) }
        return Success(emptyList())

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        // add item from reminderDto in item List
        item?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        // get data from id
        val itemId = item?.find {
            it.id == id
        }


    // if item is not null result return success and return data By id
        return when{
            snackbarReturnError ->{
                Result.Error("Error add item to list")
            }
            itemId != null ->{
                Result.Success(itemId)
            }
            else->{
                Result.Error("Reminder not found!")
            }

            // else iten is null result return  message error

        }


    }

    override suspend fun deleteAllReminders() {
        // delete all item from database
        item?.clear()

    }




}