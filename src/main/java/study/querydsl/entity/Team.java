package study.querydsl.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)     //jpa는 기본생성자가 항상 필요하다.
@ToString(of={"id","username"})
public class Team {
    @Id @GeneratedValue
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")
    List<Member> members = new ArrayList<>();
    //Many인 user가 연관관계의 주인이 된다.
    //그래서 외례키 값을 업데이트 하지 않는다.
    public Team(String name){
        this.name=name;
    }



}
