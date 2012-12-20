package com.example.pyramids;
//import android.app.DrawView;

import android.app.Activity;
import java.util.Random;
import java.util.Date;
import java.text.NumberFormat;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Color;
import android.view.View;
import android.view.MotionEvent;
import android.util.Log;
import android.util.DisplayMetrics;

public class Deck extends View
{
    private static final String TAG = "Deck";

    public static final int WON = 1;
    public static final int LOST = 2;

    public static final int stackOffset = 190;
    
    private int height, width;


    private SharedPreferences saveState;
    private int highScore, highRun, totalGames;
    private float average;

    private boolean gameOver;

    private Bitmap board;
    private Bitmap blankCard;
    private Bitmap clickHere;
    private Bitmap gameOverField;
    private Bitmap playAgain;

    private int[] cardArray = {
        R.drawable.card0,
        R.drawable.card1,
        R.drawable.card2,
        R.drawable.card3,
        R.drawable.card4,
        R.drawable.card5,
        R.drawable.card6,
        R.drawable.card7,
        R.drawable.card8,
        R.drawable.card9,
        R.drawable.card10,
        R.drawable.card11,
        R.drawable.card12,
        R.drawable.card13,
        R.drawable.card14,
        R.drawable.card15,
        R.drawable.card16,
        R.drawable.card17,
        R.drawable.card18,
        R.drawable.card19,
        R.drawable.card20,
        R.drawable.card21,
        R.drawable.card22,
        R.drawable.card23,
        R.drawable.card24,
        R.drawable.card25,
        R.drawable.card26,
        R.drawable.card27,
        R.drawable.card28,
        R.drawable.card29,
        R.drawable.card30,
        R.drawable.card31,
        R.drawable.card32,
        R.drawable.card33,
        R.drawable.card34,
        R.drawable.card35,
        R.drawable.card36,
        R.drawable.card37,
        R.drawable.card38,
        R.drawable.card39,
        R.drawable.card40,
        R.drawable.card41,
        R.drawable.card42,
        R.drawable.card43,
        R.drawable.card44,
        R.drawable.card45,
        R.drawable.card46,
        R.drawable.card47,
        R.drawable.card48,
        R.drawable.card49,
        R.drawable.card50,
        R.drawable.card51
    };

    private final int deleted = -1;
    private final int rows = 4;

    // TODO dynamic padding
    private int horizontalPadding; // = 100;
    private int verticalPadding; // = 60;

    private final int boardWidth = 565;
    private final int boardHeight = 298;

    private final int gameOverWidth = 231;
    private final int gameOverHeight = 133;

    private final int cardWidth = 54;
    private final int cardHeight = 72;

    private final int[][] cardPositions =
        {
            {81, 243, 405},
            {54, 108, 216, 270, 378, 432},
            {27, 81, 135, 189, 243, 297, 351, 405, 459},
            {0, 54, 108, 162, 216, 270, 324, 378, 432, 486}
        };
    private final int[][] spaceCode =
        {
            {6,10,10},
            {4,2,6,2,6,2},
            {2,2,2,2,2,2,2,2,2},
            {0,2,2,2,2,2,2,2,2,2}
        };
    private final int[] childrenMarkers =
        {3, 5, 7, 9, 10, 12, 13, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25, 26};
    //private final String[] colors = { "♣", "♠", "♥", "♦" };
    private final String[] colors = { "C", "S", "H", "D" };
    private final String[] faces = { "T", "J", "Q", "K", "A" };
    private final int numberOfCards = 52;

    private int[] cards;
    private Bitmap[] cardImages;

    private int stackCard;
    private int stackPos;
    private int stackRest;
    private int cardsToGo;

    private int run;
    private int score;
    private int round;

    /* http://games.yahoo.com/help/rules/py&ss=1 */
    private int currentWorth;
    private int pyramidBonus;
    private final int clearBonus = 1000;

    private void setBitmaps(Context context)
    {
        this.clickHere = BitmapFactory.decodeResource(getResources(),
            R.drawable.clickhere);
        this.gameOverField = BitmapFactory.decodeResource(getResources(),
            R.drawable.gameover);
        this.playAgain = BitmapFactory.decodeResource(getResources(),
            R.drawable.playagain);
        this.blankCard = BitmapFactory.decodeResource(getResources(),
            R.drawable.cardblank);
        this.board = BitmapFactory.decodeResource(getResources(),
            R.drawable.board);

        for (int i = 0; i < this.numberOfCards; ++i)
        {
            this.cardImages[i] = BitmapFactory.decodeResource(getResources(),
                this.cardArray[i]);
        }
    }

