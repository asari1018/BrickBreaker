package para;
import java.io.IOException;
import java.util.stream.IntStream;

import para.graphic.shape.Rectangle;
import para.graphic.shape.Attribute;
import para.graphic.shape.ShapeManager;
import para.graphic.shape.OrderedShapeManager;
import para.graphic.shape.Vec2;
import para.graphic.shape.MathUtil;
import para.graphic.shape.Shape;
import para.graphic.shape.Circle;
import para.graphic.shape.Digit;
import para.graphic.shape.CollisionChecker;
import para.graphic.target.TranslateTarget;
import para.graphic.target.TranslationRule;
import para.game.GameServerFrame;
import para.game.GameInputThread;
import para.game.GameTextTarget;

public class GameServer01{
  final Attribute wallattr = new Attribute(250,230,200,true,0,0,0);
  final Attribute outattr = new Attribute(250,0,0,true,0,0,0);
  final Attribute ballattr = new Attribute(250,120,120,true,0,0,0);
  final Attribute scoreattr = new Attribute(60,60,60,true,0,0,0);
  final int MAXCONNECTION=2;
  final GameServerFrame gsf;
  final ShapeManager[] userinput;
  final ShapeManager[] wall;
  final ShapeManager[] outwall;
  final ShapeManager[] blocks;
  final ShapeManager[] ballandscore;
  final Vec2[] pos;
  final Vec2[] vel;
  final int[] score;
  final CollisionChecker checker;
  boolean[] endflags;



  private GameServer01(){
    checker = new CollisionChecker();
    gsf = new GameServerFrame(MAXCONNECTION);
    userinput = new ShapeManager[MAXCONNECTION];
    wall = new OrderedShapeManager[MAXCONNECTION];
    outwall = new OrderedShapeManager[MAXCONNECTION];
    blocks = new OrderedShapeManager[MAXCONNECTION];
    ballandscore = new ShapeManager[MAXCONNECTION];
    pos = new Vec2[MAXCONNECTION];
    vel = new Vec2[MAXCONNECTION];
    score = new int[MAXCONNECTION];
    endflags = new boolean[MAXCONNECTION];
    for(int i=0;i<userinput.length;i++){
      userinput[i] = new ShapeManager();
      ballandscore[i] = new ShapeManager();
      wall[i] = new OrderedShapeManager();
      outwall[i] = new OrderedShapeManager();
      blocks[i] = new OrderedShapeManager();
      pos[i] = new Vec2(i*350+150,200);
      vel[i] = new Vec2(0,0);
      endflags[i] = false;

    }
  }

  public void start(){
    try{
      gsf.init();
    }catch(IOException ex){
      System.err.println(ex);
    }
    gsf.welcome();
    int gs=0;
    int startflag = 0;
    GameInputThread git1 = null;
    boolean endflag = false;
    while(!endflag){
      gs = (gs+1)%350;
      GameInputThread git = gsf.queue.poll();
      if(git != null){
        int id = git.getUserID();
        init(id);
        startflag++;
        if(startflag == 1){
          git1 = git;
        }
        if(startflag == MAXCONNECTION){
          startReceiver(git1);
          startReceiver(git);
          for(int i=0;i<MAXCONNECTION;i++){
            GameTextTarget out = gsf.getUserOutput(i);
            distributeOutput(out);
          }
          System.out.println("sleep");
          //wait for camera and board
          try{
            Thread.sleep(15000);
          }catch(InterruptedException ex){
          }
        }
      }
      try{
        Thread.sleep(100);
      }catch(InterruptedException ex){
      }
      if(startflag == MAXCONNECTION){
        for(int i=0;i<MAXCONNECTION;i++){
          GameTextTarget out = gsf.getUserOutput(i);
          if(out != null){
            calcForOneUser(i);
            ballandscore[i].put(new Circle(i*10000+1, (int)pos[i].data[0],
                                    (int)pos[i].data[1], 5, ballattr));
            putScore(i,score[i]);
            out.gamerstate(gs); //Gamerの状態をクライアントに伝える
            distributeOutput(out);
          }
        }
      }
      endflag = (endflags[0] && endflags[1]);
    }
    
    if(score[0]<score[1]){
      System.out.println("1P lose");
      GameTextTarget out = gsf.getUserOutput(0);
      if(out != null){
        out.state(1);
      }
    }else if(score[0]>score[1]){
      System.out.println("2P lose");
      GameTextTarget out = gsf.getUserOutput(1);
      if(out != null){
        out.state(1);
      }
    }else{
      for(int k=0;k<MAXCONNECTION;k++){
        GameTextTarget out = gsf.getUserOutput(0);
        if(out != null){
          out.state(1);
        }
      }
    }

    int rlt = 0;
    //result time
    while(rlt < 50){
      gs = (gs+1)%350;
      try{
        Thread.sleep(100);
      }catch(InterruptedException ex){
      }
      if(startflag == MAXCONNECTION){
        for(int i=0;i<MAXCONNECTION;i++){
          GameTextTarget out = gsf.getUserOutput(i);
          if(out != null){
            calcForOneUser(i);
            out.gamerstate(gs); //Gamerの状態をクライアントに伝える
            distributeOutput(out);
          }
        }
      }
      rlt++;
    }


    for(int i=0;i<MAXCONNECTION;i++){
      GameTextTarget out = gsf.getUserOutput(i);
      if(out != null){
        out.finish();
      }
    }
  }
    
