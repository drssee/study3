package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
//테스트에 붙은 @Transactional 어노테이션은
//테스트 작동 로직과 맞물려 동작한다
//1. @Test마다 새로운 테스트 객체가 만들어진 후 실행됨,
//2. 그러므로 각 @Test마다 개별적인 트랜잭션범위를 가짐
@Transactional //JPA는 트랜잭션 상에서 동작해야함(조회 제외)
public class JpaItemRepository implements ItemRepository {
    
    //DataSource + Transaction + JPA 설정 한번에
    private final EntityManager em;

    public JpaItemRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Item save(Item item) {
        em.persist(item); //pk도 insert 후 조회해 넣어준다 
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        //조회 시점에 스냅샷을 찍고, 해당 스냅샷 객체가 커밋시점에 변경되어 있으면 업데이트 쿼리 생성
        Item findItem = findById(itemId).orElseThrow();
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {

        //1.동적 jpql 생성
        String jpql = "select i from Item i";

        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }

        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%',:itemName,'%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " i.price <= :maxPrice";
        }

        log.info("jpql={}", jpql);

        //2.jqpl로 쿼리 객체를 만든뒤, 바인딩 필요한 파라미터값 전달
        TypedQuery<Item> query = em.createQuery(jpql, Item.class);
        if (StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }

        //3.검색 결과 반환
        List<Item> result = query.getResultList();

        return result;
    }

    @Override
    public void deleteAll() {
        em.clear();
    }
}
