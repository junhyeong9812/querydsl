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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired MemberRepository memberRepository;

    @Test
    public void basicTest(){
        Member member = new Member("member1",10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);
        //같은 트랜젝션이라 같은 객체 주소가 나와야한다.

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
        //JPA인터페이스에서 기본적인걸 다 제공해준다.
        //하지만 이러헥 하면 QueryDsl을 쓰는게 문제가 된다.
        //하지만 이건 기본적으로 동적쿼리를 만들기 힘들다.
    }

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
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamname("teamB");

        List<MemberTeamDto> result = memberRepository.search(condition);
        assertThat(result).extracting("username").containsExactly("member3","member4");
    }

}