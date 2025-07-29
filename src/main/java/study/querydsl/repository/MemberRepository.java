package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.Repository;
import study.querydsl.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long>,MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {
    List<Member> findByUsername(String username);
    //함수 이름으로 매칭을 시켜서
    //select m from Member m where m.username= ?
    //이렇게 자동으로 이름 매핑 로직이 이미 jpa에서 구현되어있다.

    /** 스프링 데이터 JPA가 제공하는 QueryDsl 기능
     *  제약이 커서 실무에서 활용하기 힘든 기술들
     *  테이블이 1개일 경우에는 추상화해서 많은걸 할 수 있지만
     *  테이블이 많고 조인이 들어가면 제대로 동작하지 않는 경우가
     *  많다.(아직은 모름)
     *  1. 인터페이스 지원
     *  ->QuerydslPredicateExcutor
     *  기능 제공
     *
     *  */
}
