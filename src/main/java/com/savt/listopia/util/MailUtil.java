package com.savt.listopia.util;

import com.savt.listopia.util.mail.MailSender;

public class MailUtil {
    public static void sendMail(
            String receiverMail,
            String receiverName,
            String textPart,
            String htmlPart,
            String subject
    ) {
        MailSender.sendMailFromListopia(receiverMail, receiverName, textPart, htmlPart, subject);
    }
}
