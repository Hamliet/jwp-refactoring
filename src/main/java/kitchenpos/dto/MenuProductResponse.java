package kitchenpos.dto;

import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.domain.MenuProduct;

public class MenuProductResponse {

    private Long seq;
    private Long menuId;
    private Long productId;
    private long quantity;

    public MenuProductResponse(Long seq, Long menuId, Long productId, long quantity) {
        this.seq = seq;
        this.menuId = menuId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public static MenuProductResponse of(MenuProduct menuProduct) {
        Long seq = menuProduct.getSeq();
        Long menuId = menuProduct.getMenu().getId();
        Long productId = menuProduct.getProduct().getId();
        long quantity = menuProduct.getQuantity();

        return new MenuProductResponse(seq, menuId, productId, quantity);
    }

    public static List<MenuProductResponse> toResponseList(List<MenuProduct> savedMenuProducts) {
        return savedMenuProducts.stream()
            .map(mp -> MenuProductResponse.of(mp))
            .collect(Collectors.toList());
    }

    public Long getSeq() {
        return seq;
    }

    public Long getMenuId() {
        return menuId;
    }

    public Long getProductId() {
        return productId;
    }

    public long getQuantity() {
        return quantity;
    }
}