    private void shuffle()
    {
        Random generator = new Random();
        int temp, randomCard;

        for (int i = 0; i < this.numberOfCards; ++i)
        {
            randomCard = generator.nextInt(this.numberOfCards);
            temp = this.cards[i];
            this.cards[i] = this.cards[randomCard];
            this.cards[randomCard] = temp;
        }
    }

    private String numWithCommas(int n)
    {
        return NumberFormat.getNumberInstance().format(n);
    }

    private String getValue(int num)
    {
        return ((num % 13) > 7
                ? this.faces[(num % 13) - 8]
                : (num % 13 + 2))
                + this.colors[num / 13];
    }

    private void printSpaces(int n)
    {
        for (int i = 0; i < n; ++i)
            System.out.print(" ");
    }

    private boolean isDeleted(int pos)
    {
        return this.cards[pos] == this.deleted;
    }

    private boolean isFree(int pos)
    {
        return isDeleted(childrenMarkers[pos]) &&
            isDeleted(childrenMarkers[pos]+1);
    }

    private void printValue(int pos)
    {
        System.out.print(getValue(this.cards[pos]));
    }

    private int getParent(int pos)
    {
        for (int i = 0; i < childrenMarkers.length; ++i)
        {
            if (childrenMarkers[i] == pos || childrenMarkers[i] == pos-1)
                return i;
        }
        return -1;
    }

    private int valueToNum(String s)
    {
        String faceString, colorString;
        int face = -1;
        int color = -1;
        s = s.toUpperCase();

        if (s.length() != 2)
            return -1;

        faceString = s.substring(0,1);
        colorString = s.substring(1,2);

        for (int i = 0; i < 8; ++i)
        {
            if (faceString.equals((new Integer(i+2)).toString()))
                face = i;
        }
        for (int i = 8; i < 13; ++i)
        {
            if (faceString.equals(this.faces[i-8]))
                face = i;
        }
        if (face < 0)
            return -1;

        for (int i = 0; i < 4; ++i)
        {
            if (colorString.equals(this.colors[i]))
                color = i;
        }
        if (color < 0)
            return -1;

        return color*13 + face;
    }

    private void deleteCard(int pos)
    {
        this.cards[pos] = -1;
        //save("cards" + pos, -1);
        save();
    }

    private boolean isNeighbor(int pos)
    {
        int x = this.cards[pos] % 13;
        int y = this.stackCard % 13;
        if ((x == 12 && y == 0) || (y == 12 && x == 0))
            return true;
        return Math.abs(x-y) == 1;
    }

    public boolean haveIWon()
    {
        return this.cardsToGo == 0;
    }

    private int nextStack()
    {
        if (this.stackRest == 1)
        {
            return 2;
        }
        ++this.stackPos;
        --this.stackRest;
        //save("stackPos", stackPos);
        //save("stackRest", stackRest);

        String s = ">>> stackPos: " + this.stackPos;
        Log.v(TAG, s);

        this.stackCard = cards[this.stackPos];
        //save("stackCard", stackCard);
        save();

        s = ">>> stackCard: " + getValue(this.stackCard);
        Log.v(TAG, s);
        return 0;
    }

    private void score(int pos)
    {
        if (pos < 3)
        {
            this.score += this.pyramidBonus;
            this.pyramidBonus += 250;
            //save("pyramidBonus", pyramidBonus);
            save();
        }
        String s = ">>> currentWorth: " + this.currentWorth;
        Log.v(TAG, s);

        s = ">>> cardsToGo: " + this.cardsToGo;
        Log.v(TAG, s);

        this.score += this.currentWorth;
        if (this.run < 7)
            this.currentWorth *= 2;
        else if (this.run == 7 || this.run == 8)
            this.currentWorth *= 1.5;
        else
            this.currentWorth += 100;

        //save("score", score);
        //save("currentWorth", currentWorth);
        save();
    }

    private void newHighRun(int run)
    {
        if (run > this.highRun)
            this.highRun = run;
    }

