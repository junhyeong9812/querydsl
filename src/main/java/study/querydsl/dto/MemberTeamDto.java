package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

/*동적 쿼리와 성능 최적화 조회 - Builder사용*/
@Data
public class MemberTeamDto {
    private Long MemberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;
    /*팀와 맴버의 조인한 dto*/
    @QueryProjection
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        MemberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
    /*쿼리 프로젝션 단점이 
    * dto가 쿼리dsl에 의존하게 되서 단점이된다.
    * 그래서 프로젝션빈이나 컨스트럭터를 사용하면 된다.
    * 
    * 어드민 화면에서 팀의 이름이나 나이 조건을 통해
    * 조회할 수 있도록*/
}
