package src;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class OrderBook {
	public enum Side{
		BUY,
		SELL,
	}
	class Order{
		int timestamp;
		String order_id;
		Side side;
		float price;
		int size;
	}

	public TreeMap<Float, ArrayList<Order>> BuyOrder = new TreeMap<>(Collections.reverseOrder());
	public TreeMap<Float, ArrayList<Order>> SellOrder = new TreeMap<>();
  HashMap<String, ArrayList<Order>> SigToOrder = new HashMap<>();
  
  Side addOrder(int timestamp, String order_id, Side side, float price, int size){
    Order order = new Order();
    order.timestamp = timestamp;
    order.order_id = order_id;
    order.side = side;
    order.price = price;
    order.size = size;

    if (side == Side.BUY) {
      ArrayList<Order> orders = BuyOrder.getOrDefault(price, new ArrayList<Order>());
      orders.add(order);
      SigToOrder.put(order_id, orders);
      BuyOrder.put(price, orders);
    }else if (side == Side.SELL) {
      ArrayList<Order> orders = SellOrder.getOrDefault(price, new ArrayList<Order>());
      orders.add(order);
      SigToOrder.put(order_id, orders);
      SellOrder.put(price, orders);
    }

    return side;
  }

  Side reduceOrder(String order_id, int reduce_amt){
    Side side = null;
    ArrayList<Order> orders = SigToOrder.get(order_id);
    if (orders == null) return side;
    for (int i = 0 ; i < orders.size(); i++){
      Order order = orders.get(i);
      if (order == null) continue;
      if (order.order_id.equals(order_id)){
        order.size -= reduce_amt;
        side = order.side;
        // if order side is zero length delete it
        if (order.size > 0) orders.set(i, order);
        else orders.remove(i);
        
        // if arraylist is zero length delete it
        if (order.side == Side.BUY){
          if (orders.size() == 0) BuyOrder.remove(order.price);
        }else if (order.side == Side.SELL){
          if (orders.size() == 0) SellOrder.remove(order.price);
        }
        break;
      }
    }
    return side;
  }

  Float findPrice(int quantity, TreeMap<Float, ArrayList<Order>> orderMap){
    float totalPrice = 0;
    for (Map.Entry<Float, ArrayList<Order>> entry : orderMap.entrySet()) {
      float price = entry.getKey();
      ArrayList<Order> orders = entry.getValue();
      for (Order order : orders){
        int reduced_amt = Math.min(quantity, order.size); 
        quantity -= reduced_amt;
        totalPrice += (reduced_amt * price);
        if (quantity <= 0) return totalPrice;
      }
    }

    return null;
  }
}
