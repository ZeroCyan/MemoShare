package be.pbin.webserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestWebserverApplication {

	public static void main(String[] args) {
		SpringApplication.from(WebserverApplication::main).with(TestWebserverApplication.class).run(args);
	}

}
