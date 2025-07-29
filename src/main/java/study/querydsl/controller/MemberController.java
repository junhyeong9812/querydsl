package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

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

    /*컨트롤러 개발*/
    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(@ModelAttribute MemberSearchCondition condition, Pageable pageable){
        return memberRepository.searchPageSimple(condition,pageable);
    }
    /*스프링 데이터가 페이저블 객체를 넘기면
    * 컨트롤러가 바인딩될 때 바인딩을 해서 데이터를 넘겨준다.
    * 이걸 넘기고 반환도 페이지로 넘겨주면 된다.
    *
    * */

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(@ModelAttribute MemberSearchCondition condition, Pageable pageable){
        return memberRepository.searchPageComplex(condition,pageable);
    }
    /*카운트 쿼리를 날리고 실행 시 카운트 쿼리가 필요 시만 날린다.
    * size가 전체 데이터보다 크거나 마지막 페이지일 경우
    * 카운트 쿼리가 안나가는 것을 확인할 수 있다.
    * 스프링 데이터에서 Sort조건을 넘기면
    * 스프링 데이터의 sort는 쿼리DSL에서 자동화하기 어렵다.
    * 수동은 쉽게 된다.
    * 스프링 데이터 JPA가 제공하는 QueryOrderSpecifier로
    * 깔끔하게 변환하는걸 제공해준다.
    * Join 시 정렬이 안된다.
    * 이게 단순하면 페이징이 동작하지 않는다.
    * 그래서 파라미터로 받아서 그때그때 오더 조건으로 넣는걸 권장한다.
    * */



}
