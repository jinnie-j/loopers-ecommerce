# E 커머스 서비스 클래스 다이어그램

```mermaid
classDiagram
    class User {
        Long id
        String name
        +like(product: Product): void
        +unlike(product: Product): void
    }
    class Point {
        Long id
        Long balance
        +addPoints(amount: Long): void
        +usePoints(amount: Long): boolean
    }
    class Product {
        Long id
        String name
        Long price
        Long stock
        +decreaseStock(amount: Long): void
        +increaseStock(amount: Long): void
    }

    class Brand {
        Long id
        String name
    }

    class Like {
        User user
        Product product
    }

    class Order {
        Long id
        User user
        List orderItems
        Long totalAmount
        +addOrderItem(product: Product, quantity: Long)
        +calculateTotalAmount(): Long
    }

    class OrderItem{
        Product product
        Long quantity
        Long price
        +calculatePrice(): Long
    }
    
    class Payment{
        Long id
        Order order
        +process(): void
    }

    %% Associations
    User "1" --> "1" Point : 포인트 보유
    User "1" --> "N" Like : 좋아요
    Product "1" --> "N" Like : 좋아요 보유
    User "1" --> "N" Order : 주문
    Order "1" --> "N" OrderItem : 포함
    OrderItem "N" --> "1" Product : 주문한 상품
    Brand "1" --> "N" Product : 소속 브랜드
    Order "1" --> "1" Payment : 결제정보
```