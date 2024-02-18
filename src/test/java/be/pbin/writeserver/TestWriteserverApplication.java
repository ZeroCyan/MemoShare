package be.pbin.writeserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestWriteserverApplication {

	public static void main(String[] args) {
		SpringApplication.from(WriteserverApplication::main).with(TestWriteserverApplication.class).run(args);
	}

}
