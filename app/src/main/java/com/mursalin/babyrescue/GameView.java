package com.mursalin.babyrescue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

public class GameView extends View {

    private final int UPDATE_DELAY = 30; // Milliseconds
    private final float TEXT_SIZE = 100;
    private final int MAX_LIFE = 3;

    private int screenWidth, screenHeight;
    private Bitmap trash, dragon, baby;
    private Handler handler;
    private Runnable runnable;
    private Random random;
    private int dragonX, dragonY;
    private int babyX, babyY;
    private boolean babyAnimation;
    private int points;
    private int life;
    private Paint textPaint;
    private Paint healthPaint;
    private int dragonSpeed;
    private int trashX, trashY;
    private MediaPlayer mpPoint, mpWhoosh, mpPop;

    public GameView(Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        Display display = getDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        trash = BitmapFactory.decodeResource(getResources(), R.drawable.trash);
        dragon = BitmapFactory.decodeResource(getResources(), R.drawable.drag1);
        baby = BitmapFactory.decodeResource(getResources(), R.drawable.baby1);

        handler = new Handler();
        runnable = this::invalidate;
        random = new Random();

        dragonX = screenWidth + random.nextInt(300);
        dragonY = screenHeight + random.nextInt(600);
        babyX = dragonX;
        babyY = dragonY + dragon.getHeight() - 30;

        babyAnimation = false;
        points = 0;
        life = MAX_LIFE;

        textPaint = new Paint();
        textPaint.setColor(Color.rgb(255, 0, 0));
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);

        healthPaint = new Paint();
        healthPaint.setColor(Color.GREEN);

        dragonSpeed = 21 + random.nextInt(30);

        trashX = screenWidth / 2 - trash.getWidth() / 2;
        trashY = screenHeight - trash.getHeight();

        mpPoint = MediaPlayer.create(getContext(), R.raw.levelpass);
        mpWhoosh = MediaPlayer.create(getContext(), R.raw.levelpass);
        mpPop = MediaPlayer.create(getContext(), R.raw.levelpass);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLUE);

        if (!babyAnimation) {
            dragonX -= dragonSpeed;
            babyX -= dragonSpeed;
        }

        if (dragonX <= -dragon.getWidth()) {
            if (mpWhoosh != null) {
                mpWhoosh.start();
            }

            resetDragonAndBabyPositions();
            life--;

            if (life == 0) {
                launchGameOverActivity();
                return;
            }
        }

        if (babyAnimation) {
            babyY += 40;
        }

        if (babyAnimation && babyHitsTrash()) {
            if (mpPoint != null) {
                mpPoint.start();
            }

            increasePoints();
            resetDragonAndBabyPositions();
        }

        if (babyAnimation && babyReachesBottom()) {
            if (mpPop != null) {
                mpPop.start();
            }

            life--;
            if (life == 0) {
                launchGameOverActivity();
                return;
            }

            resetDragonAndBabyPositions();
        }

        canvas.drawBitmap(trash, trashX, trashY, null);
        canvas.drawBitmap(dragon, dragonX, dragonY, null);
        canvas.drawBitmap(baby, babyX, babyY, null);
        canvas.drawText(String.valueOf(points), 20, TEXT_SIZE, textPaint);
        canvas.drawRect(screenWidth - 200, 30, screenHeight - 200 + 60 * life, 80, healthPaint);

        handler.postDelayed(runnable, UPDATE_DELAY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            if (!babyAnimation && touchWithinDragonBounds(touchX, touchY)) {
                babyAnimation = true;
            }
        }

        return true;
    }

    public Display getDisplay() {
        return ((Activity) getContext()).getWindowManager().getDefaultDisplay();
    }

    private void resetDragonAndBabyPositions() {
        dragonX = screenWidth + random.nextInt(300);
        dragonY = random.nextInt(600);
        babyX = dragonX;
        babyY = dragonY + dragon.getHeight() - 30;
        dragonSpeed = 21 + random.nextInt(30);
        trashX = dragon.getWidth() + random.nextInt(screenWidth - 2 * dragon.getWidth());
    }

    private boolean babyHitsTrash() {
        return babyAnimation && babyX + baby.getWidth() >= trashX &&
                babyX <= trashX + trash.getWidth() &&
                babyY + baby.getHeight() >= (screenHeight - trash.getHeight()) &&
                babyY <= screenHeight;
    }

    private boolean babyReachesBottom() {
        return babyAnimation && babyY + baby.getHeight() >= screenHeight;
    }

    private boolean touchWithinDragonBounds(float touchX, float touchY) {
        return touchX >= dragonX && touchX <= (dragonX + dragon.getWidth()) &&
                touchY >= dragonY && touchY <= (dragonY + dragon.getHeight());
    }

    private void increasePoints() {
        points++;
    }

    private void launchGameOverActivity() {
        Context context = getContext();
        Intent intent = new Intent(context, GameOver.class);
        intent.putExtra("points", points);
        context.startActivity(intent);
        ((Activity) context).finish();
    }
}
