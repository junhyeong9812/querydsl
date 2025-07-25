package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
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



}
