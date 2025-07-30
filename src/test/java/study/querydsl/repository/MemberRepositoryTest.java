package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
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

    /*페이징 객체 테스트*/
    @Test
    public void searchPageSimple(){
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
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition,pageRequest);
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1","member2","member3");
    }
    /**
     * 쿼리를 보면
     * 카운트쿼리1,Limit쿼리가 나가는 것을 확인할 수 있다.
     * 또한 검증 데이터3개와 실제 조회 타겟인 1,2,3이 조회된 것을 볼 수 있다.
     * */

    @Test
    public void querydslPredicateExcutorTest(){
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

        QMember member = QMember.member;
        Iterable<Member> result = memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")));
        /*이처럼 바로 구현체 안에서 조건을 넣을 수 있지만
        * 실무에서 사용 여부 판단을 해보면
        * join이 상당히 중요한데 left join이 불가능하다.
        * 적재적소 join이 불가능하며 클라이언트 코드가
        * querydsl에게 의존하게 된다.
        * 복잡한 실무환경에서 사용하기에는 한계가 명확하다.
        * repo를 만드는 이유는 붙여야 되는 기술을 숨기는 느낌인데
        * querydsl을 전환하기 힘들어진다. 너무 많은 의존관계가
        * 엮인다.
        * */
        for (Member findMember :result){
            System.out.println("findMember = " + findMember);
        }
    }

    /**
     * Querydsl web 서포트
     * 컨트롤러 레벨에서 @QuerydslPredicate로 루트 엔티티를 넣어주면
     * 바인딩을 조건으로 받아준다.
     * 파라미터 바인딩이 쿼리dsl의 조건식에 자동 매핑을 하여
     * 조건을 만들어서 파라미터바인딩 및 인젝션을 해준다.
     * 그럼 이걸 바로 where절에 던져줄 수 있다.
     * 그러면 코드 한줄로 간편하게 findAll을 바로 작업할 수 있다.
     * left조인이 안되고 어렵기 때문에 한계가 명확한게 아쉽다.
     * 그리고 eq이나 like컨테인이 잘안된다.
     * 그리고 리포에 바인드 커스텀을 만들어서 넣어줘야되고
     * 복잡한 커스터마이징이 필요하다.
     * 단순 조건만 가능하다.
     * 조건을 커스터마이징하는 기능이 복잡하고 명시적이지 않는다.
     * 기능이 단순한데 구현 자체는 복잡하기 떄문에 추천하지않는다.
     * 그리고 controller가 querydsl에 의존해야된다.
     * 또한 복잡한 실무환경에서 사용하기에는 한계가 명확하다.
     * */


}