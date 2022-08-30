import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

abstract class MovingObject extends JPanel{
	private float x = 0 ;
	private float y = 0 ;
	void setX(float x)
	{
		this.x=x;
	}
	void setY(float y)
	{
		this.y=y;
	}
	float getPositionX(){
		return x;
	}
	float getPositionY() {
		return y;
	}
}

class Racket extends MovingObject{
	float racketWidth=100;
	float racketHeight=30;
	private float x;
	Racket() {
		setX(250.0f);
		setY(500.0f);
	}
	void move(int f) {
		x=getPositionX();
		if(f==1)	//왼쪽
		{
			if(x<=50)
				return;
			setX(x-20);
		}
		else 	//오른쪽
		{
			if(x+racketWidth>550)
				return;
			setX(x+20);
		}
	}
}

class Wall extends MovingObject{
	Wall(float x,float y) {
		setX(x);
		setY(y);
	}
}
class MyMovingBall{	//공 하나를 의미
	float px, py;
	float vx, vy;	//x방향 속도, y방향 속도
	float ax, ay;	//가속도
	Color color;
	float radius;
	MyMovingBall(float x, float y, Color c){
		px = x;
		py = y;
		color = c;
		vx = 0;			
		vy = -330;	//속도조절
		radius = 6;
	}
	public void draw(Graphics g) {
		//Graphics2D g2 = (Graphics2D) g;   //만약 그라데이션넣고싶다던지 그래서 2D쓰고싶으면 이렇게 하면된다.
		g.setColor(color);
		g.fillOval((int)(px-radius), (int)(py-radius), (int)(radius*2), (int)(radius*2));
	}
	public void update(float dt) {
		px = px + vx*dt;
		py = py + vy*dt;
	}
	public int collisionRacket(Racket racket) {
		float racketX=racket.getPositionX();
		float racketY=racket .getPositionY();
		if(px+radius>racketX && px-radius<racketX+racket.racketWidth&&py+radius<=racketY+racket.racketHeight/2&& py-radius>=racketY-radius*2.4) {			
 			vy=-vy;
			py=racketY-radius;
			if (px < racketX+racket.racketWidth/5)
				vx =-70;
			if(px>=racketX+racket.racketWidth/5&&px<racketX+racket.racketWidth/5*2)
				vx=-50;
			if(px>=racketX+racket.racketWidth/5*2&&px<racketX+racket.racketWidth/5*3)
				vx=0;
			if(px>=racketX+racket.racketWidth/5*3&&px<racketX+racket.racketWidth/5*4)
				vx=50;
			if(px>=racketX+racket.racketWidth/5*4&&px<racketX+racket.racketWidth)
				vx=70;	
			return 1;	//충돌하면
			}
		return 0;//충돌x
		
	}
	public int collisionWall(JComponent o) {
		if(py - radius <= 20 ) {			// 윗벽
			py=20+radius*2;
			vy = -vy;
		}
		if(py + radius > o.getHeight()) {			// 아래벽, 게임오버로만들기
			return 0;
		}
		if(px+radius > o.getWidth()-50) {				// 오른쪽벽
			px=550-radius;
			vx = -vx;
		}
		if(px-radius < 50) {							// 왼쪽벽
			px=50+radius;
			vx = -vx;
		}
		return 1;
	}
	public int collisionBlock(Block blocks[][],int BlockWidth,int BlockHeight) {
	
		int f=0;
		for(int i=0;i<blocks.length;i++)
		{
			for(int j=0;j<blocks[i].length;j++)
			{
				if(blocks[i][j].exist==1)
				{
					f=1;
				if(px+radius>blocks[i][j].x 
								&& px-radius<blocks[i][j].x+BlockWidth
								&&py+radius<=blocks[i][j].y+BlockHeight+radius*2) {	
						blocks[i][j].exist=0;
						vy=-vy;
						py=blocks[i][j].y+BlockHeight+radius;
						if(blocks[i][j].color==Color.YELLOW)
							return 2;	
						return 0;//충돌했다면
					}
				else if(px+radius>blocks[i][j].x 
						&& px-radius<blocks[i][j].x+BlockWidth
						&&py-radius<=blocks[i][j].y+radius*2)
				{
					blocks[i][j].exist=0;
					vy=-vy;
					py=blocks[i][j].y-radius;
					if(blocks[i][j].color==Color.YELLOW)
						return 2;	
					return 0;//충돌했다면
				}
				}
			}
		}
		if(f==0)	//존재하는 벽돌이 없다면
			return 1;
		else
			return -1;	
	}
	
}
class Block {	
	int exist;
	int x,y;
	Color color;
}

