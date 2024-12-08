package study.querydsl.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Hello {
    @Id @GeneratedValue
    private Long id;
}
//queryDsl은 Q타입을 만들어서 그걸로 동작하는 데
//gradle의 task의 other내부에 컴파일 쿼리 dsl을 눌러주면 됬는 데 이제는
//Gradle의 최신 버전과 QueryDSL의 발전으로 인해, compileQuerydsl Task는 더 이상 필요하지 않으며,
// Java 컴파일 작업 (compileJava)이 자동으로 QueryDSL 애노테이션 프로세서를 실행하여
// 필요한 Q 클래스를 생성합니다. 따라서,
// Gradle tasks 리스트의 other 섹션에서 compileQuerydsl을 찾을 수 없는 것은 정상이며,
// 그 작업은 compileJava에 통합되었다고 이해하시면 됩니다.
//이후 컴파일 자바를 하면 generated내부에 Q쿼리 엔티티를 만드는데
//이게 바로 엔티티를 보고 querydsl이 Q쿼리를 만들어주는 것이다.
//빌드할 때 자동으로 이런 파일이 생긴다.
//이렇게 정상적으로 생기는 지 확인하는 게 중요하다.
//또한 인텔리제이는 모듈에 자동으로 등록하도록 해준다.
//아니면 프로젝트에서 ./grewdle clean을 하면 빌드 폴더가 지워지고
//./grewdle compileJava 명령를 해도 빌드를 해준다.
//하지만 생성된 q파일들 시스템이 생성해주는 파일들은 깃에 올리면 안된다.
//또한 빌드 폴더는 기본적으로 이그놀 해놓기 때문에 깃에서 자동으로 예외처리가 된다.
//src에 생성하게 되면 깃으로 별도 막아줘야 한다.

