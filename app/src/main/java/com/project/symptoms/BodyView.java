package com.project.symptoms;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class BodyView extends View {

    // Used to draw the circles over the image
    private Paint redBrush;

    // Max/Min seekBar value that represents circle sizes range
    private int MAX_SEEKBAR_VALUE = 100;
    private int MIN_SEEKBAR_VALUE = 10;

    // The image to draw on the screen
    private Drawable imageDrawable;

    // The (X,Y) pairs of the points to be drawn over the image
    private ArrayList<float[]> points;
    private ArrayList<float[]> tmpPoints;

    // Used to know the current state in order to change it accordingly
    private State currentState;
    private BodyType currentBodyType;

    // Enums to limit the options to use
    public enum BodyType{MALE, FEMALE};
    public enum State{FRONT, BACK};

    public BodyView(Context context) {
        super(context);
        init();
    }

    public BodyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BodyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void addPoint(float x, float y, float radius){
        float[] point = {x, y, radius};
        points.add(point);
        invalidate();
    }

    private void addTmpPoint(float x, float y, float radius){
        float[] point = {x, y, radius};
        tmpPoints.add(point);
        invalidate();
    }

    private void init(){
        redBrush = new Paint();
        redBrush.setAntiAlias(false);
        redBrush.setStyle(Paint.Style.FILL);
        redBrush.setColor(Color.RED);

        points = new ArrayList<>();
        tmpPoints = new ArrayList<>();

        imageDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.img_male_front, null);

        // Default state
        setBodyType(BodyType.MALE);
        setState(State.FRONT);

    }


    /**
     * Update {@link #imageDrawable} image to match the {@link #currentBodyType} and {@link #currentState}
     */

    private void updateImage(){
        int targetImageResId = R.drawable.img_male_front;// Default value

        if(currentBodyType == BodyType.FEMALE){
            targetImageResId = (currentState == State.FRONT) ? R.drawable.img_female_front : R.drawable.img_female_back;
        }
        else if(currentBodyType == BodyType.MALE){
            targetImageResId = (currentState == State.FRONT) ? R.drawable.img_male_front : R.drawable.img_male_back;
        }

        imageDrawable = ResourcesCompat.getDrawable(getResources(), targetImageResId, null);
        invalidate(); // Force redrawing
    }


    /**
     * Update the {@link #currentBodyType} attribute
     */
    public void setBodyType(BodyType newBodyType){
        currentBodyType = newBodyType;
        updateImage();
    }

    /**
     * Update the {@link #currentState} attribute
     */
    public void setState(State newState){
        currentState = newState;
        updateImage();
    }

    /**
     * Toggle {@link #currentState} and empty {@link #points}
     */
    public void flip(){
        currentState = (currentState==State.FRONT) ? State.BACK : State.FRONT;
        points.clear();
        updateImage();
    }

    /**
     * Draw the current image and draw the list of points over the image
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Set the dimensions to draw the image
        imageDrawable.setBounds(0,0,getWidth(),(int)(getHeight()*0.75f));
        imageDrawable.draw(canvas);

        // Draw the temporary points over the image
        for(float[] tmpPoint : tmpPoints){
            canvas.drawCircle(tmpPoint[0], tmpPoint[1], tmpPoint[2], redBrush);
        }

        // Draw the points over the image
        for(float[] point : points){
            canvas.drawCircle(point[0], point[1], point[2], redBrush);
        }

    }

    /**
     * When the image is touched
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float xPos = event.getX(), yPos = event.getY();
            // Let user choose circle size
            chooseCircleSize(xPos, yPos);
        }
        return true;
    }

    private void chooseCircleSize(final float xPos, final float yPos) {

        // Set up bottom sheet
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);

        // Inflate bottom sheet view to add it to the dialog
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(
                R.layout.layout_bottom_sheet_seekbar,
                (LinearLayout)findViewById(R.id.bottomSheetContainer)
        );
        bottomSheetDialog.setContentView(bottomSheetView);

        // Listen to seek bar changes made by user
        final SeekBar seekBar = bottomSheetView.findViewById(R.id.seekBar);
        seekBar.setMax(MAX_SEEKBAR_VALUE);
        seekBar.setProgress(MIN_SEEKBAR_VALUE);

        addPoint(xPos, yPos, Integer.valueOf(MIN_SEEKBAR_VALUE).floatValue());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tmpPoints.clear();
                addTmpPoint(xPos, yPos, Integer.valueOf(i).floatValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();

        Button doneButton = bottomSheetView.findViewById(R.id.size_done_button);
        doneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tmpPoints.clear();
                // Once the size was chosen then draw that circle and clean the tmp ones
                addPoint(xPos, yPos, Integer.valueOf(seekBar.getProgress()).floatValue());
                bottomSheetDialog.dismiss();
            }
        });

    }
}
