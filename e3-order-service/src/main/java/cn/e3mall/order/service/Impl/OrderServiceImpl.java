package cn.e3mall.order.service.Impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.mapper.TbOrderItemMapper;
import cn.e3mall.mapper.TbOrderMapper;
import cn.e3mall.mapper.TbOrderShippingMapper;
import cn.e3mall.order.pojo.OrderInfo;
import cn.e3mall.order.service.OrderService;
import cn.e3mall.pojo.TbOrder;
import cn.e3mall.pojo.TbOrderItem;
import cn.e3mall.pojo.TbOrderShipping;

/**  

* <p>Title: OrderServiceImpl</p>  

* <p>Description: </p>  

* @author 赵天宇

* @date 2019年1月29日  

*/
@Service
public class OrderServiceImpl implements OrderService {
	
	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	
	@Autowired
	private TbOrderShippingMapper orderShippingMapper;
	
	@Autowired
	private JedisClient jedisClient;
	
	@Value("${ORDER_ID_GEN_KEY}")
	private String ORDER_ID_GEN_KEY;
	
	@Value("${ORDER_ID_START}")  //redis中存的都是字符串，所以可以直接使用String
	private String ORDER_ID_START;
	
	@Value("${ORDER_DETAIL_ID_GEN_KEY}")
	private String ORDER_DETAIL_ID_GEN_KEY;
	
	/**
	 * 生成订单处理
	 */
	@Override
	public E3Result createorder(OrderInfo orderInfo) {
		//如果订单的key不存在，则要设置初始值
		if(!jedisClient.exists(ORDER_ID_GEN_KEY)){
			jedisClient.set(ORDER_ID_GEN_KEY, ORDER_ID_START);
			}
		// 生成订单号，（必须是唯一），使用Redis的incr来生成(自增)
		String orderId=jedisClient.incr(ORDER_ID_GEN_KEY).toString();
		// 补全orderInfo的属性
			orderInfo.setOrderId(orderId);
			//状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭
			orderInfo.setStatus(1); //新生成的订单一开始肯定是没有付款的。
			orderInfo.setCreateTime(new Date());
			orderInfo.setUpdateTime(new Date());
		// 插入订单表
			orderMapper.insert(orderInfo);
		// 向订单明细表插入数据
			List<TbOrderItem> orderItems = orderInfo.getOrderItems();
			for (TbOrderItem tbOrderItem : orderItems) {
				//生成明细表的id 
			String id = jedisClient.incr(ORDER_DETAIL_ID_GEN_KEY).toString();
				//补全明细表的属性
			tbOrderItem.setId(id);
			tbOrderItem.setOrderId(orderId);
				//向明细表插入数据
			orderItemMapper.insert(tbOrderItem);
			}
		// 向订单物流表插入数据
			TbOrderShipping orderShipping = orderInfo.getOrderShipping();
			//补全物流表
			orderShipping.setOrderId(orderId); 
			orderShipping.setCreated(new Date());
			orderShipping.setUpdated(new Date());
			orderShippingMapper.insert(orderShipping);
		// 返回E3Result，包含订单号
		return E3Result.ok(orderId);
	}

}
