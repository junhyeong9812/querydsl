package study.querydsl.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter //Setter는 가급적이면 실무에서 사용하지 않는 게 좋다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of={"id","username","age"})
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")//외례키 컬럼명이 된다.
    private Team team;

    //추가 생성자
    public Member(String username){
        this(username,0);
    }
    public Member(String username,int age){
        this(username,age,null);
    }//팀을 제외한 생성자 및 이름만 넣는 생성자를 생성


    //생성자
    public Member(String username,int age,Team team){
        this.username=username;
        this.age=age;
        if(team != null){
            changeTeam(team);
        }
    }
    // 양방향 연관관계 주입 메서드
    public void changeTeam(Team team){
        this.team=team;
        team.getMembers().add(this);
        //팀이 변경될 때 내 자신의 team도 바꾸지만 기존의 team에 속한 곳에도 변경해줘야 한다.
    }
    //ToString을 만들 때는 아래처럼 만들거나 롬복으로 만들면 되는데
    //이때 Team은 들어가면 안된다. 만약 team이 들어가면 순환 구조가 되면서 오류가 나게 된다.
    //
    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", age=" + age +
//                ", team=" + team + //team은 넣으면 안된다.
                '}';
    }
}
