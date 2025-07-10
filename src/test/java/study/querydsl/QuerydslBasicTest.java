package study.querydsl;


import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

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
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl(){
//        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        //이렇게 기본적으로 JPAQueryFactory에 em을 줘야 하고
        //"m"은 별칭으로 나중에는 만들어져있는 이름을 사용.
        QMember m = new QMember("m");
        //위처럼 별칭을 이용한 방식이 존재하며
//        QMember member=QMember.member;
        //이렇게 생성된 맴버 정보도 존재한다.
        //이때 QMember에 대해서 스태틱 임포트로
        Member findMeberStatic = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))//파라미터 바인딩
                .fetchOne();
        //위처럼 스태틱 임포트를 사용하면 import Static을 통해 바로 호출할 수 있다.
        //이렇게 스태틱 임포트로 사용하는 것을 권장한다.
        //결국 QueryDsl로 만드는건 JPQL이 되는 것인데 이때 jpql이 궁금할 경우
        //use_sql_comments:true 옵션을 통해 ymi를 적용하면 jpql을 주석으로 나오는 것을 확인할 수 있다.
        //member1이라고 나간 이유는 기본적으로 자동Q쿼리에서 member1로 생성하기 때문이다.
        //하지만 위처럼 별칭을 통한 생성을 하면 jpql의 별칭이 m1으로 변경된다.
        //같은 테이블을 조인해야 되는 경우에는 위처럼 별칭 선언을 해서 사용하면 된다.

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

        assertThat(findMeber.getUsername()).isEqualTo("member1");

    }

    //검색 조건 쿼리
    @Test
    public void search(){
        Member member1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        assertThat(member1.getUsername()).isEqualTo("member1");
    }
    //JPQL이 제공하는 모든 검색 조건 제공
    /*
    .eq("member1") 앞의 값과 문자열이 일치
    .ne("member1") 앞의 값과 문자열의 값이 일치하지 않을 경우
    .eq("member1").not() 앞의 값과 문자열의 값이 일치하지 않을 경우
    .isNotNull() 널이 아닌 값
    .in(x,y) x,y를 포함하고 있는 데이터
    notIn(x,y) x,y를 포함하고 있지 않는 데이터
    between(10,30) 10에서 30사이의 데이터

    goe(x) x보다 크거나 같다.
    gt(x) x보다 크다
    loe(x) x보다 작거나 같다
    lt(x) x보다 작다

    .like("x%") like검색
    .contains("x") like %x%검색
    .startsWith("x") like x%검색

     */

    @Test
    public void searchAndParam(){
        Member member1 = queryFactory
                .selectFrom(member)
                .where(
//                        member.username.eq("member1")
//                        .and(member.age.between(10, 30))
                        member.username.eq("member1")
                                ,(member.age.between(10, 30))
                        //이처럼 위처럼 and를 통해 연쇄체이닝으로 가져가는 경우도 있지만
                        //,를 통해 and가 생략되어 where절에 넣는 것도 가능하다.
                        //그리고 ,로 처리하면 null인 경우 무시하기 때문에 동적쿼리를 만들 때 상당히 유효하다.
                )
                .fetchOne();
        assertThat(member1.getUsername()).isEqualTo("mebere1");
    }

    /*Querydsl의 결과조회
    fetch() :리스트 조회, 데이터 없으면 빈 리스트를 반환
    fetchOne() : 단건 조회
    -결과가 없으면 :null
    -결과가 둘 이상이면 "NonUniqueResultException"
    fetchFirst() : limit(1).fetchOne()
    fetchResult() : 페이징 정보 포함, total count 쿼리 추가 실행
    fetchCount() :count 쿼리로 변경해서 count 수 조회
     */
    @Test
    public void resultFetch(){
//        //리스트 조회
//        List<Member> fetch = queryFactory
//                .selectFrom(member)
//                .fetch();
//        //단일 조회
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
//        //처음 값 조회
//        Member fetchFirst = queryFactory
//                .selectFrom(member)
////                .limit(1)
////                .fetchOne();
//                .fetchFirst();


        QueryResults<Member> results = queryFactory
                        .selectFrom(member)
                .fetchResults();
        results.getTotal();
        List<Member> content = results.getResults();
        results.getLimit();
        //이렇게 하면 쿼리가 두번 실행되는데 토탈카운트때문에 쿼리가 두번나가서 카운트쿼리까지 나간다.
        //토탈이 있어야 총 페이지 수를 알려줄 수 있기 때문에 필요.

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();
        //이렇게 카운트 쿼리로 바꿔서 가져오도록 할 수 있다.
        //복잡한 쿼리에서는 성능때문에 토탈 및 리저트를 나누기 위해서 쿼리를 따로 날려야한다.


    }

    //회원 정렬 순서
