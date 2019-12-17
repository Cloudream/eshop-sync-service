package com.cloudream.eshop.sync.rabbitmq;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.cloudream.eshop.sync.constant.OperationConstant;
import com.cloudream.eshop.sync.constant.RabbitQueueConstant;
import com.cloudream.eshop.sync.feign.EshopProductService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 数据同步服务，就是获取各种原子数据变更消息 1、通过Spring cloud feign调用eshop-product-service的各种接口，获取数据
 * 2、将原子数据在redis中进行增删改 3、将维度数据变化消息写入rabbitmq中的另外一个queue,供聚合服务来消费
 * 
 * @author Administrator
 *
 */
@Component
@RabbitListener(queues = "high-priority-data-change-queue")
public class HighPriorityRabbitMQReceiver {
	@Autowired
	private EshopProductService eshopProductService;

	@Autowired
	private JedisPool jedisPool;

	@Autowired
	private RabbitMQSender rabbitMQSender;

	private static Set<String> dimDataChangeMessageSet = Collections.synchronizedSet(new HashSet<String>());
	
	public HighPriorityRabbitMQReceiver() {
		new Thread(new SendThead()).start();
	}
	
	@RabbitHandler
	public void process(String message) {
		System.out.println("从data-change-queue接收一条消息," + message);
		JSONObject jsonObject = JSONObject.parseObject(message);
		String dataType = jsonObject.getString("data_type");
		if (OperationConstant.BRAND_SERVICE.equals(dataType)) {
			processBrandSyncChangeMessage(jsonObject);
		} else if (OperationConstant.CATEGORY_SERVICE.equals(dataType)) {
			processCategorySyncChangeMessage(jsonObject);
		} else if (OperationConstant.PRODUCT_SERVICE.equals(dataType)) {
			processProductSyncChangeMessage(jsonObject);
		} else if (OperationConstant.PRODUCTINTRO_SERVICE.equals(dataType)) {
			processProductIntroSyncChangeMessage(jsonObject);
		} else if (OperationConstant.PRODUCTPROPERTY_SERVICE.equals(dataType)) {
			processProductPropertySyncChangeMessage(jsonObject);
		} else if (OperationConstant.PRODUCTSPECIFICATION_SERVICE.equals(dataType)) {
			processProductSpecificationSyncChangeMessage(jsonObject);
		}
	}

	private void processBrandSyncChangeMessage(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		String eventType = messageJSONObject.getString("event_type");
		if (OperationConstant.ADD.equals(eventType) || OperationConstant.UPDATE.equals(eventType)) {
			JSONObject dataJSON = JSONObject.parseObject(eshopProductService.findBrandById(id));
			Jedis jedis = jedisPool.getResource();
			jedis.set("brand_" + dataJSON.getLong("id"), dataJSON.toJSONString());
		} else if (OperationConstant.DELETE.equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("brand_" + id);
		}
		//rabbitMQSender.send(RabbitQueueConstant.DATA_SYNC_CHANGE_QUEUE, "{\"dim_type\":\"brand\",\"id\":" + id + "}");
		dimDataChangeMessageSet.add("{\"dim_type\":\"brand\",\"id\":" + id + "}");
		System.out.println("[品牌数据被放入本地Set中],brandId= " + id);
	}

	private void processCategorySyncChangeMessage(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		String eventType = messageJSONObject.getString("event_type");
		if (OperationConstant.ADD.equals(eventType) || OperationConstant.UPDATE.equals(eventType)) {
			JSONObject dataJSON = JSONObject.parseObject(eshopProductService.findCategoryById(id));
			Jedis jedis = jedisPool.getResource();
			jedis.set("category_" + dataJSON.getLong("id"), dataJSON.toJSONString());
		} else if (OperationConstant.DELETE.equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("category_" + id);
		}
		//rabbitMQSender.send(RabbitQueueConstant.DATA_SYNC_CHANGE_QUEUE,"{\"dim_type\":\"category\",\"id\":" + id + "}");
		dimDataChangeMessageSet.add("{\"dim_type\":\"category\",\"id\":" + id + "}");
	}

