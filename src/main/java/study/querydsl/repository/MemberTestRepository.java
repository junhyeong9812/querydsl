package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.repository.support.Querydsl4RepositorySupport;

import java.util.List;
import java.util.function.Function;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.team;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {
    public MemberTestRepository(){
        super(Member.class);
    }

    public List<Member> basicSelect(){
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom(){
        return selectFrom(member)
                .fetch();
    }
    /*select와
    * selectfrom
    * 생성시점에 도메인 클래스에 전부 넘기고
    * 엔티티 시점에 엔티티 메니저랑 필요한 모든 것을 만들어내고
    * select에서는 쿼리 팩토리로 셀렉트 활용
    * selectfrom에서는 쿼리 팩토리에서 셀렉트 프럼 활용
    * */

    public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable){
        JPAQuery<Member> query =selectFrom(member)
                .leftJoin(member.team,team)
                .where(usernameEq(condition.getUsername())
                ,teamNameEq(condition.getTeamname()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );
        /*기존에는 springdata에서 받고
        * getQuerydsl().applyPagination(pageable,query)*/
        List<Member> content = getQuerydsl().applyPagination(pageable, query)
                .fetch();
        return PageableExecutionUtils.getPage(content,pageable,query::fetchCount);
        /*이게 그나마 최적화할 수 있는 방벙*/

    }

    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable, contentQuery -> contentQuery
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()), teamNameEq(condition.getTeamname()), ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())));
    }
    /*쿼리를 넘겨주고 페이저블을 넘겨주면
    * 쿼리를 apply는 실행시키고
    * 그걸 쿼리팩토리에 넘겨서 jpaQuery를 통해 패치로
    * 컨텐츠와 페이저블 유틸을 사용하여 쿼리에서 패치컨텐츠를 가져오도록 한다.
    * 자바 8의 람다가 나오면서 코드를 깔끔하게 만들 여지가 많아졌다.
    * */

    public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable, contentQuery -> contentQuery
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamname()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ),
                countQuery -> countQuery
                .select(member.id)
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()), teamNameEq(condition.getTeamname()), ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
        );
        /*이렇게 이전보다 쿼리를 훨씬 깔끔하게 내보낼 수 있게 되었다.
        * 여기서는 페이저블과 컨텐츠쿼리와 카운트 쿼리를 작성해서
        * 넘기면 된다.
        * 결국 람다를 활용해서 코드의 유지보수를 늘린 상황
        * */
    }

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
    protected <T> Page<T> applyPagination(Pageable pageable, Function<JPAQueryFactory, JPAQuery> contentQuery) {
        return super.applyPagination(pageable, contentQuery);
    }
}
