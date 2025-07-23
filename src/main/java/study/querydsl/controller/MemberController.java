package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(@ModelAttribute MemberSearchCondition condition){
        return memberJpaRepository.search(condition);
    }
    /*서버 구동해보고 확인해보면
    * 데이터가 잘 나오는 것을 볼 수 있다.
    * 프로파일로 나누지 않으면
    * 실행하면서 로컬의 생성데이터가 테스트에서도 생겨서
    * 기존 테스트 데이터와 충돌이 날 수 있으니
    * 프로파일로 나누는게 좋다.*/

}
