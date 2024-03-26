package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);

    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    //전체 데이터를 한번에 받기
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
    //전체 데이터를 별도로 받기
    Page<MemberTeamDto> searchPageUtils(MemberSearchCondition condition, Pageable pageable);
    //페이지에서 제공하는 카운트 사용(데이터가 페이지 보다 작아야한다)
}