//    1. 회원 나이 내림차순(desc)
//    2.회원 이름 올림차순(asc)
//    단 2에서 회원 이름이 없으면 마지막에 출력 (nulls last)
    @Test
    public void sort(){
        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 =result.get(0);
        Member member6 =result.get(1);
        Member membernull =result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(membernull.getUsername()).isNull();

    }

    @Test
    public void paging1(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
        Member member3 =result.get(0);
        assertThat(member3.getUsername()).isEqualTo("member3");
        //페이징에서도 2 offset 1로 들어가는 것을 볼 수 있다.

    }

    @Test
    public void paging2(){
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
     assertThat(queryResults.getTotal()).isEqualTo(4);
     assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }
    //실무에서는 카운트 쿼리를 분리해야 될 경우도 있기 때문에
    //페이징이 단순하지 않을 경우 페이징을 할 때 단순하게 할 수 있을 때 페이징 처리를 단순하게 해야 된다.
    //count쿼리에 다 붙기 때문에 성능상 안좋을 수도 있어서 이 부분을 고려해서 작성해야된다.

    @Test
    public void aggrefation(){
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();
        //튜플은 쿼리 DSL의 튜플로 여러개의 값을 꺼내야할 경우 튜플로 나온다.
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        //튜플로 조회한느 이유는 단일타입이 아니라 여러타입이 들어와서 튜플로 하거나
        //실무에서는 DTO로 값을 뽑아오도록 한다.
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구하라
     */
    @Test
    public void group() throws Exception{
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("TeamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("TeamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }//having 조건도 가능
    //item 가격으로 gt함수를 사용하여 해빙절도 가능하다.

    /**
     * 팀 A에 소속된 모든 회원(Join문법)
     */
    @Test
    public void join(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                //left/right/enner join 전부 가능
                .where(team.name.eq("TeamA"))
                .fetch();
        assertThat(result)
                .extracting("username")
                .containsExactly("member1","member2");
        //결국 JPQL에 의거하여 생성되는 것
    }
    //연관관계가 없어도 thetaJoin이 가능하다.
    //연관관계가 없어도 조인이 가능하다.
    @Test
    public void theta_join(){
        em.persist(new Member("TeamA"));
        em.persist(new Member("TeamB"));
        em.persist(new Member("TeamC"));

        //사람이름이 TeamA와 TeamB일 경우
        List<Member> result = queryFactory
                .select(member)
                .from(member, team) //
                .where(member.username.eq(team.name))
                .fetch();
        //모든 회원과 팀을 가져와서 일일히 where절에서 필터링을 하는 것
        //이걸 theta Join이라 한다.
        assertThat(result)
                .extracting("username")
                .containsExactly("TeamA","TeamB");
    }
    //이런 theta조인은 1가지 제약이 있는데 외부 조인 불가능
    //left/outter조인 같이 외부 조인 불가능
    //조인 on을 사용하면 외부 조인까지도 가능하다.
    //이런 select SQL을 보면 Cross Join까지 해준다.

    //하이버네이트 최신버전에서는 아래와 같은 방언이 생김
    //연관관계 없는 외부조인을 가져올 수 있는 방안
    @Test
    public void _join(){

    }


    //조인 - on절
    //1.조인 대상 필터링
//    2.연관관계 없는 엔티티 외부조인
//    2번이 많이 사용됨

    /*
    * 예) 회원과 팀을 조인하면서,팀이름이 teamA인 팀만 조인,회원은 모두 조인
    * JPQL: select m,t from Member m left join m.team t on t.name= 'teamA'
    * */
    @Test
    public void join_on_filtering(){
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("TeamA"))
//                만약 조인을 바꾸면 team이 없는 맴버는 다 빠지게 된다.
//                팀B를 가진 맴버자체는 조회되지 않는다.
                .fetch();

        for(Tuple tuple:result){
            System.out.println("tuple ="+tuple);
        }
        /*tuple =[Member{id=1, username='member1', age=10}, Team(id=1)]
tuple =[Member{id=2, username='member2', age=20}, Team(id=1)]
tuple =[Member{id=3, username='member3', age=30}, null]
tuple =[Member{id=4, username='member4', age=40}, null]*/
        /*팀A는 전부 가져왔지만
        * 팀B는 전부 안가져오게 된다.
        * 레프트 조인이기 때문에 맴버는 전부 가져오지만
        * 팀은 일치되는 값만 가져오게 된다.
        *  여기서 with 조건으로 쿼리가 나가고
        * 이후 on절로 조인하여 맴버와 팀을 조인하여
        * 엔드연산으로 팀의 이름으로 가져오는 쿼리를 만든다.*/
    }
    /*온 절을 활용하여 조인대상을 외부조인이 아닌 내부조인을 하면
    * where절 필터링 데이터와 같다.
    * where에서 팀a만 가져오지만
    * 이너조인이면 온절로 거를 이유가 없다. 왜냐면
    * 이너조인이면 결국 둘 다 있는 값만 가져오기 때문이다.
    * 이런 케이스에는 그냥 where절로 가져오기만한다.
    *
    * on절 시 내부조인이면 where절로 해결하고 정말 외부조인이 필요할 경우
    * 이 기능을 사용한다.
    * */

    /*2.연관관계 없는 엔티티 외부조인
    * 회원의 이름이 팀 이름과 같은 대상을 외부 조인
    * */

    @Test
    public void join_on_no_relation(){
        em.persist(new Member("TeamA"));
        em.persist(new Member("TeamB"));
        em.persist(new Member("TeamC"));
        /*보틍은 member.team으로 id매칭을 하는데
        * 이걸 빼고 team이면 id를 매칭을 하지않기 떄문에 이름으로
        * 매핑을 하게 된다.
        * tuple = [Member{id=1, username='member1', age=10}, null]
tuple = [Member{id=2, username='member2', age=20}, null]
tuple = [Member{id=3, username='member3', age=30}, null]
tuple = [Member{id=4, username='member4', age=40}, null]
tuple = [Member{id=5, username='TeamA', age=0}, Team(id=1)]
tuple = [Member{id=6, username='TeamB', age=0}, Team(id=2)]
        tuple = [Member{id=7, username='TeamC', age=0}, null]
        * 이렇게 가져와서 이름이 같은 경우 조인이 가능하다.
        * */

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                //기존에는 member.team같이 들어갔지만
                //이건 조건없이 값으로만 조인하려고 하면 위와 같이
                .fetch();
        //그냥 조인이면 데이터가 없어지고 일치하는 team만 남은다.

        for(Tuple tuple:result){
            System.out.println("tuple = " + tuple);
        }
        /*
        * 맴버랑 팀을 조인하는데 with를 통해 맴버이름과 팀의 이름이 같다고 한다.
        * 이때 on절에서 유저이름과 팀의 이름이 같다라는 매핑만 존재.
        * 조인에 id가 있다면 id값 매핑이지만 그냥 team이라면
        * 해당 조건의 이름값으로 조인을 한다.
        * */
        /*하이버네이트 5.1부터 on을 사용하여 서로 관게가 없는 필드로 외부조인하는
        * 기능이 생김.
        * 주의 문법을 잘봐야한다.
        * 일반 조인과 다르게 엔티티 하나만 들어간다.*/


    }

    /*패치 조인
     * 패치 조인은 SQL에서 제공하는 기능이 아닌 SQL조인을 활용하여
     * 연관된 엔티티를 SQL한번에 조회하는 기능
     * 주로 성능 최적화에서 자주 사용한다.
     * */
    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        //로딩된 엔티티인지 프록시 엔티티인 지 알려주는 유틸리티
        assertThat(loaded).as("패치 조인 미적용").isFalse();


    }
    @Test
    public void fetchJoinUse(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team,team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        //로딩된 엔티티인지 프록시 엔티티인 지 알려주는 유틸리티
        assertThat(loaded).as("패치 조인 미적용").isTrue();


    }
    /*
    * 이렇게 퍼시스트 유틸을 통해 확인해보면 no일 경우
    * 프록시 객체지만
    * 패치 조인을 하게 되면 프록시 객체가 아닌
    * 실제 팀 객체인 것을 알 수 있다.
    * */

    /*서브쿼리
    * QueryDsl에서는 자바 코드라 쉽지 않다.
    * com.querydsl.jpa.JPAExpressions 사용
    * */
    /*
    나이가 가장 많은 회원을 조회
    * */
    @Test
    public void subQuery(){
        QMember memberSub = new QMember("memberSub");
        //서로 같은 테이블 두개를 쓸때는 서브쿼리를 만들어야함
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(40);
        /*where절에서 서브쿼리가 들어왔는데
        * 맴버테이블에서 맴버의 나이가 가장 많은 사람을 조회하고
        * 해당 사용자가 조건이된다.
        * 만약 서브 Q타입을 안만들면 충돌이 일어난다.
        * */
    }
    /*나이가 평균 이상인 회원*/
    @Test
    public void subQueryGoe() {
        QMember memberSub = new QMember("memberSub");
        //서로 같은 테이블 두개를 쓸때는 서브쿼리를 만들어야함
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(30,40);
    }
    /*
    * In절 사용 */
    @Test
    public void subQueryIn(){
        QMember memberSub=new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();
        assertThat(result)
                .extracting("age")
                .containsExactly(20,30,40);
    }

    /*select절에도 서브쿼리가 된다.
    * */
    @Test
    public void selectSubquery(){

        QMember memberSub=new QMember("memberSub");
        List<Tuple> result =queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub)
                        ).from(member)
                .fetch();
        for(Tuple tuple:result){
            System.out.println("tuple = " + tuple);
        }

    }
    /*JPAExpression도 static형태로 사용 가능하다.
    * 내부에서 사용하기 때문에 깔끔하게 정리된다.*/

    /*
    * from절의 서브쿼리 한계
    * JPA JPQL 서브쿼리의 한계점으로 from절의  서브커리(인라인뷰)는 지원하지 않는다.
    * 당연히 Querydsl도 지원하지 않는다.
    * 하이버네이트 구현체를사용하면 select절의 서브쿼리는 지원한다.
    * Querydls도 하이버네이트 구현체를 사용하면select절의 서브쿼리를 지원한다.
    *
    * from절의 서브쿼리 해결방안
    * 1.서브쿼리를 join으로 변경한다.(가능한 상황도 있고 불가능한 상황도 있다.)
    * 2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
    * 3. nativeSQL을 사용한다.
    *
    * 애플리케이션에서 쿼리를 2번 분리해서 실행하는 방법이 존재.
    * 안되면 네이티브 쿼리 사용
    *
    * from절에 서브쿼리를 쓰는 이유는
    * 안좋은 이유가 많다.
    * DB라는 것은 쿼리에서 기능을 많이 제공하니깐
    * sql에 화면과 관계된 쿼리를 복잡하게 짜면
    * from절 안에 from절 구조가 나오는 경우가 있으며
    * SQL은 데이터를 가져오는 것과
    * 애플리케이션에서 로직을 태워서 화면에서 데이터 포맷은
    * 다 화면에서 해야된다.
    * 그래야 DB의 쿼리 재활용이 되는데.
    * from절 from절에 넣어야되는 화면에 맞추는 방식은
    * 현대적인 app에서 view는 프리젠테이션에서 풀어야되는데
    * 쿼리에서 다 풀려고 하니깐 쿼리가 복잡해지고
    * 서브쿼리가 들어가야되는 상황이 자주 나온다.
    * 하지만 DB는 데이터 필터링 및 그룹핑을 하고
    * 프리젠테이션 로직이나 서비스로직에서 사용하는게 좋다.
    * 데이터를 최소화해서 가져오는 방향으로 집중하는게 좋다.
    * 한방 쿼리는 정말 잘되는가?
    * -->실시간 트래픽이 중요하면 쿼리1번이 정말 아깝다.
    * 하지만 화면에 맞춘 캐시를 발라서 사용하는데
    * 어드민은 좀 느려도 되는데 여기서 쿼리한방에 짜려고
    * 하면 오히려 분리하다.
    * 이럴꺼면 쿼리를 두번 세번 나눠서 사용하는게 좋다.
    * 로직을 풀어낼 수 있는 앱단보다 DB단으로 가면
    * 더 안좋다.
    * SQL antiPetterns
    * 개발자가 알아야할 25가지 SQL패턴에 대해서
    * 여기 내용 중
    * 큰 쿼리를 사용했었는데
    * 이 책에서 복잡한 쿼리는 나눠서 호출하면
    * 몇백줄로 줄여서 훨씬 분량을 줄일 수 있을 것
    * 한방쿼리가 만능이 아니다.
    * */

    /*case문*/
    /*정확한 매치(when)
    * 복합한 조건은 CaseBuilder를 사용*/
    @Test
    public void basicCase(){
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("수무살")
                        .otherwise("기타")
                ).from(member)
                .fetch();
        for (String s:result){
            System.out.println("s = " + s);
        }
    }

    /*복잡한 조건 Case*/
    @Test
    public void complexCase(){
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타")

                ).from(member)
                .fetch();
        for(String s:result){
            System.out.println("s = " + s);
        }
        /*기본적인 jpqlCase문으로 필요할때 찾아보면 좋다.
        * 하지만 이걸 굳이 써야되는가?
        * 사실 조건처리는 DB에서 하지마라
        * DB는 로우데이터를 필터링하고 그룹핑하고
        * 필요하면 계산은 되지만 최소한의 필터링과
        * 그룹핑으로 데이터를 줄이고
        * 전환하고 바꾸고 하는건 DB가 아닌 app에서 해라.*/
    }

    /*상수,문자 더하기
    * */
    @Test
    public void constant(){
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        for (Tuple tuple:result){
            System.out.println("tuple = " + tuple);
        }
    }
    //jpql에는 문자가 안나오지만
    //실제 값에서는 뒤에 ,A로 나온다.

    /*문자더하기
    * */
    @Test
    public void concat(){
        //{username}_{age}로
        //하지만 나이는 타입이 문자열이 아니라 치환이 필요
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();
        for(String s: result){
            System.out.println("s = " + s);
        }
    }
    /*member1_10으로 나오고
    * concat으로 jpql도 나오지만 age는 as char로
    * 케스팅한 것도 확인 할 수 있다.*/
    /*.stringValue를 쓸 경우가 생각보다 많다.
    * */
    //enum처리 시 많이 사용하는 stringValue

    /*중급 문법
    * 프로젝션(select 대상 지정)
    * 프로젝션 대상이 1개일 경우
    * 반환타입을 단일 타입으로 명확하게 지정
    */
    @Test
    public void simpleProjection(){
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        for (String s:result) {
            System.out.println("result = " + result);
        }
        //당연히 유저이름이 나온다.
        //객체 타입도 프로젝션 대상이 된다.
    }

    /*

    * 프로젝션 둘 이상이면 튜플이나 dto로 조회해야된다.
    * 튜플은 여러개 조회할 때 준비해놓은 타입
    * 보통 한번에 여러개 담아서 꺼낼 수 있는게 튜플이다.
    * */
    @Test
    public void tupleProjection(){
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();
        for(Tuple tuple:result){
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }
    /*이렇게 하면 당연히 유저이름과 나이가 원하는 데이터 컬럼을 찍어서
    * 가져온다.
    * 이런걸 프로젝션이라 한다.
    * 튜플은 com.querydsl.query 패키지에 있다.
    * 이 튜플은 리포를 넘어 서비스랑 컨트롤러까지 넘어가면 좋은 설계가 아니다.
    * jdbc를 쓸때 반환해주는 것 dao나 리포에서 쓰고
    * 의존을 없도록 설계하는게 좋다. 그래야 하부 기술을 변경해도
    * 앞단 변경을 할 필요가 없다.
    * 스프링은 보통 이렇게 설계한다.(밖에서 던질때는 dto로 변환)
    *  */

    /*프로젝션과 결과반환dto
    * 쿼리할 때 dto에 필요한 값만 가져오고 싶을경우
    * 해당 값만 프로젝션하여 가져오고 싶은 경우
    * 결국 dto는 하나의 통이 되는 것*/
    @Test
    public void findDtoByJPQL(){
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username,m.age) " +
                        "from Member m", MemberDto.class)
                .getResultList();
        //이처럼 직접 지정하여 프로젝션을 하는 방법을
        //뉴 오퍼레이션이라 부른다.
        for (MemberDto memberDto:result){
            System.out.println("result = " + memberDto);
        }
        /*여기서 쿼리를 보면 username과 age 두개만
        * 조회하는 것을 확인할 수 있다.*/
    }

    /*이처럼 jpql이 나온다. 하지만 이건 코드를 짤 때 패키지명을 다 적어야하는
    * 단점이 존재한다.
    * 순수JPA에서 dto를 조회할때 new명령어를 사용해야되고
    * 생성자 방식만 지원한다.
    * setter나 바로 필드에 주입은 안된다.*/

    /*하지만 queryDsl은 이걸 극복한
    * 프로퍼티 접근,필드 직접 접근, 생성자 사용*/
    @Test
    public void findDtoBySetter(){
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for(MemberDto memberDto:result){
            System.out.println("memberDto = " + memberDto);
        }
    }
    //기본 생성자가 없는 경우 @Data는
    //getter/setter등 다 만들어준다. 하지만 기본 생성자를 안만들어준다.
    //기본 생성자를 호출해서 만드는데 없으면 오류가 나온다.
    /*이게 프로퍼티 생성자
    * */

    /*필드 생성자 방법*/
    @Test
    public void findDtoByField(){
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for(MemberDto memberDto:result){
            System.out.println("memberDto = " + memberDto);
        }
    }
    /*getter setter가 필요 없다.
    * 필드에 바로 값을 넣는다.
    * private인데 값이 들어간다.
    * 자바 리플랙션이나 다른 방법들이 존재*/

    /*생성자 접근 방법*/
    @Test
    public void findDtoByConstructor(){
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for(MemberDto memberDto:result){
            System.out.println("memberDto = " + memberDto);
        }
    }
    /*생성자 접근 방법인 constructor는
    * 엔티티와 dto의 필드 타입이 같아야한다.*/

    /*별칭이 다른 경우가 존재하는데
    * MemberDto가 아닌 userDto로 한다면?*/
    @Test
    public void findUserDto(){
        List<UserDto> result = queryFactory
                .select(Projections.bean(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();
        //그냥 필드명이 맞아야 들어간다.
        //setter는 프로퍼티 명이 맞아야된다.
        for(UserDto userDto:result){
            System.out.println("memberDto = " + userDto);
        }
        /*매칭되는 값이 없어서 무시가 되는 상황
        * 이름은 null로 들어간다. 왜냐면 매칭이 안되서
        * userDto의 name이라는 필드이기 때문에
        * 별칭으로 이름을 매핑시켜줘야한다.
        * .as("name")
        * 로 별칭값으로 변경해주면 된다.*/
    }

    /*이름이 없을때*/
    @Test
    public void findUserDtoNoAgeName(){
        QMember memberSub=new QMember("memberSub");
        List<UserDto> result = queryFactory
                .select(Projections.bean(UserDto.class,
                        member.username.as("name"),
                        //이건 이름으로 가능
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub),"age")
                        //age에서 서브쿼리를 사용하면? 회원나이의 max값으로
                        //오른쪽은 전부 최대나이로 나오게 하고 싶을 경우
                        //이렇게 필드에 익스프레션유틸로 두번째 마라미터로 감싸면
                        //별칭을 줄 수 있다.
                        //이렇게 서브쿼리를 줄 수 있다.
                ))
                .from(member)
                .fetch();

        for(UserDto userDto:result){
            System.out.println("memberDto = " + userDto);
        }
        /*매칭되는 값이 없어서 무시가 되는 상황
         * 이름은 null로 들어간다. 왜냐면 매칭이 안되서
         * userDto의 name이라는 필드이기 때문에
         * 별칭으로 이름을 매핑시켜줘야한다.
         * .as("name")
         * 로 별칭값으로 변경해주면 된다.*/
        /*프로퍼티나 필드 접근 생성 방식에서 이름이 다를 때 해결방안
        * ExprssionUtis.as기법으로 사용해도 된다.
        * 하지만 이렇게 하면 코드 자체가 지저분해진다. 그래서
        * .as를 통해 별칭을 주는게 더 좋다.
        * 서브쿼리는 방법이 없어서 이렇게 익스프레션유틸로 감싸야된다.
        * 생성자는 타입을 보고 들어가게 된다.*/


    }
    /*생성자 접근 방법*/
    @Test
    public void findDtoByUserDtoConstructor(){
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for(UserDto memberDto:result){
            System.out.println("memberDto = " + memberDto);
        }
    }
    /*이렇게 변경하면 바로 dto에 값이 들어가는데
    * 이때도 dto에 생성자가 없으면 오류가 나온다.
    * 여기 이상의 복잡도를 가진 프로젝션은 없다.
    *  */
    
    /*
    * setter 방식
    * 필드 인젝션 방식
    * 생성자 방식
    * 이 3가지가 대표적인 방법이며
    * setter나 필드 인젝션은 이름이 매칭되야하기 떄문에 별칭을
    * 활용해야된다.*/
    

}




























