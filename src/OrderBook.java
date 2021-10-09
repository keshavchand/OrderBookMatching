package src;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map;

import java.util.concurrent.ConcurrentLinkedQueue;

public class OrderBook extends Thread{
	public enum Side{
		BUY,
		SELL,
	}
	enum Action{
		ADD,
		REMOVE,
	}

	static public class IncomingOrder{
		int timestamp;
    Action action;
		String order_id;
		Side side;
		float price;
		int size;

    IncomingOrder(int timestamp, String order_id, Side side, float price, int size){
		  this.timestamp = timestamp;
      this.action = Action.ADD;
      this.order_id = order_id;
      this.side = side;
		  this.price = price;
		  this.size = size;
    }
    IncomingOrder(int timestamp, String order_id, int size){
		  this.timestamp = timestamp;
      this.action = Action.REMOVE;
      this.order_id = order_id;
		  this.size = size;
    }
	}
	class Order{
		int timestamp;
		String order_id;
		Side side;
		float price;
		int size;
	}

	TreeMap<Float, ArrayList<Order>> BuyOrder = new TreeMap<>(Collections.reverseOrder());
	TreeMap<Float, ArrayList<Order>> SellOrder = new TreeMap<>();
  HashMap<String, ArrayList<Order>> SigToOrder = new HashMap<>();
  ConcurrentLinkedQueue<IncomingOrder> iorder = null;
  ConcurrentLinkedQueue<String> output = null;

  public OrderBook(ConcurrentLinkedQueue<IncomingOrder> iorder, ConcurrentLinkedQueue<String> output, int quantity){
    this.iorder = iorder;
    this.output = output;
    this.quantity = quantity;
  }

  @Override
  public void run(){

    while (true){
      IncomingOrder io = iorder.poll();
      Side side = null;
      int timestamp = 0;
      if (io != null){
        timestamp = io.timestamp;
        if (io.action == Action.ADD)
          side = addOrder(io.timestamp, io.order_id, io.side, io.price, io.size);
        else if (io.action == Action.REMOVE)
          side = reduceOrder(io.order_id, io.size);
      }
      
      if (side != null){
        String s = printOutput(timestamp, side);
        if (s != null)
          output.add(s);
      }

    }
  }

  private Side addOrder(int timestamp, String order_id, Side side, float price, int size){
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

  private Side reduceOrder(String order_id, int reduce_amt){
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

  private Float findPrice(int quantity, TreeMap<Float, ArrayList<Order>> orderMap){
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

  boolean buy_found = false;
  boolean sell_found = false;
  int quantity = 200;
  String printOutput(int timestamp, Side side){
    Float price = null; 
    StringBuilder output_string = new StringBuilder();
    // If quantity is available on BUY side
    // then we sell
    if (side == Side.BUY){
      price = findPrice(quantity, BuyOrder);
      if (price != null){
        buy_found = true;
        output_string.append(timestamp);
        output_string.append(" ").append("S");
        output_string.append(" ").append(price);
      }else if (buy_found){
        buy_found = false;
        output_string.append(timestamp);
        output_string.append(" ").append("S");
        output_string.append(" ").append("NA");
      }
    }
    // If quantity is available on SELL side
    // then we BUY
    else if (side == Side.SELL){
      price = findPrice(quantity, SellOrder);
      if (price != null){
        sell_found = true;
        output_string.append(timestamp);
        output_string.append(" ").append("B");
        output_string.append(" ").append(price);
      }else if (sell_found){
        sell_found = false;
        output_string.append(timestamp);
        output_string.append(" ").append("B");
        output_string.append(" ").append("NA");
      }
    }

    if (output_string.length() != 0)
      return output_string.toString();
    return null;
  }
}
