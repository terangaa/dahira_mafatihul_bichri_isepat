
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

import org.mockito.Mockito;

/**
 * Configuration réservée aux tests.
 *
 * Fournit un faux JavaMailSender pour éviter
 * les dépendances à un serveur SMTP pendant les tests.
 */
@Configuration(proxyBeanMethods = false)
public class TestMailConfiguration {

    @Bean
    @Primary
    public JavaMailSender testJavaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }
}
