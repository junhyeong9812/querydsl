package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

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

    /*동적 쿼리 Builder쿼리
    * */
    @Test
    public void searchTest(){
        Team teamA=new Team("teamA");
        Team teamB=new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1=new Member("member1",10,teamA);
        Member member2=new Member("member2",20,teamA);
        Member member3=new Member("member3",30,teamB);
        Member member4=new Member("member4",40,teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
//        condition.setAgeGoe(35);
//        condition.setAgeLoe(40);
        condition.setTeamname("teamB");

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
        assertThat(result).extracting("username").containsExactly("member3","member4");
    }
    /*jpql을 보면 팀 네임과 에이지 조건도 잘 들어오는걸 볼 수 있다.
    * 그리고 결과를 가져오는 것은 우리가 원하는 결과를 잘 가져온 것을
    * 볼 수 있다.
    * 이렇게 Goe와 Loe를 주석하면
    * 실무에서 만약 조건이 다 빠지게 되면
    * where절에 빌더가 조건이 없을 때 쿼리가 데이털를 전부 가져오도록 된다.
    * 개발할때는 문제가 없지만 몇천개의 데이터가 있으면 잘못하면
    * 전부 가져오게 되어
    * 기본조건 및 리미트 조건을 걸어놔야한다.
    * 그래서 페이징 쿼리가 필수다.*/

    @Test
    public void searchWhereTest(){
        Team teamA=new Team("teamA");
        Team teamB=new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1=new Member("member1",10,teamA);
        Member member2=new Member("member2",20,teamA);
        Member member3=new Member("member3",30,teamB);
        Member member4=new Member("member4",40,teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(40);
        condition.setTeamname("teamB");

        List<MemberTeamDto> result = memberJpaRepository.search(condition);
        assertThat(result).extracting("username").containsExactly("member3","member4");
    }

    /*where절 같은 경우 한번에 쿼리를 알 수 있으며
    * 가끔은 빌더를 사용해야될 때가 있지만 비교적 where절이 좋다.*/





}