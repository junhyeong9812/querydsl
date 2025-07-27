package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;
/*JPA는 인터페이스이기 때문에
 * 사용자 정의 리포지토리를 통해 구현체가 만들 수 없는
 * 리포지토리 함수들을 만들어야한다.
 * 사용자 정의 인터페이스 작성 ->구현 ->인터페이스 상속*/
public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);

    /*스프링 데이터 페이징 활용
    * 스프링 데이터의 Page/Pageable을 활용
    * 전체 카운트를 한번에 조회하는 단순한 방법
    * 데이터 내용과 전체 카운트를 별도로 조회하는 방법*/
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    /*카운트 쿼리와 나눠서 */
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);

    public Page<MemberTeamDto> searchPageComplexPage(MemberSearchCondition condition, Pageable pageable);


}
