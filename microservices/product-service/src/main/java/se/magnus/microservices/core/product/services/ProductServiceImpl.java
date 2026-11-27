package se.magnus.microservices.core.product.services;

import static java.util.logging.Level.FINE;  // ★ 추가

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;  // ★ 추가
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;
import se.magnus.util.http.ServiceUtil;

@RestController
public class ProductServiceImpl implements ProductService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

  private final ServiceUtil serviceUtil;

  private final ProductRepository repository;

  private final ProductMapper mapper;

  public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
  }

  @Override
  // ★ 변경 시작 (전 2줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public Product createProduct(Product body) {
  // ★ 전: try {
  public Mono<Product> createProduct(Product body) {

    if (body.getProductId() < 1) {
      throw new InvalidInputException("Invalid productId: " + body.getProductId());
    }
  // ★ 변경 끝

    ProductEntity entity = mapper.apiToEntity(body);
    // ★ 변경 시작 (전 1줄 → 6줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: ProductEntity newEntity = repository.save(entity);
    Mono<Product> newEntity = repository.save(entity)
      .log(LOG.getName(), FINE)
      .onErrorMap(
        DuplicateKeyException.class,
        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
      .map(e -> mapper.entityToApi(e));
    // ★ 변경 끝

    // ★ 변경 시작 (전 5줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: LOG.debug("createProduct: entity created for productId: {}", body.getProductId());
    // ★ 전: return mapper.entityToApi(newEntity);
    // ★ 전: } catch (DuplicateKeyException dke) {
    // ★ 전: throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
    // ★ 전: }
    return newEntity;
    // ★ 변경 끝
  }

  @Override
  public Mono<Product> getProduct(int productId) {  // ★ 변경 (전: public Product getProduct(int productId) {)

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    // ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: ProductEntity entity = repository.findByProductId(productId)
    // ★ 전: .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));
    LOG.info("Will get product info for id={}", productId);
    // ★ 변경 끝

    // ★ 변경 시작 (전 4줄 → 5줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: Product response = mapper.entityToApi(entity);
    // ★ 전: response.setServiceAddress(serviceUtil.getServiceAddress());
    // ★ 전: LOG.debug("getProduct: found productId: {}", response.getProductId());
    // ★ 전: return response;
    return repository.findByProductId(productId)
      .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
      .log(LOG.getName(), FINE)
      .map(e -> mapper.entityToApi(e))
      .map(e -> setServiceAddress(e));
    // ★ 변경 끝
  }

  @Override
  // ★ 변경 시작 (전 1줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public void deleteProduct(int productId) {
  public Mono<Void> deleteProduct(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }
  // ★ 변경 끝

    LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
    // ★ 변경 시작 (전 1줄 → 5줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: repository.findByProductId(productId).ifPresent(e -> repository.delete(e));
    return repository.findByProductId(productId).log(LOG.getName(), FINE).map(e -> repository.delete(e)).flatMap(e -> e);
  }

  private Product setServiceAddress(Product e) {
    e.setServiceAddress(serviceUtil.getServiceAddress());
    return e;
    // ★ 변경 끝
  }
}