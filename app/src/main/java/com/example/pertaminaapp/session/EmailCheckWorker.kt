//package com.example.pertaminaapp.session
//
//import android.content.Context
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//class EmailCheckWorker(context: Context, workerParams: WorkerParameters) :
//    CoroutineWorker(context, workerParams) {
//
//    override suspend fun doWork(): Result {
//        val email = inputData.getString("email")
//        return withContext(Dispatchers.IO) {
//            try {
//                // Call your email checking function here
//                val emailNotifier = EmailNotifier(applicationContext)
//                if (email != null) {
//                    emailNotifier.checkForNewEmails(email)
//                }
//                Result.success()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Result.failure()
//            }
//        }
//    }
//}
//
