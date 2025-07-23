package study.querydsl.controller;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

@Profile("local")
/*application의 프로파일에 조건이 일치하면 동작
* 그러면 시작할때 데이터를 넣고 시작하도록 할 수 있다.*/
@Component
@RequiredArgsConstructor
public class initMember {

    private final InitMemberService initMemberService;

    @PostConstruct
    public void init(){
            initMemberService.init();
    }

    @Component
    static class InitMemberService{

        @PersistenceContext private EntityManager em;
        @Transactional
        public void init(){
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);

            for(int i = 0;i <100;i++){
                Team selectedTeam = i%2==0?teamA:teamB;
                em.persist(new Member("member"+i,i,selectedTeam));
            }
        }
        /*트랜젝션을 포스트컴포넌트에 넣으면안되나요?
        * 안된다.
        * 포스트컨스트럭트와 트랜젝션이 동시에 불가능하다.
        * 아마 래핑과정에서 문제가 발생하는 듯하다.*/
    }
}
