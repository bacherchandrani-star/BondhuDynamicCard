
package com.bondhubazar.dynamiccard.bondhudynamiccard;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.content.Context;
import java.net.URL;
import java.util.HashMap;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.YailList;

@UsesPermissions(permissionNames = "android.permission.INTERNET")
@Extension(
    description = "Dynamic Product Card Extension for Bondhu Bazar",
    icon = "icon.png"
)
public class BondhuDynamicCard extends AndroidNonvisibleComponent {

    private Context context;
    private HashMap<String, LinearLayout> cards =
        new HashMap<String, LinearLayout>();

    private HashMap<String, ImageView> images =
        new HashMap<String, ImageView>();

    private HashMap<String, TextView> titles =
        new HashMap<String, TextView>();

    private HashMap<String, TextView> prices =
        new HashMap<String, TextView>();

    public BondhuDynamicCard(ComponentContainer container) {
        super(container.$form());
        context = container.$context();
    }

    private int dp(float value) {
        return (int) (value * context.getResources()
            .getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout findLayout(View view) {
        if (view instanceof LinearLayout) {
            return (LinearLayout) view;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                LinearLayout found =
                    findLayout(group.getChildAt(i));

                if (found != null) return found;
            }
        }

        return null;
    }

    @SimpleFunction(description = "Create a product card inside an Arrangement.")
    public void CreateCard(
        HVArrangement parent,
        String id,
        String title,
        String price
    ) {
        LinearLayout layout = findLayout(parent.getView());

        if (layout == null) return;

        if (cards.containsKey(id)) {
            RemoveCard(id);
        }

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(12));
        background.setStroke(dp(1), Color.LTGRAY);

        card.setBackground(background);
        card.setElevation(dp(3));

        LinearLayout.LayoutParams cardParams =
            new LinearLayout.LayoutParams(
                -1, -2
            );

        cardParams.setMargins(dp(5), dp(5), dp(5), dp(5));

        ImageView image = new ImageView(context);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundColor(Color.LTGRAY);

        LinearLayout.LayoutParams imageParams =
            new LinearLayout.LayoutParams(-1, dp(140));

        card.addView(image, imageParams);

        TextView name = new TextView(context);
        name.setText(title);
        name.setTextSize(18);
        name.setTextColor(Color.BLACK);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(0, dp(8), 0, dp(4));

        card.addView(name);

        TextView amount = new TextView(context);
        amount.setText("₹" + price);
        amount.setTextSize(16);
        amount.setTextColor(Color.rgb(30, 130, 60));
        amount.setTypeface(null, Typeface.BOLD);

        card.addView(amount);

        Button addButton = new Button(context);
        addButton.setText("Add to Cart");

        addButton.setOnClickListener(v -> {
            AddToCartClicked(id, name.getText().toString(),
                amount.getText().toString());
        });

        card.addView(addButton);

        card.setOnClickListener(v -> {
            CardClicked(id, name.getText().toString(),
                amount.getText().toString());
        });

        layout.addView(card, cardParams);

        cards.put(id, card);
        images.put(id, image);
        titles.put(id, name);
        prices.put(id, amount);

        CardCreated(id);
    }

    @SimpleFunction(description = "Change product title.")
    public void SetTitle(String id, String title) {
        if (titles.containsKey(id)) {
            titles.get(id).setText(title);
        }
    }

    @SimpleFunction(description = "Change product price.")
    public void SetPrice(String id, String price) {
        if (prices.containsKey(id)) {
            prices.get(id).setText("₹" + price);
        }
    }

    @SimpleFunction(description = "Change card background color.")
    public void SetCardColor(String id, int color) {
        if (cards.containsKey(id)) {
            cards.get(id).setBackgroundColor(color);
        }
    }

    @SimpleFunction(description = "Set product image using an internet URL.")
    public void SetImage(String id, String imageUrl) {
        if (!images.containsKey(id)) return;

        final ImageView target = images.get(id);

        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(
                    url.openConnection().getInputStream()
                );

                if (bitmap != null) {
                    form.runOnUiThread(() -> target.setImageBitmap(bitmap));
                }
            } catch (Exception e) {
                form.runOnUiThread(() ->
                    ImageError(id, e.toString()));
            }
        }).start();
    }

    @SimpleFunction(description = "Remove a product card.")
    public void RemoveCard(String id) {
        if (cards.containsKey(id)) {
            LinearLayout card = cards.remove(id);

            ViewGroup parent = (ViewGroup) card.getParent();

            if (parent != null) parent.removeView(card);

            images.remove(id);
            titles.remove(id);
            prices.remove(id);
        }
    }

    @SimpleFunction(description = "Remove all product cards.")
    public void ClearCards() {
        for (String id : new java.util.ArrayList<String>(cards.keySet())) {
            RemoveCard(id);
        }
    }

    @SimpleEvent(description = "Fires when a card is created.")
    public void CardCreated(String id) {
        EventDispatcher.dispatchEvent(this, "CardCreated", id);
    }

    @SimpleEvent(description = "Fires when a product card is clicked.")
    public void CardClicked(String id, String title, String price) {
        EventDispatcher.dispatchEvent(this, "CardClicked",
            id, title, price);
    }

    @SimpleEvent(description = "Fires when Add to Cart is clicked.")
    public void AddToCartClicked(String id, String title, String price) {
        EventDispatcher.dispatchEvent(this, "AddToCartClicked",
            id, title, price);
    }

    @SimpleEvent(description = "Fires if an image cannot be loaded.")
    public void ImageError(String id, String error) {
        EventDispatcher.dispatchEvent(this, "ImageError", id, error);
    }
}
