package study.querydsl.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Commit //Commit어노테이션을 통해 실제 DB에 Commit이 되서 실제 데이터가 저장되는 지 확인할 수 있다.
    //하지만 Commit을 하면 다른 테스트와 꼬일 수 있기 때문에 다음 테스트에도 데이터가 남아있어서 다음 테스트가 깨질 수 있다.
class MemberTest {
    @Autowired
    EntityManager em;

    @Test
    public void testEntity(){
        //given
       Team teamA=new Team("TeamA");
       Team teamB=new Team("TeamB");
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

        //초기화
        em.flush();//영속성 오브젝트를 쿼리로 날림
        em.clear();//영속성 컨텍스트 캐시 초기화
        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("->member.team = " + member.getTeam());
        }
        //assert대신 확인
        //공부할때만 soutv를 사용
        //member와 Team은 객체에서는 참조 값으로 이동하지만 DB에서는
        //외례키 FK로 연관관계를 맺게 된다.
    }
}