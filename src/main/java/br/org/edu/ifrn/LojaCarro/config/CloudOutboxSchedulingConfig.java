package br.org.edu.ifrn.LojaCarro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Profile("cloud")
@EnableScheduling
public class CloudOutboxSchedulingConfig {
}
