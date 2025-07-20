package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslApplication.class, args);
	}
	//기본적으로 build and running을 gradle이 아닌 intelij로 변경
	//gradle설정에서 변경 가능
	//lombok설정 및 Build>Compiler>Annotation Processors >Enable annotation processing 설정

	@Bean
	JPAQueryFactory jpaQueryFactory(EntityManager em){
		return new JPAQueryFactory(em);
	}
	/* 이렇게 스프링 빈으로 등록하여 미리 생성 후 메모리에 객체를
	* 올려놓으면 된다. */
}
