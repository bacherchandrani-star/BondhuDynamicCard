
package com.bondhubazar.dynamiccard.bondhudynamiccard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.appinventor.components.annotations.Extension;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

@Extension(
    description = "Bondhu Bazar Dynamic Product Card Extension",
    icon = "icon.png"
)
public class BondhuDynamicCard extends AndroidViewComponent {

    private final Context context;
    private final GridLayout grid;

    public BondhuDynamicCard(ComponentContainer container) {
        super(container);

        context = container.$context();

        grid = new GridLayout(context);
        grid.setColumnCount(2);
        grid.setUseDefaultMargins(false);

        container.$add(this);
    }

    @Override
    public View getView() {
        return grid;
    }

    // Old function - preserved
    @SimpleFunction(description = "Returns the sum of given integers.")
    public int SumAll(YailList listOfInts) {
        int sum = 0;

        for (Object o : listOfInts.toArray()) {
            try {
                sum += Integer.parseInt(o.toString());
            } catch (NumberFormatException e) {
                throw new YailRuntimeError(
                    e.toString(), "NumberFormatException"
                );
            }
        }

        return sum;
    }

    // Set number of columns
    @SimpleFunction(description = "Sets the number of product card columns.")
    public void SetColumns(int columns) {
        grid.setColumnCount(Math.max(1, columns));
    }

    // Add Product Card
    @SimpleFunction(description = "Adds a product card to the grid.")
    public void AddProductCard(
        String productId,
        String name,
        String pack,
        String price,
        int imageRes
    ) {

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(
            dp(6), dp(6), dp(6), dp(6)
        );

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), Color.LTGRAY);
        card.setBackground(bg);

        GridLayout.LayoutParams cardParams =
            new GridLayout.LayoutParams();

        cardParams.width = 0;
        cardParams.height = dp(200);
        cardParams.columnSpec =
            GridLayout.spec(GridLayout.UNDEFINED, 1f);

        cardParams.setMargins(
            dp(4), dp(4), dp(4), dp(4)
        );

        card.setLayoutParams(cardParams);

        // Image area
        FrameLayout imageBox = new FrameLayout(context);

        ImageView image = new ImageView(context);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (imageRes != 0) {
            image.setImageResource(imageRes);
        }

        imageBox.addView(
            image,
            new FrameLayout.LayoutParams(
                -1, dp(95)
            )
        );

        // Favorite
        TextView favorite = new TextView(context);
        favorite.setText("♡");
        favorite.setTextSize(25);
        favorite.setTextColor(Color.GRAY);
        favorite.setGravity(Gravity.CENTER);
        favorite.setBackgroundColor(Color.WHITE);

        FrameLayout.LayoutParams favParams =
            new FrameLayout.LayoutParams(
                dp(35), dp(35),
                Gravity.TOP | Gravity.RIGHT
            );

        imageBox.addView(favorite, favParams);
        card.addView(imageBox);

        // Product name
        TextView productName = new TextView(context);
        productName.setText(name);
        productName.setTextSize(14);
        productName.setTextColor(Color.BLACK);
        productName.setTypeface(null, Typeface.BOLD);
        productName.setMaxLines(1);
        card.addView(productName);

        // Pack size
        TextView packSize = new TextView(context);
        packSize.setText(pack);
        packSize.setTextSize(11);
        packSize.setTextColor(Color.GRAY);
        card.addView(packSize);

        // Price
        TextView productPrice = new TextView(context);
        productPrice.setText("₹" + price);
        productPrice.setTextSize(16);
        productPrice.setTextColor(Color.rgb(20, 140, 60));
        productPrice.setTypeface(null, Typeface.BOLD);
        card.addView(productPrice);

        // Add to Cart
        Button addButton = new Button(context);
        addButton.setText("Add to Cart");
        addButton.setTextSize(11);

        LinearLayout.LayoutParams buttonParams =
            new LinearLayout.LayoutParams(
                -1, dp(38)
            );

        card.addView(addButton, buttonParams);

        // Favorite click
        final boolean[] isFavorite = {false};

        favorite.setOnClickListener(v -> {
            isFavorite[0] = !isFavorite[0];

            favorite.setText(
                isFavorite[0] ? "♥" : "♡"
            );

            favorite.setTextColor(
                isFavorite[0] ? Color.RED : Color.GRAY
            );

            FavoriteChanged(productId, isFavorite[0]);
        });

        // Add to Cart click
        addButton.setOnClickListener(v -> {
            AddToCart(productId);
        });

        // Product card click
        card.setOnClickListener(v -> {
            ProductClicked(productId);
        });

        grid.addView(card);
    }

    // Clear all cards
    @SimpleFunction(description = "Removes all product cards.")
    public void ClearCards() {
        grid.removeAllViews();
    }

    // Events
    @SimpleEvent(description = "Triggered when Add to Cart is clicked.")
    public void AddToCart(String productId) {
        EventDispatcher.dispatchEvent(
            this, "AddToCart", productId
        );
    }

    @SimpleEvent(description = "Triggered when favorite status changes.")
    public void FavoriteChanged(
        String productId,
        boolean isFavorite
    ) {
        EventDispatcher.dispatchEvent(
            this, "FavoriteChanged",
            productId, isFavorite
        );
    }

    @SimpleEvent(description = "Triggered when a product card is clicked.")
    public void ProductClicked(String productId) {
        EventDispatcher.dispatchEvent(
            this, "ProductClicked", productId
        );
    }

    private int dp(int value) {
        return (int) (
            value * context.getResources()
            .getDisplayMetrics().density + 0.5f
        );
    }
}