  private void startReceiver(GameInputThread git){
    int id = git.getUserID();
    git.init(new TranslateTarget(userinput[id],
                    new TranslationRule(id*10000,new Vec2(id*350,0))),
             new ShapeManager[]{userinput[id],wall[id],outwall[id],
                                blocks[id],ballandscore[id]}
             );
    git.start();
  }

  private void init(int id){
    wall[id].add(new Rectangle(id*10000+5, id*350+0, 0, 320, 20, wallattr));
    wall[id].add(new Rectangle(id*10000+6, id*350+0, 0, 20, 300, wallattr));
    wall[id].add(new Rectangle(id*10000+7, id*350+300,0, 20, 300, wallattr));
    //wall[id].add(new Rectangle(id*10000+8, id*350+0,281, 320, 20, wallattr));
    outwall[id].add(new Rectangle(id*10000+8, id*350+0,281, 320, 20, outattr));
    

    IntStream.range(0,10*20).forEach(n->{
        int x = n%20;
        int y = n/20;
        blocks[id].add(new Rectangle(id*10000+n,id*350+30+x*13,50+y*13,10,10,
                            new Attribute(250-y*10,100+y*10,250+y*10,true,0,0,0)));
      });
    pos[id] = new Vec2(id*350+150,200);
    vel[id] = new Vec2(4,-12);
    score[id] = 0;
  }
  
  private void calcForOneUser(int id){
    float[] btime = new float[]{1.0f};
    float[] stime = new float[]{1.0f};
    float[] wtime = new float[]{1.0f};
    float[] otime = new float[]{1.0f};
    float time =1.0f + score[id]*0.01f;

    while(0<time){
      btime[0] = time;
      stime[0] = time;
      wtime[0] = time;
      otime[0] = time;
      Vec2 tmpbpos = new Vec2(pos[id]);
      Vec2 tmpbvel = new Vec2(vel[id]);
      Vec2 tmpspos = new Vec2(pos[id]);
      Vec2 tmpsvel = new Vec2(vel[id]);
      Vec2 tmpwpos = new Vec2(pos[id]);
      Vec2 tmpwvel = new Vec2(vel[id]);
      Vec2 tmpopos = new Vec2(pos[id]);
      Vec2 tmpovel = new Vec2(vel[id]);
      Shape b=checker.check(userinput[id], tmpbpos, tmpbvel, btime);
      Shape s=checker.check(blocks[id], tmpspos, tmpsvel, stime);
      Shape w=checker.check(wall[id], tmpwpos, tmpwvel, wtime);
      Shape o=checker.check(outwall[id], tmpopos, tmpovel, otime);
      if( b != null && 
          (s == null || stime[0]<btime[0]) &&
          (w == null || wtime[0]<btime[0]) &&
          (o == null || otime[0]<btime[0])){
        pos[id] = tmpbpos;
        vel[id] = tmpbvel;
        time = btime[0];
      }else if(s != null){
        blocks[id].remove(s); // block hit!
        score[id]++;
        pos[id] = tmpspos;
        vel[id] = tmpsvel;
        time = stime[0];
      }else if(w != null){
        pos[id] = tmpwpos;
        vel[id] = tmpwvel;
        time = wtime[0];
      }else if(o != null){
        endflags[id] = true;
        time = otime[0];
        break;
      }else{
        pos[id] = MathUtil.plus(pos[id], MathUtil.times(vel[id],time));
        time = 0;
      }
    }
  }

  private void putScore(int id, int score){
    int one = score%10;
    int ten = (score/10)%10;
    int hun = (score/100)%10;
    ballandscore[id].put(new Digit(id*10000+2,id*300+250,330,20,one,scoreattr));
    ballandscore[id].put(new Digit(id*10000+3,id*300+200,330,20,ten,scoreattr));
    ballandscore[id].put(new Digit(id*10000+4,id*300+150,330,20,hun,scoreattr));
  }
  
  private void distributeOutput(GameTextTarget out){
    if(out == null){
      return;
    }
    out.clear();
    for(int i=0;i<MAXCONNECTION;i++){
      if(gsf.getUserOutput(i)!=null){
        out.draw(wall[i]);
        out.draw(outwall[i]);
        out.draw(blocks[i]);
        out.draw(userinput[i]);
        out.draw(ballandscore[i]);
      }
    }
    out.flush();
  }

  public static void main(String[] args){
    GameServer01 gs = new GameServer01();
    gs.start();
  }
}
