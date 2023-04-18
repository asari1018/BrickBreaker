package para;

import java.util.Scanner;
import java.util.List;
import java.net.*;
import java.io.*;
import para.graphic.target.*;
import para.graphic.opencl.*;
import para.graphic.shape.*;
import para.graphic.parser.*;
import para.game.*;

public class Game04 extends GameFrame{
  TargetImageFilter inputside;
  final Target outputside;
  volatile Thread thread;
  InputStream istream;
  OutputStream ostream;
  ShapeManager osm;
  ShapeManager ism;
  volatile Target target;
  String serveraddress;
  static final int WIDTH=700;
  static final int HEIGHT=700;
  
  public Game04(){
    super(new JavaFXCanvasTarget(WIDTH, HEIGHT));
    title="Game04";
    outputside = canvas;
    osm = new OrderedShapeManager();
    ism = new ShapeManager();
  }

  public void init(){
    List<String> params = getParameters().getRaw();
    if (params.size()!=0){
      serveraddress = params.get(0);
    }else{
      serveraddress = "localhost";
    }
  }
  
  public void gamestart(int v){
    if(thread != null){
      return;
    }
    try{
      Socket socket;
      socket = new Socket(serveraddress, para.game.GameServerFrame.PORTNO);
      istream = socket.getInputStream();
      ostream = socket.getOutputStream();
      //inputside = new TargetImageFilter(new TextTarget(WIDTH, HEIGHT, ostream),
                                        //this, "imagefilter.cl", "Filter2" );
      inputside = new TargetImageFilter(new TextTarget(WIDTH, HEIGHT, ostream));
    }catch(IOException ex){
      System.err.print("To:"+serveraddress+" ");
      System.err.println(ex);
      System.exit(0);
    }
    
    /* ユーザ入力をサーバに送信するスレッド */
    thread = new Thread(()->{
        int x=150;
        Attribute attr = new Attribute(200,128,128);

        ism.put(new Rectangle(v+1, x,30*v+225,100-lev*15,20,attr));
        inputside.draw(ism);
        ism.put(new Camera(0, 0, 300,attr));
        target = new TargetRecorder("game1911212", outputside);
        target.init();
        target.clear();
        inputside.draw(ism);
        while(true){
          synchronized(target){
            target.clear();
            target.draw(ism);
            target.flush();
          }
          try{
            Thread.sleep(80);
          }catch(InterruptedException ex){
            thread = null;
            break;
          }
          if((lefton ==1 || righton ==1)){
            x = x-4*lefton+4*righton;
            ism.put(new Rectangle(v+1, x,30*v+225,100-lev*15,20,attr));
          }
          if(state == 1){
            inputside = new TargetImageFilter(new TextTarget(WIDTH, HEIGHT, ostream),
                                        this, "loser.cl", "Filter" );
          }
          
          inputside.setParameter(gamerstate);
          inputside.draw(ism);
        }
      },"UserInput");
    thread.start();


    /* 受信したデータを画面に出力するスレッド */
    Thread thread2 = new Thread(()->{
        GameMainParser parser = new GameMainParser(this, outputside, osm);
        BufferedReader br = new BufferedReader(new InputStreamReader(istream));
        parser.parse(new Scanner(istream));//loop
        System.out.println("connection closed");
        thread.interrupt();
      },"UserOutput");
    thread2.start();
  }
}
