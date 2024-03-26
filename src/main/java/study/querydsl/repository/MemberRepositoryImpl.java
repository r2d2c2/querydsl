package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom{
    //커스텀 인터페이스

    private final JPAQueryFactory queryFactory;
    public MemberRepositoryImpl(EntityManager em) {
        super(Member.class);
        this.queryFactory=new JPAQueryFactory(em);
    }



    //QuerydslRepositorySupport를 사용하여 주석 처리
    /*private final JPAQueryFactory queryFactory;
    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }*/

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
         return from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        , teamNameEq(condition.getTeamName())
                        , ageGoe(condition.getAgeGoe())
                        , ageLoe(condition.getAgeLoe()))
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                )).fetch();


        /*return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        ,teamNameEq(condition.getTeamName())
                        ,ageGoe(condition.getAgeGoe())
                        ,ageLoe(condition.getAgeLoe()))
                .fetch();*/
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        /*QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        , teamNameEq(condition.getTeamName())
                        , ageGoe(condition.getAgeGoe())
                        , ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())//전체 페이지
                //페이지 조회
                .limit(pageable.getPageSize())//모든 페이지
                // 시작과 끝 정의
                .fetchResults();

        List<MemberTeamDto> conten=results.getResults();
        //데이터베이스에 받은 데이터를 리스트로 저장
        long total = results.getTotal();
        return new PageImpl<>(conten,pageable,total);
        //(리스트,페이지,모든 수)*/

        JPQLQuery<MemberTeamDto> jpqlQuery = from(member)
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        , teamNameEq(condition.getTeamName())
                        , ageGoe(condition.getAgeGoe())
                        , ageLoe(condition.getAgeLoe()));


        JPQLQuery<MemberTeamDto> jpa = getQuerydsl().applyPagination(pageable, jpqlQuery);
        //.offset(pageable.getOffset())
        //.limit(pageable.getPageSize())//생략 된다

        jpa.fetchResults();

        return new PageImpl<>(jpa.fetch(),pageable,jpa.fetchResults().getTotal());
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> co = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        , teamNameEq(condition.getTeamName())
                        , ageGoe(condition.getAgeGoe())
                        , ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())//전체 페이지
                //페이지 조회
                .limit(pageable.getPageSize())//모든 페이지
                // 시작과 끝 정의
                .fetch();
        long total = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .fetchCount();



        return new PageImpl<>(co,pageable,total);
    }

    @Override
    public Page<MemberTeamDto> searchPageUtils(MemberSearchCondition condition, Pageable pageable) {
        //가장 효율적인 카운트 방식이지만 조건이 조금 까다롭다
        //마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함, 더 정확히는 마지막 페이지이면
        //서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때)
        List<MemberTeamDto> co = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        , teamNameEq(condition.getTeamName())
                        , ageGoe(condition.getAgeGoe())
                        , ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())//전체 페이지
                //페이지 조회
                .limit(pageable.getPageSize())//모든 페이지
                // 시작과 끝 정의
                .fetch();
        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        , teamNameEq(condition.getTeamName())
                        , ageGoe(condition.getAgeGoe())
                        , ageLoe(condition.getAgeLoe()));
        return PageableExecutionUtils.getPage(co,pageable, countQuery::fetchCount);
        // (데이터 리스트,페이지,JPAQuery::fetchCount)
    }

    private BooleanExpression usernameEq(String username) {
        //없으면 null
        return StringUtils.hasText(username) ? member.username.eq(username) :null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName)?team.name.eq(teamName):null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe!=null ? member.age.goe(ageGoe):null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe!=null?member.age.loe(ageLoe):null;
    }



            /*JPAQuery<Member> query = queryFactory
                .selectFrom(member);
        for (Sort.Order o : pageable.getSort()) {
            PathBuilder pathBuilder = new PathBuilder(member.getType(),
                    member.getMetadata());
            query.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC,
                    pathBuilder.get(o.getProperty())));
        }
        List<Member> result = query.fetch();*/
}
