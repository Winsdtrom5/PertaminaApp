package com.example.pertaminaapp.session

import android.util.Log
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class EmailSender() {
    fun sendEmail(recipientEmail: String, subject: String, message: String){
        val username = "anggagant@gmail.com"
        val password = "fwnl dkky skyq ljkb"

        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com" // Use your email provider's SMTP server
        props["mail.smtp.port"] = "587"            // Use the appropriate port

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val emailMessage = MimeMessage(session)
            emailMessage.setFrom(InternetAddress(username))
            emailMessage.setRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
            emailMessage.subject = subject
            emailMessage.setText(message)

            Transport.send(emailMessage)
        } catch (e: MessagingException) {
            e.message?.let { Log.d("Ambasing3", it) }
            throw RuntimeException(e)
        }
    }
}
