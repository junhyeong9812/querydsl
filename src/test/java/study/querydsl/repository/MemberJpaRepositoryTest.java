package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest(){
        Member member = new Member("member1",10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);
        //같은 트랜젝션이라 같은 객체 주소가 나와야한다.

        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);

    }

    @Test
    public void basicQuerydslTest(){
        Member member = new Member("member1",10);
        memberJpaRepository.save(member);

        List<Member> result1 = memberJpaRepository.findAll_Querydsl();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUsername_Querydsl("member1");
        assertThat(result2).containsExactly(member);

    }
    /*현재 쿼리도 예측한대로 깔끔하게 JPQL로 나가는걸 볼 수 있다.
    * 이떄 장점으로는 JPQL은 문자열로 짜기 때문에 오타가 나도
    * IDE가 없으면 알 수가 없다.
    * 실제 실행 레벨에서 오류가 발생하는데 컴파일레벨에서 오류가
    * 나도록 해준다.
    * 그리고 set파라미터도 조건이 파라미터 바인딩을 해주기 때문에
    * 신경 쓸 일이 없다.
    * */


}