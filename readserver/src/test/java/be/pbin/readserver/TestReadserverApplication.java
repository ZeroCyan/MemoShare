package be.pbin.readserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestReadserverApplication {

	public static void main(String[] args) {
		SpringApplication.from(ReadserverApplication::main).with(TestReadserverApplication.class).run(args);
	}

}
