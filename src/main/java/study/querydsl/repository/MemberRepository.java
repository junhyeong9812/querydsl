package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import study.querydsl.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long>,MemberRepositoryCustom {
    List<Member> findByUsername(String username);
    //함수 이름으로 매칭을 시켜서
    //select m from Member m where m.username= ?
    //이렇게 자동으로 이름 매핑 로직이 이미 jpa에서 구현되어있다.

}
