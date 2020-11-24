package kitchenpos.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.dto.MenuCreateRequest;
import kitchenpos.dto.MenuProductRequest;
import kitchenpos.dto.MenuProductResponse;
import kitchenpos.dto.MenuResponse;
import kitchenpos.repository.MenuGroupRepository;
import kitchenpos.repository.MenuProductRepository;
import kitchenpos.repository.MenuRepository;
import kitchenpos.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final MenuProductRepository menuProductRepository;
    private final ProductRepository productRepository;

    public MenuService(
            final MenuRepository menuRepository,
            final MenuGroupRepository menuGroupRepository,
            final MenuProductRepository menuProductRepository,
        final ProductRepository productRepository
    ) {
        this.menuRepository = menuRepository;
        this.menuGroupRepository = menuGroupRepository;
        this.menuProductRepository = menuProductRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public MenuResponse create(final MenuCreateRequest menuCreateRequest) {
        validateMenuGroupExist(menuCreateRequest);
        validateSummation(menuCreateRequest);

        final Menu savedMenu = saveMenu(menuCreateRequest);
        List<MenuProductResponse> menuProductResponses
            = createMenuProductResponses(savedMenu, menuCreateRequest);

        return MenuResponse.of(savedMenu, menuProductResponses);
    }

    private void validateMenuGroupExist(MenuCreateRequest menuCreateRequest) {
        if (!menuGroupRepository.existsById(menuCreateRequest.getMenuGroupId())) {
            throw new IllegalArgumentException();
        }
    }

    private void validateSummation(MenuCreateRequest menuCreateRequest) {
        final BigDecimal price = menuCreateRequest.getPrice();
        final List<MenuProductRequest> menuProductsRequest
            = menuCreateRequest.getMenuProductRequests();
        BigDecimal sum = BigDecimal.ZERO;

        for (final MenuProductRequest menuProductRequest : menuProductsRequest) {
            final Product product = productRepository.findById(menuProductRequest.getProductId())
                .orElseThrow(IllegalArgumentException::new);
            sum = sum.add(
                product.getPrice().multiply(BigDecimal.valueOf(menuProductRequest.getQuantity())));
        }

        if (price.compareTo(sum) > 0) {
            throw new IllegalArgumentException();
        }
    }

    private List<MenuProductResponse> createMenuProductResponses(Menu savedMenu,
        MenuCreateRequest menuCreateRequest) {
        final List<MenuProductRequest> menuProductsRequest
            = menuCreateRequest.getMenuProductRequests();
        final List<MenuProduct> savedMenuProducts = new ArrayList<>();

        for (final MenuProductRequest menuProductRequest : menuProductsRequest) {
            Product product = productRepository.getOne(menuProductRequest.getProductId());
            MenuProduct menuProduct = menuProductRequest.toEntity(savedMenu, product);

            savedMenuProducts.add(menuProductRepository.save(menuProduct));
        }

        return MenuProductResponse.toResponseList(savedMenuProducts);
    }

    private Menu saveMenu(MenuCreateRequest menuCreateRequest) {
        MenuGroup menuGroup = menuGroupRepository.getOne(menuCreateRequest.getMenuGroupId());

        return menuRepository.save(menuCreateRequest.toEntity(menuGroup));
    }

    public List<MenuResponse> list() {
        final List<Menu> menus = menuRepository.findAll();
        final List<MenuResponse> menuResponses = new ArrayList<>();

        for (final Menu menu : menus) {
            List<MenuProduct> menuProducts = menuProductRepository.findAllByMenuId(menu.getId());
            List<MenuProductResponse> menuProductResponses
                = MenuProductResponse.toResponseList(menuProducts);

            menuResponses.add(MenuResponse.of(menu, menuProductResponses));
        }

        return menuResponses;
    }
}