	private void processProductSyncChangeMessage(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		String eventType = messageJSONObject.getString("event_type");
		if (OperationConstant.ADD.equals(eventType) || OperationConstant.UPDATE.equals(eventType)) {
			JSONObject dataJSON = JSONObject.parseObject(eshopProductService.findProductById(id));
			Jedis jedis = jedisPool.getResource();
			jedis.set("product_" + id, dataJSON.toJSONString());
		} else if (OperationConstant.DELETE.equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("product_" + id);
		}
		//rabbitMQSender.send(RabbitQueueConstant.DATA_SYNC_CHANGE_QUEUE, "{\"dim_type\":\"product\",\"id\":" + id + "}");
		dimDataChangeMessageSet.add("{\"dim_type\":\"product\",\"id\":" + id + "}");
	}

	private void processProductPropertySyncChangeMessage(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		Long productId = messageJSONObject.getLong("product_id");
		String eventType = messageJSONObject.getString("event_type");
		if (OperationConstant.ADD.equals(eventType) || OperationConstant.UPDATE.equals(eventType)) {
			JSONObject dataJSON = JSONObject.parseObject(eshopProductService.findProductPropertyById(id));
			Jedis jedis = jedisPool.getResource();
			jedis.set("productProperty_" + productId, dataJSON.toJSONString());
		} else if (OperationConstant.DELETE.equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("productProperty_" + productId);
		}
		//rabbitMQSender.send(RabbitQueueConstant.DATA_SYNC_CHANGE_QUEUE,"{\"dim_type\":\"product\",\"id\":" + productId + "}");
		dimDataChangeMessageSet.add("{\"dim_type\":\"product\",\"id\":" + productId + "}");
	}

	private void processProductSpecificationSyncChangeMessage(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		Long productId = messageJSONObject.getLong("product_id");
		String eventType = messageJSONObject.getString("event_type");
		if (OperationConstant.ADD.equals(eventType) || OperationConstant.UPDATE.equals(eventType)) {
			JSONObject dataJSON = JSONObject.parseObject(eshopProductService.findProductSpecificationById(id));
			Jedis jedis = jedisPool.getResource();
			jedis.set("productSpecification_" + productId, dataJSON.toJSONString());
		} else if (OperationConstant.DELETE.equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("productSpecification_" + productId);
		}
		//rabbitMQSender.send(RabbitQueueConstant.DATA_SYNC_CHANGE_QUEUE,"{\"dim_type\":\"product\",\"id\":" + productId + "}");
		dimDataChangeMessageSet.add("{\"dim_type\":\"product\",\"id\":" + productId + "}");
	}

	private void processProductIntroSyncChangeMessage(JSONObject messageJSONObject) {
		Long id = messageJSONObject.getLong("id");
		Long productId = messageJSONObject.getLong("product_id");
		String eventType = messageJSONObject.getString("event_type");
		if (OperationConstant.ADD.equals(eventType) || OperationConstant.UPDATE.equals(eventType)) {
			JSONObject dataJSON = JSONObject.parseObject(eshopProductService.findProductIntroById(id));
			Jedis jedis = jedisPool.getResource();
			jedis.set("productIntro_" + productId, dataJSON.toJSONString());
		} else if (OperationConstant.DELETE.equals(eventType)) {
			Jedis jedis = jedisPool.getResource();
			jedis.del("productIntro_" + productId);
		}
		//rabbitMQSender.send(RabbitQueueConstant.DATA_SYNC_CHANGE_QUEUE,"{\"dim_type\":\"productIntro\",\"id\":" + productId + "}");
		dimDataChangeMessageSet.add("{\"dim_type\":\"productIntro\",\"id\":" + productId + "}");
	}
	
	private class SendThead implements Runnable {
		@Override
		public void run() {
			while (true) {
				if (!dimDataChangeMessageSet.isEmpty()) {
					for (String message : dimDataChangeMessageSet) {
						rabbitMQSender.send(RabbitQueueConstant.HIGH_PRIORITY_AGGR_DATA_CHANGE_QUEUE, message);
						System.out.println("[将去重后的维度变更消息发送到下一个queue],message = " + message);
					}
					dimDataChangeMessageSet.clear();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
