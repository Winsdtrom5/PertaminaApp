//package com.example.pertaminaapp.session
//import android.content.Context
//import android.util.Log
//import java.util.*
//
//class EmailNotifier(private val context: Context) {
//
//    fun checkForNewEmails(email:String) {
//        // Initialize your email properties (e.g., IMAP server, username, password)
//        val username = "your-office-email@example.com"
//        val password = "your-office-email-password"
//        // Determine the email provider (e.g., "gmail.com" or "outlook.com")
//        val emailProvider = username.substringAfterLast('@')
//
//// Initialize the host based on the email provider
//        val host: String = when (emailProvider) {
//            "gmail.com" -> "imap.gmail.com" // IMAP server for Gmail
//            "outlook.com" -> "outlook.office365.com" // IMAP server for Outlook/Office 365
//            else -> "imap.your-email-provider.com" // Default to your-email-provider.com
//        }
//        val port = 993 // IMAPS port
//
//        val properties = Properties()
//        properties.setProperty("mail.store.protocol", "imaps")
//        properties.setProperty("mail.imaps.host", host)
//        properties.setProperty("mail.imaps.port", port.toString())
//        properties.setProperty("mail.imaps.auth", "true")
//
//        val session = Session.getDefaultInstance(properties)
//
//        try {
//            // Connect to the email server
//            val store = session.getStore("imaps")
//            store.connect(host, username, password)
//
//            // Open the SENT folder
//            val sentFolder = store.getFolder("SENT")
//            sentFolder.open(Folder.READ_ONLY)
//
//            // Get messages in the SENT folder
//            val messages = sentFolder.messages
//
//            // Process messages
//            for (message in messages) {
//                // Check if the message is not set as SEEN (unread)
//                if (!message.isSet(Flags.Flag.SEEN)) {
//                    // Get the recipients of the email
//                    val recipients = message.getRecipients(Message.RecipientType.TO)
//
//                    // Check if there are recipients
//                    if (recipients != null) {
//                        // Iterate through recipients
//                        for (recipient in recipients) {
//                            // Check if the recipient matches the user's email
//                            val recipientEmail = recipient.toString()
//
//                            if (recipientEmail.equals(email, ignoreCase = true)) {
//                                // TODO: Implement logic to show a notification
//                                Log.d("EmailNotifier", "New email from office to user: ${message.subject}")
//                                // Mark the message as SEEN to prevent duplicate notifications
//                                message.setFlag(Flags.Flag.SEEN, true)
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Close the SENT folder and store
//            sentFolder.close(false)
//            store.close()
//
//        } catch (e: MessagingException) {
//            e.printStackTrace()
//            Log.e("EmailNotifier", "Error checking for new emails: ${e.message}")
//        }
//    }
//}
