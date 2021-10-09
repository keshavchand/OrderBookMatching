package src;

import java.util.Scanner;
//import src.OrderBook;
//import src.OrderBook.Side;

public class Main {
  boolean buy_found = false;
  boolean sell_found = false;
  OrderBook book = new OrderBook();
  Scanner stdin = new Scanner(System.in);
  int quantity = 200;

  public static void main(String args[]) {
	  Main main = new Main();
	  main.run(args);
  }

  void run(String args[]) {

    while(stdin.hasNext()){
      if (args.length >= 1) quantity = Integer.parseInt(args[0]);
      String input[] = stdin.nextLine().split(" ");
      //timestamp A order-id side price size
      //timestamp R order-id size
      int timestamp = Integer.parseInt(input[0]);
      String order_id = input[2];
      OrderBook.Side side = OrderBook.Side.BUY; // may change ??

      if (input[1].equals("A")){
        if (input[3].equals("S")) side = OrderBook.Side.SELL;
        float price = Float.parseFloat(input[4]);
        int size = Integer.parseInt(input[5]);
        side = book.addOrder(timestamp, order_id, side, price, size);
      }else if(input[1].equals("R")){
        int size = Integer.parseInt(input[3]);
        side = book.reduceOrder(order_id, size);
      }

      String s = printOutput(timestamp, side);
      if (s != null) System.out.println(s);
    }
    
    return;
  }

  String printOutput(int timestamp, OrderBook.Side side){
    Float price = null; 
    StringBuilder output_string = new StringBuilder();
    // If quantity is available on BUY side
    // then we sell
    if (side == OrderBook.Side.BUY){
      price = book.findPrice(quantity, book.BuyOrder);
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
    else if (side == OrderBook.Side.SELL){
      price = book.findPrice(quantity, book.SellOrder);
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
