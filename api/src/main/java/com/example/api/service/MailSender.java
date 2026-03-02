package com.example.api.service;

public interface MailSender {
    void sendSimpleMail(String to, String subject, String content);
}