class SoundPlayer extends JPanel{
	Clip clip1,clip2,clip3,clip4,clip5,clip6;

	SoundPlayer(){
		
		try {

			clip1 = AudioSystem.getClip();
			clip2 = AudioSystem.getClip();
			clip3 = AudioSystem.getClip();
			clip4 = AudioSystem.getClip();
			clip5 = AudioSystem.getClip();
			clip6 = AudioSystem.getClip();
			
			URL url1 = getClass().getClassLoader().getResource("ping.wav");	//그냥 벽돌 부딪힐 때 소리
			URL url2 = getClass().getClassLoader().getResource("ping2.wav");	//노란 벽돌 부딪힐 때 소리
			URL url3 = getClass().getClassLoader().getResource("pong.wav");		//라켓 부딪힐 때 소리
			URL url4 = getClass().getClassLoader().getResource("title.wav");	//screen1
			URL url5 = getClass().getClassLoader().getResource("stageclear.wav");	//screen3
			URL url6 = getClass().getClassLoader().getResource("dead.wav");	//gameover될 때 사운드
			AudioInputStream audioStream1 = AudioSystem.getAudioInputStream(url1);
			AudioInputStream audioStream2 = AudioSystem.getAudioInputStream(url2);
			AudioInputStream audioStream3 = AudioSystem.getAudioInputStream(url3);
			AudioInputStream audioStream4 = AudioSystem.getAudioInputStream(url4);
			AudioInputStream audioStream5 = AudioSystem.getAudioInputStream(url5);
			AudioInputStream audioStream6 = AudioSystem.getAudioInputStream(url6);
			
			clip1.open(audioStream1);
			clip2.open(audioStream2);
			clip3.open(audioStream3);
			clip4.open(audioStream4);
			clip5.open(audioStream5);
			clip6.open(audioStream6);
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void pingSound() {
		clip1.setFramePosition(0);
		clip1.start();
	}
	void ping2Sound() {
		clip2.setFramePosition(0);
		clip2.start();
	}
	void pongSound() {
		clip3.setFramePosition(0);
		clip3.start();
	}
	void startScreen1Sound() {
		clip4.setFramePosition(0);
		clip4.start();
	}
	void exitScreen1Sound() {
		clip4.stop();
	}
	void stageClearSound() {
		clip5.setFramePosition(0);
		clip5.start();
	}
	void deadSound() {
		clip6.setFramePosition(0);
		clip6.start();
	}
}
class GameOverScreen extends JPanel implements Runnable{
	BufferedImage image;	
	static int highScore=0, yourScore=0;
	JLabel label;
	JLabel label2;
	JLabel score1Label;
	JLabel score2Label;
	Thread t;
	GameOverScreen(){
		setLayout(null);
		setVisible(false);
		label2=new JLabel("Game Over!!");
		label2.setForeground(Color.WHITE);
		label2.setBounds(100,100,400,200);
		label2.setFont(new Font("궁서 보통",Font.BOLD,40));
		label=new JLabel("Press Spacebar!");
		label.setForeground(Color.RED);
		label.setBounds(100,200,400,200);
		Font font=new Font("궁서 보통",Font.BOLD,30);
		label.setFont(font);
		score1Label=new JLabel("High Score: "+highScore);
		score1Label.setBounds(100,250,400,400);
		score1Label.setForeground(Color.BLACK);
		score1Label.setFont(font);
		score2Label=new JLabel("Your Score: "+yourScore);
		score2Label.setBounds(100,300,400,400);
		score2Label.setForeground(Color.BLACK);
		score2Label.setFont(font);
		
		add(label);	//***
		add(label2);
		add(score1Label);
		add(score2Label);
		
		setBackground(Color.BLACK);
		
		setBounds(0,0,600,600);
		setLayout(null);
		
		t=new Thread(this);
		
		try {	
			URL imageUrl = getClass().getClassLoader().getResource("blockImage.jpg");	
			image = ImageIO.read(imageUrl);
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	@Override
	public void run() {
		while(true)
		{
			try {
					score1Label.setText("Your Score: "+yourScore);
					score2Label.setText("High Score: "+highScore);
					label.setText("Press Spacebar!");
					Thread.sleep(500);
					label.setText("");
					Thread.sleep(200);
			}
			catch (InterruptedException e) {
				setVisible(false);
				return;
			}
		}
		
	}
	@Override
	public void paintComponent(Graphics g){
		g.drawImage(image, 0, 0, null);	
		Image resizeImage = image.getScaledInstance(600, 600, Image.SCALE_SMOOTH);
		g.drawImage(resizeImage, 0, 0, null);
	}
	
}
class Detectball_YellowPosition{
	float x,y,vx,vy;
	Detectball_YellowPosition(float x, float y,float vx,float vy){
		this.x=x;
		this.y=y;
		this.vx=vx;
		this.vy=vy;
	}
}
class GameScreen extends JPanel implements Runnable{	//Screen2
	
	Racket racket;
	ArrayList<MyMovingBall> balls;
	ArrayList<Detectball_YellowPosition>yellowXY;
	Wall leftWall,rightWall,upWall;
	SoundPlayer Sound;
	
	int score=0;
	private float x,y;
	private float dt = 0.03f;
	static int BlockRow;
	static int BlockCol;
	static int BlockWidth;
	static int BlockHeight;
	Block[][] blocks;
	Thread t;
	
	GameScreen(){
		racket=new Racket();
		x=racket.getPositionX();
		y=racket.getPositionY();
		
		balls=new ArrayList<>();
		balls.add(new MyMovingBall(x+50, y-6, Color.WHITE));
		leftWall=new Wall(0,0);
		rightWall=new Wall(550,0);
		upWall=new Wall(0,0);
		yellowXY = new ArrayList<>();
		int flag=0;	//색깔 초기화 flag
		
		Sound=new SoundPlayer();
		
		setBounds(0,0,600,600);
		setBackground(Color.DARK_GRAY);
		setLayout(null);
		setVisible(false);
		t=new Thread(this);
	}
	
	void make_block(int row,int col){
		BlockRow=row;
		BlockCol=col;
	    BlockWidth = 500/BlockCol;
	    BlockHeight = 200/BlockRow;
		blocks= new Block[BlockRow][BlockCol];
	    for (int i = 0; i < blocks.length; i++) {
	        for (int j = 0; j < blocks[0].length; j++) {
	        	blocks[i][j]=new Block();
	            blocks[i][j].exist = 1;
	     	    int random = (int)(Math.random()*2);	//0~255
    		    if(random==0)
    		    	blocks[i][j].color= Color.MAGENTA;
    		    else
    		    	blocks[i][j].color=Color.YELLOW;
    		    
    		 	blocks[i][j].x=j*BlockWidth+50;
            	blocks[i][j].y=i * BlockHeight + 20;
	        }
	    }
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2=(Graphics2D)g;
		super.paintComponent(g);	
		GradientPaint gp2 = new GradientPaint(0,0,Color.WHITE,100,20,Color.LIGHT_GRAY);
		GradientPaint gp3;
		g.setColor(Color.GRAY);	//racket draw
		g2.setPaint(gp2);
		g.fillRect((int)x, (int)y, 100, 20);
		racket.racketWidth=100;
		
		for(MyMovingBall ball: balls)
		{
			ball.draw(g);	//ball
		}

		//wall draw
		g.setColor(Color.PINK);	
		g.fillRect((int)leftWall.getPositionX(),(int)leftWall.getPositionY(),50,600);
		g.fillRect((int)rightWall.getPositionX(),(int)rightWall.getPositionY(),50,600);
		g.fillRect((int)upWall.getPositionX(), (int)upWall.getPositionY(),600,20);
		
		//blockdraw
	    for (int i = 0; i < blocks.length; i++) {
	        for (int j = 0; j < blocks[0].length; j++) {
	            if(blocks[i][j].exist == 1)
	            {
	            	gp3= new GradientPaint(blocks[i][j].x,blocks[i][j].y,Color.WHITE,blocks[i][j].x+BlockWidth,blocks[i][j].y+BlockHeight,blocks[i][j].color);
	            	g2.setPaint(gp3);
	                g.fillRect(j * BlockWidth + 50, i * BlockHeight + 20, BlockWidth, BlockHeight);
	            	g.setColor(Color.BLACK);
	                g.drawRect(j * BlockWidth + 50, i * BlockHeight + 20, BlockWidth, BlockHeight);
	            }
	        }
	    }
	}
	void moveRacket(int f) {
		if(f==1)	//왼쪽
		{
			racket.move(f);
		}
		else 	//오른쪽
		{
			racket.move(f);
		}
	}
	int over,over2,racketcollision;
	int addball=0;
	@Override
	public void run() {
		int f=0;
		try {
			BlockRow=3;
			BlockCol=3;
			make_block(BlockRow,BlockCol);	
			while(true) {
				for(MyMovingBall ball:balls)
				{
					ball.update(dt);
					racketcollision=ball.collisionRacket(racket);
					if(racketcollision==1)
					{
						Sound.pongSound();
					}
					ball.collisionWall(this);
					over2=ball.collisionBlock( blocks,BlockWidth,BlockHeight);
					if(over2==0)	//벽돌 충돌
					{
						score+=10;
						Sound.pingSound();
					}
					if(over2==2)	//노란벽돌과 충돌
					{
						Sound.ping2Sound();
						Detectball_YellowPosition yellow=new Detectball_YellowPosition(ball.px,ball.py,ball.vx,ball.vy);
						yellowXY.add(yellow);
						addball++;
						score+=10;
					}
					for(MyMovingBall ball2 : balls)
					{
						over=ball2.collisionWall(this);
						if(over!=0)	
						{
							f=1;
						}
					}
					if(f==0)
					{
						Sound.deadSound();
						Thread.sleep(1000);
						t.interrupt();
					}
					f=0;
				}
				if(over2==1)	//벽돌이 다 없어지면 다음스테이지리셋
				{
					Sound.stageClearSound();
					Thread.sleep(2000);
					balls.removeAll(balls);
					yellowXY.removeAll(yellowXY);
					BlockRow*=2;
					BlockCol*=2;
					make_block(BlockRow,BlockCol);
					x=racket.getPositionX();
					y=racket.getPositionY();
					balls.add(new MyMovingBall(x+50, y-6, Color.WHITE));//zz
	            	over2=0;
				}
				if(addball>0)
				{
					for(Detectball_YellowPosition yellow : yellowXY)
					{
						MyMovingBall newball=new MyMovingBall(yellow.x, yellow.y, Color.WHITE);
						balls.add(newball);
						newball.vy=-newball.vy;
						newball.vx=yellow.vx-50;
						newball=new MyMovingBall(yellow.x, yellow.y, Color.WHITE);
						balls.add(newball);
						newball.vy=-newball.vy;
						newball.vx=yellow.vx+50;
						
						over2=0;
						addball=0;
					}
					addball=0;
					yellowXY.removeAll(yellowXY);
				}
				x=racket.getPositionX();
				y=racket.getPositionY();
				repaint();
			
				Thread.sleep((int)(dt*1000));	//0.01초마다 갱신
				}
			}
		catch(InterruptedException e) {
				BlockCrashGame Main = new BlockCrashGame();
				Main.f=1;
				Main.Screen3.yourScore=score;
				if(Main.Screen3.highScore <= score)
				{
					Main.Screen3.highScore=score;
				}
				Main.Screen3.t.start();
				Main.Screen3.setVisible(true);
				setVisible(false);
				return;
			}	
		}
}
class StartScreen extends JPanel implements Runnable {	//screen1
	
	JLabel label;
	JLabel label2;
	Thread t;
	BufferedImage image;
	
	StartScreen(){
		setLayout(null);
		setBounds(0,0,600,600);
		label2=new JLabel("Block Breaker");
		label2.setForeground(Color.WHITE);
		label2.setBounds(100,100,400,200);
		label2.setFont(new Font("궁서 보통",Font.BOLD,40));
		label=new JLabel("Press Spacebar to play");
		label.setForeground(Color.RED);
		label.setBounds(100,200,400,200);
		Font font=new Font("궁서 보통",Font.BOLD,30);
		label.setFont(font);
		add(label);	//***
		add(label2);
		
		t=new Thread(this);
		t.start();
		//스레드 호출 후에 해야 화면크기 이상하지 X
		try {	
			URL imageUrl = getClass().getClassLoader().getResource("blockImage.jpg");	
			image = ImageIO.read(imageUrl);
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
	}
	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, null);	
		Image resizeImage = image.getScaledInstance(600, 600, Image.SCALE_SMOOTH);
		g.drawImage(resizeImage, 0, 0, null);
	}
	@Override
	public void run() {
		while(true)
		{
			try {
					label.setText("Press Spacebar to play");
					Thread.sleep(500);
					label.setText("");
					Thread.sleep(200);
			}
			catch (InterruptedException e) {
				return;
			}
		}
		
	}
}
public class BlockCrashGame extends JFrame implements KeyListener{

	public static StartScreen Screen1;
	public static GameScreen Screen2;
	public static GameOverScreen Screen3;
	static int f;
	SoundPlayer Sound;
	
	public static void main(String[] args) {
		var Game=new BlockCrashGame();
		Game.make_game();
		f=0;
	}
	
	void make_game(){
		Sound=new SoundPlayer();
		setTitle("Java Homework4");
		setSize(610,635);
	
		setLocationRelativeTo(null);//창이 가운데 나오게
		
		Screen1=new StartScreen();
		add(Screen1);	//***순서
		Sound.startScreen1Sound();
		Screen2=new GameScreen();
		add(Screen2);
		Screen3=new GameOverScreen();
		add(Screen3);
		
		this.addKeyListener(this);
		this.setFocusable(true);
		this.requestFocus();
	
		setLayout(null);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_SPACE)
		{
			if(f==0)
			{
				Sound.exitScreen1Sound();
				Screen1.t.interrupt();
				Screen1.setVisible(false);
				Screen2.setVisible(true);
				Screen2.t.start();
				f=2;
			}
			else if(f==1)
			{
				Screen3.t.interrupt();
				Screen3.setVisible(false);
				Sound.startScreen1Sound();
				Screen1=new StartScreen();	//순서
				add(Screen1);	//순서중요
				Screen2=new GameScreen();
				add(Screen2);
				Screen3=new GameOverScreen();
				add(Screen3);
				
				f=0;
			}
			else//f==2
			{
				Screen2.t.interrupt();
				Sound.deadSound();
			}
		}
		if(e.getKeyCode()==KeyEvent.VK_LEFT)
		{
			Screen2.moveRacket(1);
			repaint();
		}
		if(e.getKeyCode()==KeyEvent.VK_RIGHT)
		{
			Screen2.moveRacket(2);
			repaint();
		}	
	}
	@Override
	public void keyTyped(KeyEvent e) {
	}
	@Override
	public void keyReleased(KeyEvent e) {
	}


}
