# E 커머스 서비스 시퀀스 다이어그램
## 상품
### 상품 상세조회 (/api/v1/products/{productId})
```mermaid
sequenceDiagram
participant U as User
participant PC as ProductController
participant PS as ProductService

	U ->> PC: 상품 조회 요청 (productId)
	PC ->> PS: 상품 조회(productId)
	alt 상품이 없음
        PS -->> PC: 예외 발생
        PC -->> U: 404 Not Found
    else 판매중이 아님
        PS -->> PC: 예외 발생
        PC -->> U: 409 Conflict
    else
        PS -->> PC: 상품 정보 반환
        PC -->> U: 200 OK + 상품 정보
    end
```

### 상품 목록 조회 (/api/v1/products)
```mermaid
sequenceDiagram
participant U as User
participant PC as ProductController
participant PS as ProductService

	U ->> PC: 상품 목록 조회 요청 [categoryId, brandId]
	PC ->> PS: 상품 조회 조회 (필터 조건)
	alt 조회 결과 없음
	    PS -->> PC: 빈 목록
	    PC -->> U: 200 OK + []
    else
        PS -->> PC: 상품 목록 반환
        PC -->> U: 200 OK + 상품 목록
    end
```

## 브랜드
### 브랜드 상세 조회 (/api/v1/brands/{brandId})
```mermaid
sequenceDiagram
participant U as User
participant BC as BrandController
participant BS as BranService

    U ->> BC: 브랜드 조회 요청 (brandId)
    BC ->> BS: 브랜드 조회(brandId)
    alt 브랜드가 없음
        BS -->> BC: 예외 발생
        BC -->> U: 404 Not Found
    else
        BS -->> BC: 브랜드 정보 반환
        BC -->> U: 200 OK + 브랜드 정보
    end
```
## 좋아요
### 좋아요 등록 (/api/v1/products/{productId}/likes)
```mermaid
sequenceDiagram
    participant U as User
    participant PC as ProductController
    participant US as UserService
    participant PS as ProductService    
    participant LS as LikeService

    U ->> PC: 상품 좋아요 요청 (productId)
    PC ->> US: 사용자 인증 확인 (X-USER-ID)
    alt 인증 실패 (사용자 미존재, 헤더 미존재)
        US -->> PC: 예외 발생
        PC -->> U: 401 Unauthorized
    else 인증 성공
        US -->> PC: 사용자 정보 반환
        PC ->> PS: 상품 상태 조회 (productId)
        alt 상품이 없음
            PS -->> PC: 예외 발생
            PC -->> U: 404 Not Found
        else 판매중이 아님
            PS -->> PC: 예외 발생
            PC -->> U: 409 Conflict
        else 상품 존재
            PS -->> PC: 상품 정보 반환
            PC ->> LS: 좋아요 상태 조회(userId, productId)
            alt 좋아요 상태 아님
                LS ->> LS: 좋아요 등록
                LS -->> PC: 등록 결과 반환
            else 이미 좋아요 상태
                LS ->> PS: 등록 상태 유지 (멱등성)
            end
            PC -->> U: 200 OK + 처리 결과
        end
    end
```

### 좋아요 취소 (/api/v1/products/{productId}/likes)
```mermaid
sequenceDiagram
    participant U as User
    participant PC as ProductController
    participant US as UserService
    participant PS as ProductService    
    participant LS as LikeService

    U ->> PC: 상품 좋아요 요청 (productId)
    PC ->> US: 사용자 인증 확인 (X-USER-ID)
    alt 인증 실패 (사용자 미존재, 헤더 미존재)
        US -->> PC: 사용자 없음
        PC -->> U: 401 Unauthorized
    else 인증 성공
        US -->> PC: 사용자 정보 반환
        PC ->> PS: 상품 상태 조회 (productId)
        alt 상품이 없음
            PS -->> PC: 예외 발생
            PC -->> U: 404 Not Found
        else 판매중이 아님
            PS -->> PC: 예외 발생
            PC -->> U: 409 Conflict
        else 상품 존재
            PS -->> PC: 상품 정보 반환
            PC ->> LS: 좋아요 상태 조회(userId, productId)
            alt 좋아요 상태 아님
                LS ->> PC: 예외 발생
                PC -->> U: 204 No Content (멱등성)
            else 좋아요 상태
                LS ->> LS: 좋아요 취소
                LS ->> PC: 취소 결과 반환
                PC -->> U: 200 OK + 처리 결과
            end
        end
    end
```

