package mascompetition.unit.Email;

import mascompetition.Email.EmailService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    /**
     * Manual test to verify that the email service is able to send emails
     */
    @Disabled
    @Test
    void sendEmail_validDetails_SendsEmail() {
        // Manual test to check email service : Replace with your own / temporary email to check the service
        emailService.sendSimpleMessage("fawthorp878@gmail.com", "Test email", "This is a test email");
    }
}
