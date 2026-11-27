package se.magnus.microservices.core.product.services;

// ★ 추가 시작 (3줄)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
// ★ 추가 끝
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
// ★ 추가 시작 (2줄)
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;
// ★ 추가 끝
import se.magnus.util.http.ServiceUtil;

@RestController
// ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: public class ProductServiceImpl implements ProductService{
public class ProductServiceImpl implements ProductService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
// ★ 변경 끝

  private final ServiceUtil serviceUtil;

  // ★ 변경 시작 (전 1줄 → 5줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public ProductServiceImpl(ServiceUtil serviceUtil){
  private final ProductRepository repository;

  private final ProductMapper mapper;

  public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
    this.repository = repository;
    this.mapper = mapper;
  // ★ 변경 끝
    this.serviceUtil = serviceUtil;
  }

  // ★ 추가 시작 (11줄)
  @Override
  public Product createProduct(Product body) {
    try {
      ProductEntity entity = mapper.apiToEntity(body);
      ProductEntity newEntity = repository.save(entity);

      LOG.debug("createProduct: entity created for productId: {}", body.getProductId());
      return mapper.entityToApi(newEntity);

    } catch (DuplicateKeyException dke) {
      throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
    }
  }
  // ★ 추가 끝

  @Override
  public Product getProduct(int productId) {

    // ★ 변경 시작 (전 3줄 → 14줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
    // ★ 전: if (productId == 13) throw new NotFoundException("No product found for productId: " + productId);
    // ★ 전: return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    ProductEntity entity = repository.findByProductId(productId)
      .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

    Product response = mapper.entityToApi(entity);
    response.setServiceAddress(serviceUtil.getServiceAddress());

    LOG.debug("getProduct: found productId: {}", response.getProductId());

    return response;
  }

  @Override
  public void deleteProduct(int productId) {
    LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
    repository.findByProductId(productId).ifPresent(e -> repository.delete(e));
    // ★ 변경 끝
  }
}