### 좋아요 조회 (/api/v1/users/{userId}/likes)
```mermaid
sequenceDiagram
    participant U as User
    participant UC as UserController
    participant US as UserService
    participant LS as LikeService

    U ->> UC: 내가 좋아요 한 목록 요청
    UC ->> US: 사용자 인증 확인 (X-USER-ID)
    alt 인증 실패
        US -->> UC: 예외 발생
        UC -->> U: 401 Unauthorized
    else 인증 성공
        US -->> UC: 사용자 정보 반환
        alt 요청 userId와 인증 userId 불일치
            UC -->> U: 403 Forbidden
        else 요청 userId와 인증 userId 일치
            UC ->> LS: 좋아요 한 목록 조회 (userId)
            LS -->> UC: 좋아요 한 목록 반환
            UC -->> U: 200 OK + 좋아요 한 상품 목록
        end
    end
```
## 주문 및 결제
### 주문 요청 (/api/v1/orders)
```mermaid
sequenceDiagram
    participant U as User
    participant OC as OrderController
    participant US as UserService
    participant OS as OrderService
    participant PS as ProductService
    participant PTS as PointService
    participant PG as 외부시스템
    
    U ->> OC: 주문 요청 (X-USER_ID, 상품 목록, 결제 정보)
    OC ->> US: 사용자 인증 확인 (X-USER-ID)
    alt 인증 실패
        US -->> OC: 예외 발생
        OC -->> U: 401 Unauthorized
        else 인증 성공
        US -->> OC: 사용자 정보 반환
        OC ->> OS: 주문 생성 요청(userId, 상품 목록, 결제 정보)
        OS ->> PS: 상품 존재 확인
        alt 상품 없음
            PS -->> OS: 예외 발생
            OS -->> U: 404 Not Found
        else 재고 부족
            PS -->> OS: 예외 발생
            OS -->> U: 409 Conflict
        else
            OS ->> PTS: 포인트 잔액 확인
            alt 포인트 부족
                PTS -->> OS: 예외 발생
                OS -->> U: 409 Conflict
            else
                OS ->> PS: 재고, 포인트 차감
                PS -->> OS: 재고, 포인트 차감 완료
                OS ->> PG: 주문 정보 전송
                PG -->> OS: 전송 성공
                OS -->> OC: 주문 생성 성공(주문ID)
                OC -->> U: 주문 성공 응답
            end
        end
    end
```
### 유저의 주문 목록 조회 (/api/v1/orders)
```mermaid
sequenceDiagram
    participant U as User
    participant OC as OrderController
    participant US as UserService
    participant OS as OrderService

    U ->> OC: 주문 목록 조회 요청
    OC ->> US: 사용자 인증 확인 (X-USER-ID)
    alt 인증 실패
        US -->> OC: 예외 발생
        OC -->> U: 401 Unauthorized
    else 인증 성공
        US -->> OC: 사용자 정보 반환
        OC ->> OS: 주문 목록 조회 (userId)
        OS -->> OC: 주문 목록 반환
        OC -->> U: 200 OK + 주문 목록
        
    end
```
### 단일 주문 상세 조회 (/api/v1/orders/{orderId})
```mermaid
sequenceDiagram
    participant U as User
    participant OC as OrderController
    participant US as UserService
    participant OS as OrderService

    U ->> OC: 단일 주문 상세 조회 요청 (orderId)
    OC ->> US: 사용자 인증 확인 (X-USER-ID)
    alt 인증 실패
        US -->> OC: 예외 발생
        OC -->> U: 401 Unauthorized
    else 인증 성공
        US -->> OC: 사용자 정보 반환
        OC ->> OS: 주문 상세 조회 요청 (orderId)
        OS ->> OS: 주문 존재 확인
        alt 주문이 존재하지 않음
            OS -->> OC: 예외 발생
            OC -->> U: 404 Not Found
        else 주문 존재
            OS -->> OC: 주문 상세 정보 반환
            OC -->> U: 200 OK + 주문 상세 정보

        end
    end
```