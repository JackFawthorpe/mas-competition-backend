package mascompetition.Email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * JavaMailSender sends emails with properties accessed from the properties file
 * Adapted from: https://www.baeldung.com/spring-email
 */
public class JavaMailSender {

    @Value("${spring.mail.host}")
    private String hostName;
    @Value("${spring.mail.username}")
    private String emailUsername;
    @Value("${spring.mail.password}")
    private String emailPassword;

    @Value("${spring.mail.port}")
    private int emailPort;

    @Value("${spring.mail.transport.protocol}")
    private String emailProtocol;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean enableAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean startTls;

    @Value("${spring.mail.debug}")
    private boolean enableDebug;

    /**
     * Instantiates the Mail sender for the application and adds in the credentials
     * and settings from the properties file in use
     *
     * @return The mail sender
     */
    @Bean
    public org.springframework.mail.javamail.JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(hostName);
        mailSender.setPort(emailPort);

        mailSender.setUsername(emailUsername);
        mailSender.setPassword(emailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", emailProtocol);
        props.put("mail.smtp.auth", enableAuth);
        props.put("mail.smtp.starttls.enable", startTls);
        props.put("mail.debug", enableDebug);

        return mailSender;
    }
}
