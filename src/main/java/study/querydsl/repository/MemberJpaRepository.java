package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.team;

/* 기존의 DAO와 비슷*/
@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    /*이떄 em/queryFactory가 싱글톤인데
    * 멀티쓰래드에서 다쓰면 동시성 문제?
    * 문제가 되지 않는다.
    * 엔티티 매니저 내부에 싱글톤이지만
    * 쿼리 팩토리의 동시성문제는 엔티티메니져에 의존하는데
    * 엔티티메니저가 스프링과 엮이면
    * 동시성 문제와 관계 없이 트랜잭션 단위에서
    * 분리되어 사용된다.
    * 그래서 스프링에서는 em이 영속성 컨텍스트가 아닌 프록시로
    * 가짜를 반환하고
    * 이걸 트랜젝션 단위로 다른 곳 바인딩되도록 라우팅만 해준다.
    * 결론적으로 문제 없다.!!
    * ->해당 내용은 트랜잭션 범위의 영속성 컨텍스트에 대해서 알면 좋다.
    * 결국 스프링단에서 하는 메세지큐같은 역할같은데 확인해보자.*/

    public MemberJpaRepository(EntityManager em,JPAQueryFactory queryFactory) {
        this.em = em;
        this.queryFactory = queryFactory;
        /*이렇게 빈으로 등록해놓고 자동주입으로
        * 주입받아도 가능하다.
        * 이러한 빈 구조로 만들면
        * 롬북의 @RequireArgsConstructor를 이용해서
        * 이러한 생성자도 생략 가능하다.
        * bean에 등록해놨을 경우
        * 그러니 쿼리빌드를 할때 이렇게 빈을 만들어놓는게 좋다.
        * 빈을 주입받아야 되서 테스트 코드에서는 불편하다는 점이 존재한다.
        *
        * */

    }
    /*하지만 이걸 바로 컨트롤러나 외부에서 호출할떄
    * 트랜젝션 내부에서 처리할 수 있도록 해야된다.
    * 그리고 */

//    public MemberJpaRepository(EntityManager em) {
//        this.em = em;
//        this.queryFactory = new JPAQueryFactory(em);
//        //이렇게 em을 받고 쿼리 팩토리로 new형식으로 해도 되지만
//        //해당 구조를 빈으로 등록을 해도 된다.
//    }
    /*EntityManage -> jpa접근 시 반드시 필요
    * 생성자 인젝션을 통해 스프링 인젝션을 받는다.
    * 쿼리dsl을 사용하기 위해 엔티티메니져로 쿼리 팩토리 생성*/

    public void save(Member member){
        em.persist(member);
    }

    public Optional<Member> findById(Long id){
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
    /*이처럼 위에 쿼리를 치는 것보다
    * 자바코드로 한번에 만들 수 있다.*/
    public List<Member> findAll_Querydsl(){
         return queryFactory
                 .selectFrom(member)
                 .fetch();
    }


    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username= :username", Member.class)
                .setParameter("username",username)
                .getResultList();
    }
    /*이걸 보면 알겠지만 단순히 쿼리 빌드 느낌으로 깔끔하게 된느걸
    * 볼 수 있다.*/

    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    /*검색 조건 생성*/
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){
        /*빌더를 통한 동적 쿼리 생성*/
        BooleanBuilder builder = new BooleanBuilder();
        //이건
        if(hasText(condition.getUsername())){
            builder.and(member.username.eq(condition.getUsername()));
        }
        if(hasText(condition.getTeamname())){
            builder.and(team.name.eq(condition.getTeamname()));
        }
        //hasText -> null !=  "" !=
        if(condition.getAgeGoe() != null){
            builder.and(member.age.goe((condition.getAgeGoe())));
        }
        if(condition.getAgeLoe() != null){
            builder.and(member.age.loe((condition.getAgeLoe())));
        }
        /*이렇게 동적쿼리와 성능최적화까지 가능하다.*/

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }
}
