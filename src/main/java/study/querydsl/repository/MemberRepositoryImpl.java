package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    /*where절 파라미터 방식*/
    public List<MemberTeamDto> search(MemberSearchCondition condition){
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team,team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamname()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                /*쿼리처럼 깔끔하게 데이터가 나오는 걸 볼 수 있다.
                 * 이렇게 BooleanExpression타입으로 만들어놓으면
                 * 아래 함수들을 조건절에 재사용이 가능하다.*/
                .fetch();
    }

    /*이런 복잡한 쿼리는 결국 직접 만들어야 된다.
    * 조회가 복잡하면
    * 커스텀을 안하고
    * 특정한 기능에 맞춰진 조회 기능이라면
    * 별도의 MemberQueryRepository로 구현체를 만든다.
    * 검색기능이 너무 특화된 기능이기 때문에
    * */

    private BooleanExpression ageBetween(int ageLoe, int ageGoe){
        return ageGoe(ageLoe).and(ageGoe(ageGoe));
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null ;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamname()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                /**
                 * 이렇게 페이지 설정을 할 수 있다.
                 * fetchResults를 사용하면
                 * 페이징용 쿼리와 조회용 쿼리 두번을 날린다.
                 * 토탈 카운트 쿼리에서는
                 * orderBy는 쿼리에서 제거된다.
                 * 2번째로 데이터의 내용과 전체 카운트를 별도로 조회*/
                .fetchResults();
        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content,pageable,total);
        /**
         * 페이지Impl이 페이지의 구현체로 해당 객체로 리턴*/

    }
    /*페이저블은 오프셋이나 페이지를 확인 가능하다.*/

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = getMemberTeamDtos(condition, pageable);

        long total = getTotal(condition);


        return new PageImpl<>(content,pageable,total);
    }

    private List<MemberTeamDto> getMemberTeamDtos(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamname()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        return content;
    }

    private long getTotal(MemberSearchCondition condition) {
        long total = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamname()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetchCount();
        return total;
    }
    /**
     * 1번은 패치 리저트로 토탈 쿼리를 날려주는데
     * 2번은 내가 직접 날리는 것
     * 이걸 하면 좋은 점은?
     * 상황에따라 다른데
     * 조인이 필요 없을 경우가 있는데
     * 조인을 줄여서 해도 되거나.
     * 이런 경우 fetchResult를 쓰면 결국 조인을 다 써야되서
     * 최적화가 되지않는다.
     * 하지만 2번안으로 별도의 카운트 쿼리로 나누는게 더 좋다.
     * count를 하고 값이 없으면
     * result요청을 안하고 이런식으로 서비스레이어에서 최적화를 할 수 있다.
     * 그리고 생각보다 카운트 쿼리는 효율화하는게 좋다.
     * 물론 전재가 있는데 의미 없는데 의미를 들일 필요 없다.
     * 또한 이렇게 쿼리 2개를 메서드 형태로 분리 가능하다.
     * */


}
