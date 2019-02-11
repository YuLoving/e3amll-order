package cn.e3mall.order.service;

import cn.e3mall.common.utils.E3Result;
import cn.e3mall.order.pojo.OrderInfo;

/**  

* <p>Title: OrderService</p>  

* <p>Description:生成订单页面的逻辑处理 </p>  

* @author 赵天宇

* @date 2019年1月29日  

*/
public interface OrderService {

	//生成订单
	E3Result createorder(OrderInfo orderInfo);
}
