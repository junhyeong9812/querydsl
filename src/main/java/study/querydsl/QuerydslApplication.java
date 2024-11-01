package study.querydsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslApplication.class, args);
	}
	//기본적으로 build and running을 gradle이 아닌 intelij로 변경
	//gradle설정에서 변경 가능
	//lombok설정 및 Build>Compiler>Annotation Processors >Enable annotation processing 설정


}
