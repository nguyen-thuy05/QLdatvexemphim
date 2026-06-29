package com.example.codeappdatvexemphim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.Random;

public class BarcodeView extends View {

    private String ticketCode = "BKG-000000";
    private Paint paint;

    public BarcodeView(Context context) {
        super(context);
        init();
    }

    public BarcodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarcodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setTicketCode(String ticketCode) {
        if (ticketCode != null && !ticketCode.isEmpty()) {
            this.ticketCode = ticketCode;
            invalidate(); // Redraw view
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Get view dimensions
        int width = getWidth();
        int height = getHeight();

        // Draw white background
        canvas.drawColor(Color.WHITE);

        // Generate deterministic barcode lines based on ticket code
        long seed = ticketCode.hashCode();
        Random random = new Random(seed);

        float currentX = 20f; // Left padding
        float endX = width - 20f; // Right padding

        while (currentX < endX) {
            // Random stripe width between 2 and 8 pixels
            float stripeWidth = 2 + random.nextInt(6);
            
            // Random space width between 2 and 6 pixels
            float spaceWidth = 2 + random.nextInt(5);

            // Draw stripe
            if (currentX + stripeWidth < endX) {
                canvas.drawRect(currentX, 10f, currentX + stripeWidth, height - 10f, paint);
            }
            
            currentX += stripeWidth + spaceWidth;
        }
    }
}