    private void newRun()
    {
        newHighRun(this.run);
        this.run = 0;
        this.currentWorth = 10;

        //save("run", run);
        //save("currentWorth", currentWorth);
        save();
    }

    public int currentRound()
    {
        return this.round;
    }

    public int currentScore()
    {
        return this.score;
    }

    public int move(int i)
    {
        if (isNeighbor(i))
        {
            this.stackCard = cards[i];
            //save("stackCard", stackCard);

            String s = ">>> stackCard (move): " + getValue(this.stackCard)
                                                  + ", i: " + i;
            Log.v(TAG, s);

            --this.cardsToGo;
            ++this.run;
            //save("cardsToGo", cardsToGo);
            //save("run", run);
            deleteCard(i);
            score(i);
            if (haveIWon())
                iWon();
                //return 1;
            save();
            invalidate();
        }
        return 0;
    }

    private String printStack()
    {
        String s = "";
        for (int i = 0; i < this.stackRest - 1; ++i)
            s += "#";
        return s;
    }

    public void print()
    {
        int pos = 0;
        System.out.println();
        for (int i = 0; i < this.rows; ++i)
        {
            for (int j = 0; j < spaceCode[i].length; ++j)
            {
                printSpaces(spaceCode[i][j]);
                if (isDeleted(pos))
                    System.out.print("  ");
                else
                {
                    if (i < this.rows-1)
                    {
                        if (isFree(pos))
                            printValue(pos);
                        else
                            System.out.print("@@");
                    }
                    else
                    {
                        printValue(pos);
                    }
                }
                ++pos;
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Card:  " + getValue(this.stackCard));
        System.out.println("Stack: " + printStack());
        System.out.println();
        System.out.println("Run:   " + this.run);
        System.out.println("Score: " + this.score);
        System.out.println("Round: " + this.round);
        System.out.println();
    }

    private void iWon()
    {
        Log.v(TAG, "i won.");
        ++this.round;
        this.score += this.clearBonus + this.stackRest * 100;
        //save("round", round);
        //save("score", score);
        save();
        init();
    }

    private void calculateAverage(int score)
    {
       this.average = (float)
           (average*((double)totalGames/(totalGames+1)) +
            ((double)score/(totalGames+1)));
    }

    private void youLost()
    {
        newHighscore(this.score);
        calculateAverage(this.score);
        ++this.totalGames;
        this.gameOver = true;
        //save("gameOver", gameOver);
        save();
        invalidate();
        Log.v(TAG, "you lost.");
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev)
    {
        int pos = 0;
        int x = ((int)ev.getX()) - this.horizontalPadding;
        int y = ((int)ev.getY()) - this.verticalPadding;

        if (ev.getAction() != MotionEvent.ACTION_DOWN)
            return true;

        // Play Again
        int playX = (boardWidth-gameOverWidth)/2+80;
        int playY = (boardHeight-gameOverHeight)/2+95;
        if (gameOver)
        {
            if( x >= playX && x <= playX + 74 &&
                y >= playY && y <= playY + 25)
            {
                Log.v(TAG, "playagain");
                this.round = 1;
                this.score = 0;
                //save("round", round);
                //save("score", score);
                init();
                this.gameOver = false;
                //save("gameOver", gameOver);
                save();
                invalidate();
                // TODO save highscores
            }
            return true;
        }

        // Click Here
        if (x >= 0 && x <= this.cardWidth &&
                y >= stackOffset && y <= stackOffset+this.cardHeight)
        {
            if (this.stackRest == 1)
            {
                youLost();
                return true;
            }
        }

        // Stack
        if (x >= 0 && x <= (this.stackRest-2)*12 + this.cardWidth &&
                y >= stackOffset && y <= stackOffset + this.cardHeight)
        {
            newRun();
            nextStack();
            String s = "########## High Run: " + highRun + ", " +
                "Average: " + average + ", " +
                "Total Games: " + totalGames;
            Log.v(TAG, s);
            invalidate();
            return true;
        }

        pos = -1;
        for (int i = 0; i < this.cardPositions.length; ++i)
        {
            for (int j = 0; j < this.cardPositions[i].length; ++j)
            {
                ++pos;
                if ((pos <= 17 && !isFree(pos)) || isDeleted(pos))
                    continue;

                int cardX = this.cardPositions[i][j];
                int cardY = i*30;

                if (
                        (x >= cardX) &&
                        (x <= cardX + this.cardWidth) &&
                        (y >= cardY) &&
                        (y <= cardY + this.cardHeight) &&
                        (!isDeleted(pos)) &&
                        (pos > 17 || isFree(pos))
                        )
                {
                    String s = ": " + pos;
                    Log.v(TAG, s);
                    move(pos);
                    return true;
                }
            }
        }
        return true;
    }

    private void printTime(long start)
    {
        String s = "time: " + ((new Date().getTime())-start);
        Log.v(TAG, s);
    }

    private void drawText(Canvas canvas, Paint paint, String text,
            int left, int top, Paint.Align align, int size)
    {
        paint.setTextSize(size);
        paint.setTextAlign(align);
        canvas.drawText(text,
                horizontalPadding + left,
                verticalPadding + top,
                paint);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        int pos = 0;

        Bitmap currentCard;
        String text;

        /*
        canvas.drawBitmap(this.board,
                this.horizontalPadding-5,
                this.verticalPadding-5, null);
                */

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        paint.setAntiAlias(true);

        drawText(canvas, paint, "Run: " + this.run,
                10, -10, Paint.Align.LEFT, 18);
        drawText(canvas, paint, "Score: " + numWithCommas(this.score),
                230, -10, Paint.Align.LEFT, 18);
        drawText(canvas, paint, "Round: " + this.round,
                530, -10, Paint.Align.RIGHT, 18);
        drawText(canvas, paint, "High Score: " + numWithCommas(highScore),
                10, 283, Paint.Align.LEFT, 14);
        drawText(canvas, paint, "Average: " + this.average,
                160, 283, Paint.Align.LEFT, 14);
        drawText(canvas, paint, "Total Games: " + this.totalGames,
                300, 283, Paint.Align.LEFT, 14);
        drawText(canvas, paint, "High Run: " + this.highRun,
                445, 283, Paint.Align.LEFT, 14);

        for (int i = 0; i < this.rows; ++i)
        {
            for (int j = 0; j < this.cardPositions[i].length; ++j)
            {
                if (!isDeleted(pos))
                {
                    if ((i < this.rows-1 && isFree(pos)) || (i >= this.rows-1))
                        currentCard = this.cardImages[this.cards[pos]];
                    else
                        currentCard = this.blankCard;

                    canvas.drawBitmap(currentCard,
                            this.horizontalPadding + this.cardPositions[i][j],
                            this.verticalPadding + i*30, null);
                }
                ++pos;
            }
        }

        // Click Here
        canvas.drawBitmap(this.clickHere,
                this.horizontalPadding,
                this.verticalPadding + stackOffset, null);

        // Stack
        for (int i = 0; i < this.stackRest-1; ++i)
        {
            canvas.drawBitmap(this.blankCard,
                    this.horizontalPadding + i*12,
                    this.verticalPadding + stackOffset, null);
        }

        String s = ">>> stackCard (onDraw): " + getValue(this.stackCard);
        Log.v(TAG, s);

        // Stack Card
        canvas.drawBitmap(this.cardImages[this.stackCard],
                this.horizontalPadding + 350,
                this.verticalPadding + stackOffset, null);

        if (gameOver)
        {
            canvas.drawBitmap(this.gameOverField,
                    this.horizontalPadding + (boardWidth-gameOverWidth)/2,
                    this.verticalPadding + (boardHeight-gameOverHeight)/2,
                    null);
            canvas.drawBitmap(this.playAgain,
                    this.horizontalPadding + (boardWidth-gameOverWidth)/2 + 80,
                    this.verticalPadding + (boardHeight-gameOverHeight)/2 + 95,
                    null);
            paint.setColor(0xff0000aa);
            paint.setTextSize(18);
            paint.setTextAlign(Paint.Align.RIGHT);
            text = numWithCommas(this.score);
            canvas.drawText(text,
                    this.horizontalPadding + (boardWidth-gameOverWidth)/2 + 220,
                    this.verticalPadding + (boardHeight-gameOverHeight)/2 + 66,
                    paint);
        }
    }

    private void generateCards()
    {
        for (int i = 0; i < numberOfCards; ++i)
            cards[i] = i;
        shuffle();
    }

    private void init()
    {
        //this.pyramidBonus = 500;
        this.stackPos = 28;
        this.stackRest = this.numberOfCards - this.stackPos;
        this.stackCard = cards[this.stackPos];
        this.cardsToGo = 28;

        //save("stackPos", stackPos);
        //save("stackRest", stackRest);
        //save("stackCard", stackCard);
        //save("cardsToGo", cardsToGo);

        /* Yahoo doesn't reset the runs, but resets
         * currentWorth to 10 when round is won.
         * Probably a bug. */
        //this.run = 0;
        this.currentWorth = 10;

        this.pyramidBonus = 250+this.round*250;

        //save("currentWorth", currentWorth);
        //save("pyramidBonus", pyramidBonus);

        generateCards();
        //for (int i = 0; i < numberOfCards; ++i)
            //save("cards" + i, cards[i]);
        save();
    }

    private void save()
    {
        SharedPreferences.Editor editor = this.saveState.edit();
        editor.putInt("highRun", highRun);
        editor.putFloat("average", average);
        editor.putInt("totalGames", totalGames);

        editor.putInt("score", score);
        editor.putBoolean("gameOver", gameOver);
        editor.putInt("round", round);
        editor.putInt("pyramidBonus", pyramidBonus);
        editor.putInt("currentWorth", currentWorth);
        editor.putInt("run", run);

        editor.putInt("stackPos", stackPos);
        editor.putInt("stackRest", stackRest);
        editor.putInt("stackCard", stackCard);
        editor.putInt("cardsToGo", cardsToGo);

        editor.putInt("currentWorth", currentWorth);

        for (int i = 0; i < numberOfCards; ++i)
            editor.putInt("cards" + i, cards[i]);

        editor.commit();
    }

    private void save(String s, boolean value)
    {
        SharedPreferences.Editor editor = this.saveState.edit();
        editor.putBoolean(s, value);
        editor.commit();
    }

    private void save(String s, int value)
    {
        SharedPreferences.Editor editor = this.saveState.edit();
        editor.putInt(s, value);
        editor.commit();
    }

    private void newHighscore(int score)
    {
        if (score <= this.highScore)
            return;
        this.highScore = score;
        SharedPreferences.Editor editor = this.saveState.edit();
        editor.putInt("highscore", this.highScore);
        editor.commit();
    }

    private boolean restoreLastState()
    {
        this.highScore = saveState.getInt("highscore", 0);
        this.highRun = saveState.getInt("highRun", 0);
        this.average = saveState.getFloat("average", 0F);
        this.totalGames = saveState.getInt("totalGames", 0);

        this.score = saveState.getInt("score", 0);
        this.gameOver = saveState.getBoolean("gameOver", false);
        this.round = saveState.getInt("round", 1);
        //this.pyramidBonus = saveState.getInt("pyramidBonus", 750);
        this.currentWorth = saveState.getInt("currentWorth", 10);
        this.run = saveState.getInt("run", 0);

        this.stackPos = saveState.getInt("stackPos", 28);
        this.stackRest = saveState.getInt("stackRest",
                this.numberOfCards - this.stackPos);
        this.stackCard = saveState.getInt("stackCard",
                cards[this.stackPos]);
        this.cardsToGo = saveState.getInt("cardsToGo", 28);

        /* Yahoo doesn't reset the runs, but resets
         * currentWorth to 10 when round is won.
         * Probably a bug. */
        //this.run = 0;
        this.currentWorth = saveState.getInt("currentWorth", 10);

        this.pyramidBonus = saveState.getInt("pyramidBonus",
                250+this.round*250);

        // TODO maybe check all 52 cards?
        if (saveState.getInt("cards51", -9) != -9)
        {
            for (int i = 0; i < numberOfCards; ++i)
                cards[i] = saveState.getInt("cards" + i, i);
        }
        else
            generateCards();

        return true;
    }

    public Deck(Context context)
    {
        super(context);
    }

    public Deck(Context context, int width, int height, SharedPreferences save)
    {
        this(context);

        this.saveState = save;

        cards = new int[numberOfCards];
        cardImages = new Bitmap[numberOfCards];
        if (!restoreLastState())
        {
            this.score = 0;
            this.gameOver = false;
            this.round = 1;
            this.pyramidBonus = 750;
            this.currentWorth = 10;
            this.run = 0;
            init();
        }
        setBitmaps(context);

        this.horizontalPadding = (width-this.boardWidth)/2+13;
        this.verticalPadding = (height-this.boardHeight-30)/2+32;
    }
}
