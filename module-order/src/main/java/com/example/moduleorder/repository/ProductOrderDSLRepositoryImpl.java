package com.example.moduleorder.repository;

import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleorder.model.dto.admin.business.AdminOrderDTO;
import com.example.moduleorder.model.dto.admin.page.AdminOrderPageDTO;
import com.example.moduleorder.model.dto.in.MemberOrderDTO;
import com.example.moduleorder.model.dto.page.OrderPageDTO;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.modulecommon.model.entity.QProductOrder.productOrder;

@Repository
@RequiredArgsConstructor
public class ProductOrderDSLRepositoryImpl implements ProductOrderDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ProductOrder> findByUserId(MemberOrderDTO memberOrderDTO, OrderPageDTO pageDTO, Pageable pageable) {

        List<ProductOrder> list = jpaQueryFactory.selectFrom(productOrder)
                                            .where(search(memberOrderDTO, pageDTO))
                                            .orderBy(productOrder.id.desc())
                                            .offset(pageable.getOffset())
                                            .limit(pageable.getPageSize())
                                            .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(productOrder.countDistinct())
                                        .from(productOrder)
                                        .where(search(memberOrderDTO, pageDTO));
        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }

    private BooleanExpression search(MemberOrderDTO memberOrderDTO, OrderPageDTO pageDTO) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime term = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0);

        if(pageDTO.term().equals("all"))
            term = LocalDateTime.of(1900, 1, 1, 0, 0);
        else
            term = term.minusMonths(Long.parseLong(pageDTO.term()));

        if(memberOrderDTO.userId() == null || memberOrderDTO.userId().equals(Role.ANONYMOUS.getRole())) {
            return productOrder.recipient
                    .eq(memberOrderDTO.recipient())
                    .and(productOrder.orderPhone.eq(memberOrderDTO.phone()))
                    .and(productOrder.member.userId.eq(Role.ANONYMOUS.getRole()))
                    .and(productOrder.createdAt.goe(term));
        }else
            return productOrder.member.userId.eq(memberOrderDTO.userId())
                    .and(productOrder.createdAt.goe(term));
    }

    public Expression<AdminOrderDTO> getFindAllOrderListSelect() {
        return Projections.constructor(
                AdminOrderDTO.class,
                productOrder.id.as("orderId"),
                productOrder.recipient,
                productOrder.member.userId,
                productOrder.orderPhone.as("phone"),
                productOrder.createdAt,
                productOrder.orderAddress.as("address"),
                productOrder.orderStat.as("orderStatus")
        );
    }

    public BooleanExpression searchAdminOrder(AdminOrderPageDTO pageDTO) {
        if(pageDTO.searchType() == null)
            return null;
        else if(pageDTO.searchType().equals("recipient"))
            return productOrder.recipient.eq(pageDTO.keyword());
        else if(pageDTO.searchType().equals("userId"))
            return productOrder.member.userId.eq(pageDTO.keyword());

        return null;
    }

    @Override
    public List<AdminOrderDTO> findAllOrderList(AdminOrderPageDTO pageDTO) {
        return jpaQueryFactory.select(getFindAllOrderListSelect())
                .from(productOrder)
                .where(searchAdminOrder(pageDTO))
                .orderBy(productOrder.createdAt.desc())
                .offset(pageDTO.offset())
                .limit(pageDTO.amount())
                .fetch();
    }

    @Override
    public Long findAllOrderListCount(AdminOrderPageDTO pageDTO) {
        return jpaQueryFactory.select(productOrder.createdAt.count())
                .from(productOrder)
                .where(searchAdminOrder(pageDTO))
                .fetchOne();
    }

    @Override
    public List<AdminOrderDTO> findNewOrderList(AdminOrderPageDTO pageDTO, LocalDateTime todayLastOrderTime) {
        return jpaQueryFactory.select(getFindAllOrderListSelect())
                .from(productOrder)
                .where(
                        productOrder.createdAt.loe(todayLastOrderTime)
                                .and(productOrder.orderStat.eq(OrderStatus.ORDER.getStatusStr()))
                                .and(searchAdminOrder(pageDTO))
                )
                .orderBy(productOrder.createdAt.desc())
                .offset(pageDTO.offset())
                .limit(pageDTO.amount())
                .fetch();
    }

    @Override
    public Long findNewOrderListCount(AdminOrderPageDTO pageDTO, LocalDateTime todayLastOrderTime) {
        return jpaQueryFactory.select(productOrder.countDistinct())
                .from(productOrder)
                .where(
                        productOrder.createdAt.loe(todayLastOrderTime)
                                .and(productOrder.orderStat.eq(OrderStatus.ORDER.getStatusStr()))
                                .and(searchAdminOrder(pageDTO))
                )
                .fetchOne();
    }

    @Override
    public Page<ProductOrder> findProductOrderPageListByDay(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<ProductOrder> list = jpaQueryFactory.select(productOrder)
                                                .from(productOrder)
                                                .where(productOrder.createdAt.between(startDate, endDate))
                                                .orderBy(productOrder.createdAt.desc())
                                                .offset(pageable.getOffset())
                                                .limit(pageable.getPageSize())
                                                .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(productOrder.id.countDistinct())
                                            .from(productOrder)
                                            .where(productOrder.createdAt.between(startDate, endDate));

        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }
}
