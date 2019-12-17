package com.cloudream.eshop.sync.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "eshop-product-service")
public interface EshopProductService {

	@RequestMapping(value = "/brand/findById", method = RequestMethod.GET)
	String findBrandById(@RequestParam(value = "id") Long id);
	
	@RequestMapping(value = "/brand/findByIds", method = RequestMethod.GET)
	String findBrandByIds(@RequestParam(value = "ids") String ids);

	@RequestMapping(value = "/category/findById", method = RequestMethod.GET)
	String findCategoryById(@RequestParam(value = "id") Long id);

	@RequestMapping(value = "/product/findById", method = RequestMethod.GET)
	String findProductById(@RequestParam(value = "id") Long id);

	@RequestMapping(value = "/productIntro/findById", method = RequestMethod.GET)
	String findProductIntroById(@RequestParam(value = "id") Long id);

	@RequestMapping(value = "/productProperty/findById", method = RequestMethod.GET)
	String findProductPropertyById(@RequestParam(value = "id") Long id);

	@RequestMapping(value = "/productSpecification/findById", method = RequestMethod.GET)
	String findProductSpecificationById(@RequestParam(value = "id") Long id);
}
