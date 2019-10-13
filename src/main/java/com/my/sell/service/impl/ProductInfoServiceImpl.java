package com.my.sell.service.impl;

import com.my.sell.dto.CartDTO;
import com.my.sell.enums.ProductStatusEnum;
import com.my.sell.enums.ResultEnum;
import com.my.sell.exception.SellException;
import com.my.sell.model.ProductInfo;
import com.my.sell.repository.ProductInfoRepository;
import com.my.sell.service.CacheService;
import com.my.sell.service.ProductInfoService;
import com.my.sell.utils.IdWorker;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service(value = "productInfoService")
@Transactional
@Slf4j
public class ProductInfoServiceImpl implements ProductInfoService {

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Autowired
    IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    private static String CACHE_PREFIX = "PRODUCT:";

    @Override
    @HystrixCommand(
            fallbackMethod = "findOneFallback",
            threadPoolProperties = {  //10个核心线程池,超过20个的队列外的请求被拒绝; 当一切都是正常的时候，线程池一般仅会有1到2个线程激活来提供服务
                    @HystrixProperty(name = "coreSize", value = "10"),
                    @HystrixProperty(name = "maxQueueSize", value = "100"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "20")},
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10000"), //命令执行超时时间
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "2"), //若干10s一个窗口内失败三次, 则达到触发熔断的最少请求量
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "30000") //断路30s后尝试执行, 默认为5s
            })
    public ProductInfo findOne(String productId) {
        //throw new RuntimeException("我就是要错");
        return productInfoRepository.findById(productId).orElse(null);
    }

    public ProductInfo findOneFallback(String productId) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductId("11111");
        productInfo.setProductName("降级商品");
        return productInfo;
    }

    @Override
    public List<ProductInfo> findAll() {
        return productInfoRepository.findAll();
    }

    @Override
    public Page<ProductInfo> findAll(Pageable pageable) {
        return productInfoRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public ProductInfo save(ProductInfo productInfo) {
        return productInfoRepository.save(productInfo);
    }

    @Override
    public ProductInfo onSale(String productId) {
        ProductInfo productInfo = productInfoRepository.findById(productId).orElse(null);
        if (productInfo == null) {
            throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
        }
        //如果已上架
        if (productInfo.getProductStatus().equals(ProductStatusEnum.UP.getCode())) {
            throw new SellException(ResultEnum.PRODUCT_STATUS_ERROR);
        }

        productInfo.setProductStatus(ProductStatusEnum.UP.getCode());
        return productInfoRepository.save(productInfo);
    }

    @Override
    public ProductInfo offSale(String productId) {
        ProductInfo productInfo = productInfoRepository.findById(productId).orElse(null);
        if (productInfo == null) {
            throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
        }
        //如果已下架
        if (productInfo.getProductStatus().equals(ProductStatusEnum.DOWN.getCode())) {
            throw new SellException(ResultEnum.PRODUCT_STATUS_ERROR);
        }
        productInfo.setProductStatus(ProductStatusEnum.DOWN.getCode());
        return productInfoRepository.save(productInfo);
    }

    @Override
    public List<ProductInfo> findUpAll() {
        return productInfoRepository.
                findProductInfosByProductStatus(ProductStatusEnum.UP.getCode());
    }

    @Override
    @Transactional
    public void increaseProductStock(List<CartDTO> cartDTOList) {
        for (CartDTO cartDTO : cartDTOList) {
            String productId = cartDTO.getProductId();
            Optional<ProductInfo> optional = productInfoRepository.findById(productId);
            if(optional.isPresent()){
                ProductInfo productInfo = optional.get();
                productInfo.setProductStock(
                        productInfo.getProductStock()+cartDTO.getProductQuantity());
                productInfoRepository.save(productInfo);
                log.info("更新商品库存product:{},增加了{}",productId,cartDTO.getProductQuantity());
            }else{
                throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
            }
        }
    }

    @Override
    @Transactional
    public void decreaseProductStock(List<CartDTO> cartDTOList) {
        for (CartDTO cartDTO : cartDTOList) {
            String productId = cartDTO.getProductId();
            Optional<ProductInfo> optional = productInfoRepository.findById(productId);
            if(optional.isPresent()){
                ProductInfo productInfo = optional.get();
                int productStock = productInfo.getProductStock() - cartDTO.getProductQuantity();
                if (productStock < 0) {
                    throw new SellException(ResultEnum.PRODUCT_STOCK_ERROR);
                }
                productInfo.setProductStock(
                        productStock);
                productInfoRepository.save(productInfo);
                log.info("更新商品库存product:{},减少了{}",productId,cartDTO.getProductQuantity());
            }else{
                log.error("商品不存在");
                throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeCache(String productId) {
        redisTemplate.delete(CACHE_PREFIX + productId);
    }

    @Override
    public ProductInfo findOneInRedis(String productId) {
        log.info("productId{}",productId);
        ProductInfo productInfo = (ProductInfo) redisTemplate.opsForValue().get(CACHE_PREFIX + productId);
        if(productInfo!=null){
            log.info("从redis中拿,{}",productId);
            return productInfo;
        }
        log.info("拿不到");
        return null;
    }

    @Override
    public ProductInfo findOneInCache(String productId) {

        //从redis中查找
        ProductInfo productInfo = (ProductInfo) redisTemplate.opsForValue().get(CACHE_PREFIX + productId);
        if(productInfo!=null){
            log.info("从redis中拿,{}",productId);
            return productInfo;
        }
        else{
            if (cacheService.getProductInfoFromLocalCache(productId) != null) {
                log.info("从ehcache中拿,{}",productId);
                return cacheService.getProductInfoFromLocalCache(productId);
            }else{
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setCache(ProductInfo productInfo) {
        log.info("设置了redis和ehcache,{}",productInfo.getProductId());
        redisTemplate.opsForValue().set(CACHE_PREFIX+productInfo.getProductId(),productInfo);
        cacheService.saveProductInfo2LocalCache(productInfo);
    }
}
