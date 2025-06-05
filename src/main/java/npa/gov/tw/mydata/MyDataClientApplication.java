package npa.gov.tw.mydata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages= "npa.gov.tw.mydata")
public class MyDataClientApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MyDataClientApplication.class);
    }
    
	public static void main(String[] args) {
		SpringApplication.run(MyDataClientApplication.class, args);
	}

}
