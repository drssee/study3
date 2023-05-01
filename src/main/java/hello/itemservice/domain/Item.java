package hello.itemservice.domain;

import lombok.Data;

import javax.persistence.*;

@Entity //JPA가 관리하는 클래스
@Data
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)//IDENTITY = DB에서 pk값 증가
    private Long id;

    @Column(name = "item_name", length = 10)
    private String itemName;
    private Integer price;
    private Integer quantity;

    //JPA는 기본생성자가 필수
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
