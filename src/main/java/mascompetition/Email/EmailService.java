package mascompetition.Email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails to users
 * Adapted from: <a href="https://www.baeldung.com/spring-email">...</a>
 * Note: The sending email and password are set through application-*.properties
 */
@Service
public class EmailService {

    private final String defaultEmail = "doNotReply@mascompetition.com";
    Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    MailProperties mailProperties;

    @Autowired
    TaskExecutor taskExecutor;

    @Autowired
    private JavaMailSender emailSender;

    /**
     * Sends a simple text email to the user
     *
     * @param to      The email of the user
     * @param subject The subject line of the email
     * @param text    The contents of the email
     */
    public void sendSimpleMessage(
            String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(defaultEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}