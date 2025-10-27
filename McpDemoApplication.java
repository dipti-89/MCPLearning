package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication(exclude = {WebMvcAutoConfiguration.class})
public class McpDemoApplication {

	public static void main(String[] args) {
		System.setProperty("spring.main.banner-mode", "off");
		System.setProperty("logging.level.root", "ERROR");
		SpringApplication app = new SpringApplication(McpDemoApplication.class);
		app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
		app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
		app.run(args);
	}

    @Bean
    public CommandLineRunner runner(MCPRequestHandler handler, Environment env) {
        return args -> {
            // Only start MCP handler if running in MCP mode (default behavior)
            handler.start();
        };
    }
}
