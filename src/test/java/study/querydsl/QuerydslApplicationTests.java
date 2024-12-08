package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Commit //Commit 어노테이션을 통해 롤백이 안되도록 할 수 있다.
class QuerydslApplicationTests {

	@Autowired
//	@PersistenceContext
	EntityManager em;


	@Test
	void contextLoads() {
		Hello hello =new Hello();
		em.persist(hello);

		JPAQueryFactory query= new JPAQueryFactory(em);
		//QueryDsl을 사용하기 위해서는 쿼리 팩토리로 만들어야 한다.
//		QHello qHello= new QHello("h");
		//이때 "h"는 별칭
		QHello qHello= QHello.hello;
		//이렇게 생성하여 만들어놓은 게 있으니 가져다 써도 된다.

		Hello result = query
				.selectFrom(qHello)
				.fetchOne();
		//이렇게 자바의 빌드형식으로 쿼리를 생성할 수 있다.
		//쿼리랑 관련된 정보는 전부 Q파일로 넣어야 된다.

		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
		//테스트에서 롤백을 통해 쿼리가 잘 안보이기 때문에 별도의 옵션이 필요하다.
		//이렇게 querydsl 설정이 잘 되었는 지 확인
	}

}
