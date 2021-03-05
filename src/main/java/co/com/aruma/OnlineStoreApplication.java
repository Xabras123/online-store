package co.com.aruma;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import lombok.extern.slf4j.Slf4j;



@Slf4j
@SpringBootApplication
public class OnlineStoreApplication implements ApplicationListener<ContextRefreshedEvent>{

	public static void main(String[] args) {
		SpringApplication.run(OnlineStoreApplication.class, args);
	}
	
	@Value("${spring.application.name}") String name;	
	@Value("${spring.application.version}") String version;
	@Value("${spring.application.restPort}") String restPort;
	
	
	
	  @Override public void onApplicationEvent(ContextRefreshedEvent event) { try {
	  String LINE = "------------------------------------------------"; //
	  //Evidenciar en el LOG el inicio correcto de los servicios log.info(LINE);
	  log.info("{} ~Application started", name); 
	  log.info("~Port: {}", restPort);
	  log.info("~Version: {}", version); 
	  log.info("~Launched [OK]"); log.info(LINE);
	  
	  } catch (Exception e) { log.error(e.getMessage(), e); } }
	
}