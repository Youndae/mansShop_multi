package com.example.modulemypage.repository;

import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.modulemypage.model.dto.memberQnA.business.MemberQnADetailDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnAListDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

import static com.example.modulecommon.model.entity.QMemberQnA.memberQnA;
import static com.example.modulecommon.model.entity.QQnAClassification.qnAClassification;

@Repository
@RequiredArgsConstructor
public class MemberQnADSLRepositoryImpl implements MemberQnADSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<MemberQnAListDTO> findAllByUserId(String userId, Pageable pageable) {
        List<MemberQnAListDTO> list = jpaQueryFactory.select(
                        Projections.constructor(
                                MemberQnAListDTO.class
                                , memberQnA.id.as("memberQnAId")
                                , memberQnA.memberQnATitle
                                , memberQnA.memberQnAStat
                                , qnAClassification.qnaClassificationName.as("qnaClassification")
                                , memberQnA.updatedAt
                        )
                )
                .from(memberQnA)
                .innerJoin(memberQnA.qnAClassification, qnAClassification)
                .where(memberQnA.member.userId.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(memberQnA.id.desc())
                .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(memberQnA.countDistinct())
                .from(memberQnA)
                .where(memberQnA.member.userId.eq(userId));

        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }

    @Override
    public MemberQnADetailDTO findByQnAId(long qnaId) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                MemberQnADetailDTO.class,
                                memberQnA.id.as("memberQnAId"),
                                qnAClassification.qnaClassificationName.as("qnaClassification"),
                                memberQnA.memberQnATitle.as("qnaTitle"),
                                new CaseBuilder()
                                        .when(memberQnA.member.nickname.isNull())
                                        .then(memberQnA.member.userName)
                                        .otherwise(memberQnA.member.nickname)
                                        .as("writer"),
                                memberQnA.memberQnAContent.as("qnaContent"),
                                memberQnA.updatedAt,
                                memberQnA.memberQnAStat
                        )
                )
                .from(memberQnA)
                .innerJoin(memberQnA.qnAClassification, qnAClassification)
                .where(memberQnA.id.eq(qnaId))
                .fetchOne();
    }

    @Override
    public MemberQnA findModifyDataByIdAndUserId(long qnaId, String userId) {
        return jpaQueryFactory.select(memberQnA)
                .from(memberQnA)
                .where(
                        memberQnA.id.eq(qnaId)
                                .and(memberQnA.member.userId.eq(userId))
                )
                .fetchOne();
    }

    @Override
    public List<AdminQnAListResponseDTO> findAllByAdminMemberQnAPage(AdminQnAPageDTO pageDTO) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT q.id, ")
                .append("qc.qnaClassificationName, ")
                .append("q.memberQnATitle, ");

        if(pageDTO.keyword() == null)
            queryBuilder.append("CASE WHEN (m.nickname IS NULL) THEN m.userName ELSE m.nickname END, ");
        else
            queryBuilder.append("q.userId, ");

        queryBuilder.append("q.updatedAt, ")
                .append("q.memberQnAStat ")
                .append("FROM (")
                .append(adminMemberQnADynamicSubQuery(pageDTO))
                .append(") as q ")
                .append("INNER JOIN qnaClassification qc ")
                .append("ON q.qnaClassificationId = qc.id ");

        if(pageDTO.keyword() == null)
            queryBuilder.append("LEFT JOIN member m ")
                    .append("ON m.userId = q.userId");

        Query query = em.createNativeQuery(queryBuilder.toString());

        query.setParameter("offset", pageDTO.offset());
        query.setParameter("amount", pageDTO.amount());

        if(pageDTO.keyword() != null)
            query.setParameter("keyword", pageDTO.keyword());

        List<Object[]> resultList = query.getResultList();

        return resultList.stream()
                .map(val -> new AdminQnAListResponseDTO(
                        ((Number) val[0]).longValue(),
                        (String) val[1],
                        (String) val[2],
                        (String) val[3],
                        ((Timestamp) val[4]).toLocalDateTime(),
                        (Boolean) val[5]
                ))
                .toList();
    }

    public String adminMemberQnADynamicSubQuery(AdminQnAPageDTO pageDTO) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT mq.id, ")
                .append("mq.qnaClassificationId, ")
                .append("mq.memberQnATitle, ")
                .append("mq.updatedAt, ")
                .append("mq.memberQnAStat, ");

        if(pageDTO.keyword() == null)
            queryBuilder.append("mq.userId ")
                    .append("FROM memberQnA mq ")
                    .append("WHERE 1=1 ");
        else
            queryBuilder.append("CASE WHEN (m.nickname IS NULL) THEN m.userName ELSE m.nickname END as userId ")
                    .append("FROM memberQnA mq ")
                    .append("INNER JOIN member m ")
                    .append("ON mq.userId = m.userId ")
                    .append("WHERE (m.nickname = :keyword OR m.userId = :keyword) ");

        queryBuilder.append(adminMemberQnASubQuerySearch(pageDTO))
                .append("ORDER BY mq.updatedAt DESC LIMIT :offset, :amount");

        return queryBuilder.toString();
    }

    public String adminMemberQnASubQuerySearch(AdminQnAPageDTO pageDTO) {
        String query = "";

        if(pageDTO.listType().equals(AdminListType.NEW.getType()))
            query = "AND mq.memberQnAStat = 0 ";

        return query;
    }

    @Override
    public Long countByAdminMemberQnA(AdminQnAPageDTO pageDTO) {
        return jpaQueryFactory.select(memberQnA.updatedAt.count())
                .from(memberQnA)
                .where(adminMemberQnASearch(pageDTO))
                .fetchOne();
    }

    public BooleanExpression adminMemberQnASearch(AdminQnAPageDTO pageDTO){
        if(pageDTO.listType().equals(AdminListType.NEW.getType())){
            if(pageDTO.keyword() != null) {
                return memberQnA.memberQnAStat.isFalse()
                        .and(
                                memberQnA.member.nickname.eq(pageDTO.keyword())
                                        .or(memberQnA.member.userId.eq(pageDTO.keyword()))
                        );
            }else
                return memberQnA.memberQnAStat.isFalse();
        }else if(pageDTO.listType().equals(AdminListType.ALL.getType())) {
            if(pageDTO.keyword() != null)
                return memberQnA.member.nickname.eq(pageDTO.keyword()).or(memberQnA.member.userId.eq(pageDTO.keyword()));
        }

        return null;
    }
}
