package study.querydsl;


import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
        //이렇게 필드 레벨로 가져가도 괜찮다.
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

    }
    @Test
    public void startJPQL(){
        //member1 탐색
        Member findMember = em.createQuery("select m from Member m" +
                        " where m.username=:username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl(){
//        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        //이렇게 기본적으로 JPAQueryFactory에 em을 줘야 하고
        //"m"은 별칭으로 나중에는 만들어져있는 이름을 사용.
        QMember m = new QMember("m");

        Member findMeber = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))//파라미터 바인딩
                .fetchOne();
        //여기서는 파라미터 바인딩을 해줄 필요가없다.
        //함수를 통해 프리페어스테이트먼트로 자동으로 매핑을 시켜준다.
        //이래서 SQL인젝션같은 공격을 방지 할 수 있다.
        //또한 오류 시점또한 JPQL은 문자열이라 실제 돌려봐야 확인이 되지만
        //런타임 오류를 통해 확인해야 된다.
        //QueryDsl을 사용하면 컴파일타임에 오류가 잡혀서 훨씬 간편하게 확인할 수 있다.

        Assertions.assertThat(findMeber.getUsername()).isEqualTo("member1");

    }


}

















