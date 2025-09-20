package com.example.master;

//import com.example.master.Dto.testDto;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;


@EnableAsync
@SpringBootApplication
@EnableKafka
@EnableMethodSecurity
//@CrossOrigin(origins = "*")
public class MasterApplication {
//CommandLineRunner run () {
//    return args -> {
//        var testDto  = new testDto( 1, "sanket", 2002L);
//        System.out.println(testDto.id());
//    };
//}
	public static void main(String[] args) {
		SpringApplication.run(MasterApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> System.out.println("App started successfully!");
	}

	@Bean
	public WebClient keycloakWebClient(
			@Value("${keycloak.auth-server-url}")
			String baseUrl // e.g. http://localhost:8080
	) {
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(
						HttpClient.create()
				))
				.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}
}
