package hello.itemservice.domain;

import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import hello.itemservice.repository.v2.ItemQueryRepositoryV2;
import hello.itemservice.repository.v2.ItemRepositoryV2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@Slf4j
class ItemRepositoryV2Test {

    @Autowired
    ItemRepositoryV2 itemRepository;

    @Autowired
    ItemQueryRepositoryV2 itemQueryRepository;

    @Test
    void save() {
        //given
        Item item = new Item("itemA", 10000, 10);

        //when
        Item savedItem = itemRepository.save(item);

        //트랜잭션 적용일때 위에서 한 save를 아래의 find에서 찾을 수 있는 이유
        //트랜잭션 커밋을 안해도 같은 DB세션에선 임시상태의 조회가 가능하다
        //then
        Item findItem = itemRepository.findById(item.getId()).get();
        assertThat(findItem).isEqualTo(savedItem);
    }

    @Test
    void updateItem() {
        //given
        Item item = new Item("item1", 10000, 10);
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        //when
        ItemUpdateDto updateParam = new ItemUpdateDto("item2", 20000, 30);
        Item findItem1 = itemRepository.findById(itemId).orElseThrow();



        findItem1.setItemName(updateParam.getItemName());
        findItem1.setPrice(updateParam.getPrice());
        findItem1.setQuantity(updateParam.getQuantity());

        /*
        if 테스트 트렌젝션이 해당 메소드 영역에 안결려있을시
        아래의 save가 없다면 영속성 컨텍스트에서 가져온 findItem1을 변경해봐야,
        변경된 내용을 db에 쿼리로 전달하는 커밋이 없기 때문에, 변경내용이 반영되지 않는다

        하지만, 해당 메소드 영역에 트랜잭션이 걸려있다면 한 트랜잭션내에서 동작하기 때문에 영속성 컨텍스트 내부에서
        해당 작업이 메소드 끝까지 이뤄진후 결과물을 커밋하기 때문에
        아래의 save가 없어도 된다(좀더 테스트 해봐야함)
         */
//        itemRepository.save(findItem1);



        //then
        Item findItem2 = itemRepository.findById(itemId).get();
        assertThat(findItem2.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem2.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem2.getQuantity()).isEqualTo(updateParam.getQuantity());
    }

    @Test
    void findItems() {
        //given
        Item item1 = new Item("itemA-1", 10000, 10);
        Item item2 = new Item("itemA-2", 20000, 20);
        Item item3 = new Item("itemB-1", 30000, 30);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        //둘 다 없음 검증
        test(null, null, item1, item2, item3);
        test("", null, item1, item2, item3);

        //itemName 검증
        test("itemA", null, item1, item2);
        test("temA", null, item1, item2);
        test("itemB", null, item3);

        //maxPrice 검증
        test(null, 10000, item1);

        //둘 다 있음 검증
        test("itemA", 10000, item1);
    }

    void test(String itemName, Integer maxPrice, Item... items) {
        List<Item> result = itemQueryRepository.findAll(new ItemSearchCond(itemName, maxPrice));
        assertThat(result).containsExactly(items);
    }

//    @Test
//    @Commit
//    void deleteAll() {
//        itemRepository.deleteAll();
//        Assertions.assertThat(itemRepository.findAll(null).size()).isEqualTo(0);
//    }
}
