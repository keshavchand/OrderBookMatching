package src;

import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
//import src.OrderBook;
//import src.OrderBook.Side;

public class OrderBookMatching {
  ConcurrentLinkedQueue<OrderBook.IncomingOrder> iorder = null;
  ConcurrentLinkedQueue<String> output = null;

  OrderBook book = null;
  Scanner stdin = new Scanner(System.in);
  int quantity = 200;

  public static void main(String args[]) {
	  OrderBookMatching main = new OrderBookMatching();
	  main.run(args);
  }

  void run(String args[]) {
    iorder = new ConcurrentLinkedQueue<OrderBook.IncomingOrder>();
    output = new ConcurrentLinkedQueue<String>();
    book = new OrderBook(iorder, output, quantity);
    book.start();

    if (args.length >= 1) quantity = Integer.parseInt(args[0]);
    while(true){
      String input[] = readInput();

      if (input != null){
        //timestamp A order-id side price size
        //timestamp R order-id size
        int timestamp = Integer.parseInt(input[0]);
        String order_id = input[2];
        OrderBook.Side side = OrderBook.Side.BUY; // may change ??

        if (input[1].equals("A")){
          if (input[3].equals("S")) side = OrderBook.Side.SELL;
          float price = Float.parseFloat(input[4]);
          int size = Integer.parseInt(input[5]);
          OrderBook.IncomingOrder incoming = 
            new OrderBook.IncomingOrder(timestamp, order_id, side, price, size);
          iorder.add(incoming);
        }else if(input[1].equals("R")){
          int size = Integer.parseInt(input[3]);
          OrderBook.IncomingOrder incoming =
            new OrderBook.IncomingOrder(timestamp, order_id, size);
          iorder.add(incoming);
        }
      }
      writeOutput();
    }
    
  }

  String[] readInput(){
    String input[] = null;
    if (stdin.hasNext()) input = stdin.nextLine().split(" ");
    return input;
  }

  boolean writeOutput(){
      String s = output.poll();
      if (s != null) System.out.println(s);
      if (s != null) return true;

      return false;
  }

}
