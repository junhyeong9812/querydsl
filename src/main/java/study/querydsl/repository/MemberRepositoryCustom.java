package study.querydsl.repository;

import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;
/*JPA는 인터페이스이기 때문에
 * 사용자 정의 리포지토리를 통해 구현체가 만들 수 없는
 * 리포지토리 함수들을 만들어야한다.
 * 사용자 정의 인터페이스 작성 ->구현 ->인터페이스 상속*/
public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
