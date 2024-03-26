package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import study.querydsl.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long>,MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {


    //기본으로 없는 쿼리 생성
    List<Member> findByUsername(String username);
}
