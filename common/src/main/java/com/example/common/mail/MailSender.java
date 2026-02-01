package com.example.common.mail;

public interface MailSender {
    void sendSimpleMail(String to, String subject, String content);
}
